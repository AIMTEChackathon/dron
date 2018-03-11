package cz.aimtec.hackathon.drone.stocktaking;

import android.content.Context;
import android.util.Log;

import java.util.List;

import cz.aimtec.hackathon.drone.connectivity.AsyncHttpResponseHandlerEmpty;
import cz.aimtec.hackathon.drone.connectivity.DBConnector;
import cz.aimtec.hackathon.drone.connectivity.SewioConnector;
import cz.aimtec.hackathon.drone.drone.BebopDrone;
import cz.aimtec.hackathon.drone.drone.IBitmapResolverListener;
import cz.aimtec.hackathon.drone.models.Package;
import cz.aimtec.hackathon.drone.models.Point3D;
import cz.aimtec.hackathon.drone.models.Position;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class StockTakingDispatcher implements IBitmapResolverListener {

    private Context context;
    private final SewioConnector sewioConnector;
    private final DBConnector dbConnector;
    private BebopDrone drone;
    private static final float DISTANCE_DELTA_TOLLERANCE = 0.2f;
    private Object actualPositionLock = new Object();
    public volatile boolean isCorrectorRunning = false;

    private Runnable positionCorrector = new Runnable() {
        @Override
        public void run() {
            isCorrectorRunning = true;
            while(wantedDronPosition != null){
                try {
                    Point3D targetPosition;
                    Point3D actualPosition;
                    float distanceToTarget;
                    synchronized (actualPositionLock) {
                        targetPosition = wantedDronPosition;
                        actualPosition = actualDronPosition;
                        distanceToTarget = distance(actualPosition, targetPosition);
                    }

                    if(distanceToTarget > DISTANCE_DELTA_TOLLERANCE) {
                        Point3D moveToPosition = calculateMove(actualPosition, targetPosition);
                        drone.moveToRelativePosition(moveToPosition.getX(), moveToPosition.getY(), moveToPosition.getZ(), 0);
                        Thread.sleep(1000);
                    } else {
                        positionReached();
                    }

                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Log.e("positionCorrector", "positionCorrector interrupted", e);
                }
            }
            isCorrectorRunning = false;
        }
    };

    private List<Position> positions;

    private int wantedPositionIndex;

    private Point3D wantedDronPosition;
    private Point3D actualDronPosition;
    private float actualDronZPosition;

    private PositionReachedListener positionReachedListener;

    private String lastQrResult;
    private String qrResult;
    private Object qrLockToken;
    private boolean isLocked;

    public StockTakingDispatcher(Context context, SewioConnector sewioConnector, DBConnector dbConnector, BebopDrone drone) {
        this.context = context;
        this.sewioConnector = sewioConnector;
        this.dbConnector = dbConnector;
        this.drone = drone;

        qrLockToken = new Object();
        qrResult = "";
        lastQrResult = "";
        isLocked = true;
    }

    public void startStocktaking() {
        goToPosition(positions.get(0), 0);
    }

    public void positionReached() {
        if (positionReachedListener != null) {
            Position position = positions.get(wantedPositionIndex);
            positionReachedListener.onPositionReached(position, position.getCenterPoint(), wantedPositionIndex);
            positionReachedListener = null;
        }
    }

    private void goToPosition(Position position, int wantedPositionIndex) {
        synchronized (actualPositionLock) {
            this.wantedPositionIndex = wantedPositionIndex;
            this.wantedDronPosition = positions.get(wantedPositionIndex).getCenterPoint();
        }
        positionReachedListener = new PositionReachedListener() {
            @Override
            public void onPositionReached(Position position, Point3D point, int positionIndex) {
                scanQRCode(position, positionIndex);
            }
        };
    }

    private void scanQRCode(Position position, int positionIndex) {
        //wait for qr code
        pauseThread();

        isLocked = true;
        labelScanned(new Package(qrResult, position.getName(), position.getId()), position, positionIndex);
        lastQrResult = qrResult;
        qrResult = "";
        isLocked = false;
    }

    public void labelScanned(Package label, Position position, int positionIndex) {
        dbConnector.postPackage(context, label, new AsyncHttpResponseHandlerEmpty());
        if (positionIndex < positions.size() - 1) {
            int index = positionIndex + 1;
            goToPosition(positions.get(index), index);
        } else {
            synchronized (actualPositionLock) {
                wantedDronPosition = null;
            }
            drone.land();
        }
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }

    public void onCurrentDronePositionChanged(Point3D point) {
        synchronized (actualPositionLock) {
            if (actualDronPosition == null) {
                actualDronPosition = point;
            } else {
                actualDronPosition.setX(point.getX());
                actualDronPosition.setY(point.getY());
            }
        }
        if (!isCorrectorRunning) {
            Thread t = new Thread(positionCorrector);
            t.start();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private Point3D calculateMove(Point3D actualPosition, Point3D targetPosition){
        float x = targetPosition.getX() - actualPosition.getX();
        float y = targetPosition.getY() - actualPosition.getY();
        float z = targetPosition.getZ() - actualPosition.getZ();

        return new Point3D(x/2, y/2, z/2);
    }

    private float distance(Point3D actualPosition, Point3D targetPosition){
        float x = targetPosition.getX() - actualPosition.getX();
        float y = targetPosition.getY() - actualPosition.getY();
        float z = targetPosition.getZ() - actualPosition.getZ();

        float x2 = x*x;
        float y2 = y*y;
        float z2 = z*z;

        float sum = x2 + y2 + z2;

        return (float) Math.abs(Math.sqrt(sum));
    }

    /**
     * Wakes up QR thread from waiting
     */
    public void notifyThread() {
        try {
            if (qrLockToken != null) {
                synchronized (qrLockToken) {
                    qrLockToken.notifyAll();
                }
            }
        } catch (Exception e) {
            System.err.println("Nothing to notify");
        }
    }

    /**
     * Puts current thread to waiting, used by QR thread
     */
    public void pauseThread() {
        try {
            synchronized (qrLockToken) {
                qrLockToken.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void qrResolved(String result) {

        if (!isLocked && lastQrResult.compareTo(result) != 0 && result.length() > 0) {
            qrResult = result;
            notifyThread();
        }
    }
}
