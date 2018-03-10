package cz.aimtec.hackathon.drone.stocktaking;

import android.content.Context;

import java.util.List;

import cz.aimtec.hackathon.drone.connectivity.AsyncHttpResponseHandlerEmpty;
import cz.aimtec.hackathon.drone.connectivity.DBConnector;
import cz.aimtec.hackathon.drone.connectivity.SewioConnector;
import cz.aimtec.hackathon.drone.drone.BebopDrone;
import cz.aimtec.hackathon.drone.models.Package;
import cz.aimtec.hackathon.drone.models.Point3D;
import cz.aimtec.hackathon.drone.models.Position;

/**
 * Created by Jan Klik on 10.3.2018.
 */

public class StockTakingDispatcher {

    private Context context;
    private final SewioConnector sewioConnector;
    private final DBConnector dbConnector;
    private BebopDrone drone;

    private List<Position> positions;

    private int wantedPositionIndex;

    //private Point3D wantedDronPosition;
    private Point3D actualDronPosition;
    private float actualDronZPosition;

    private PositionReachedListener positionReachedListener;

    public StockTakingDispatcher(Context context, SewioConnector sewioConnector, DBConnector dbConnector, BebopDrone drone) {
        this.context = context;
        this.sewioConnector = sewioConnector;
        this.dbConnector = dbConnector;
        this.drone = drone;
    }

    public void startStocktaking() {
        goToPosition(positions.get(0), 0);
    }

    public void positionReached() {
        if (positionReachedListener != null) {
            Position position = positions.get(wantedPositionIndex);
            positionReachedListener.onPositionReached(position, position.getCenterPoint(), wantedPositionIndex);
        }
    }

    private void goToPosition(Position position, int wantedPositionIndex) {
        this.wantedPositionIndex = wantedPositionIndex;
        positionReachedListener = new PositionReachedListener() {
            @Override
            public void onPositionReached(Position position, Point3D point, int positionIndex) {
                scanQRCode(position, positionIndex);
            }
        };
    }

    private void scanQRCode(Position position, int positionIndex) {
        // TODO scan QR code
        if (true) {
            labelScanned(new Package("fakeLabel", position.getName(), position.getId()), position, positionIndex);
        }
    }

    public void labelScanned(Package label, Position position, int positionIndex) {
        dbConnector.postPackage(context, label, new AsyncHttpResponseHandlerEmpty());
        if (positionIndex < positions.size() - 1) {
            int index = positionIndex + 1;
            goToPosition(positions.get(index), index);
        } else {
            drone.land();
        }
    }

    public List<Position> getPositions() {
        return positions;
    }

    public void setPositions(List<Position> positions) {
        this.positions = positions;
    }
}
