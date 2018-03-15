package cz.aimtec.hackathon.drone.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.gson.Gson;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

import java.util.ArrayList;
import java.util.List;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.connectivity.AsyncHttpResponseHandlerEmpty;
import cz.aimtec.hackathon.drone.connectivity.DBConnector;
import cz.aimtec.hackathon.drone.connectivity.SewioConnector;
import cz.aimtec.hackathon.drone.connectivity.SewioWebSocketConnector;
import cz.aimtec.hackathon.drone.connectivity.SewioWebSocketListener;
import cz.aimtec.hackathon.drone.drone.DefaultBebopAdapter;
import cz.aimtec.hackathon.drone.drone.IBebopListener;
import cz.aimtec.hackathon.drone.drone.IBitmapResolverListener;
import cz.aimtec.hackathon.drone.drone.IFrameListener;
import cz.aimtec.hackathon.drone.models.Point3D;
import cz.aimtec.hackathon.drone.models.Position;
import cz.aimtec.hackathon.drone.models.SewioWebsocketMessageFeed;
import cz.aimtec.hackathon.drone.stocktaking.StockTakingDispatcher;
import cz.aimtec.hackathon.drone.views.StreamProcessingView;
import cz.msebera.android.httpclient.Header;
import okhttp3.WebSocket;

public class StorageScanActivity extends ADroneActivity implements IFrameListener {

    private StockTakingDispatcher stockTakingDispatcher;
    private SewioConnector sewioConnector = new SewioConnector();
    private DBConnector dbConnector = new DBConnector();
    private SewioWebSocketConnector sewioWebSocketConnector = new SewioWebSocketConnector();

    protected StreamProcessingView videoStreamView;
    protected ProgressBar progressBar;

    //object of barcode detector library
    private BarcodeDetector barCodeDetector;

    //object of received bitmap
    protected Bitmap currentBitmap;
    //lock token for receiving of frames and distributing them to listeners
    protected Object qrLockToken;
    //current state of qrLockToken
    protected boolean isLocked;
    //listeners that wants react on resolved image
    private List<IBitmapResolverListener> bitmapListeners;
    //thread that executes demo
    protected Thread demoThread;

    protected Thread qrThread = new Thread() {
        public void run() {

            while (true) {
                //wait for stream frame notify
                pauseThread();
                //locking thread
                isLocked = true;
                //trying to decode
                decodeBarcode();

                //wait for a while
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //unlocking thread
                isLocked = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_scan);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        videoStreamView = findViewById(R.id.videoStreamView);
        progressBar = findViewById(R.id.demoProgressBar);

        qrLockToken = new Object();
        isLocked = false;

        initSewio();

        bitmapListeners = new ArrayList<>();

        //demo executor is ours bitmap listener
        addBitmapListener(stockTakingDispatcher);

        barCodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        //start qr resolving thread
        qrThread.start();
    }

    private void initSewio()
    {
        stockTakingDispatcher = new StockTakingDispatcher(this, sewioConnector, dbConnector, drone);

        sewioWebSocketConnector.connect(new SewioWebSocketListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                Gson gson = new Gson();
                SewioWebsocketMessageFeed response = gson.fromJson(text, SewioWebsocketMessageFeed.class);
                Point3D point = response.getPoint();

                stockTakingDispatcher.onCurrentDronePositionChanged(point);
            }
        });

        sewioConnector.getModels(new SewioConnector.AsyncSewioResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Object parsedJsonObject, String responseText) {
                stockTakingDispatcher.setPositions((List<Position>) parsedJsonObject);
                makeToast("Received response: " + responseText);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                makeToast("Received response: " + statusCode + response);
            }
        });

        dbConnector.deleteAllPackages(new AsyncHttpResponseHandlerEmpty());
    }

    @Override
    public Context getCurrentContext() {
        return StorageScanActivity.this;
    }

    public void addBitmapListener(IBitmapResolverListener listener) {
        bitmapListeners.add(listener);
    }

    /***
     * Our reactions on bebop events
     */
    @Override
    protected IBebopListener initBebopListener() {
        return new DefaultBebopAdapter(this) {
            @Override
            public void configureDecoder(ARControllerCodec codec) {
                videoStreamView.configureDecoder(codec);
            }

            @Override
            public void onFrameReceived(ARFrame frame) {
                videoStreamView.displayFrame(frame);
            }

            @Override
            public void attitudeChanged(double roll, double pitch, double yaw) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.rollTextView)).setText("Roll: " + roll);
                        ((TextView) findViewById(R.id.pitchTextView)).setText("Pitch: " + pitch);
                        ((TextView) findViewById(R.id.yawTextView)).setText("Yaw: " + yaw);
                    }
                });
            }

            @Override
            public void altitudeChanged(double altitude) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.altitudeTextView)).setText("Alt: " + altitude);
                        Point3D position = new Point3D(0,0, (float) altitude);

                        stockTakingDispatcher.onCurrentDronePositionChanged(position);

                        dbConnector.postPosition(getCurrentContext(), position, new AsyncHttpResponseHandlerEmpty());
                    }
                });
            }

            @Override
            public void speedChanged(double speedX, double speedY, double speedZ) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) findViewById(R.id.xsTextView)).setText("sx: " + speedX);
                        ((TextView) findViewById(R.id.ysTextView)).setText("sy: " + speedY);
                        ((TextView) findViewById(R.id.zsTextView)).setText("sz: " + speedZ);
                    }
                });
            }
        };
    }

    public void decodeBarcode() {
        synchronized (qrLockToken) {
            if (currentBitmap == null) {
                return;
            }

            if (!barCodeDetector.isOperational()) {
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(currentBitmap).build();
            SparseArray<Barcode> barcodes = barCodeDetector.detect(frame);

            if (barcodes.size() > 0) {
                Barcode thisCode = barcodes.valueAt(0);
                String result = thisCode.rawValue;

                this.makeToast(result);

                System.out.println(result);

                notifyBitmapListeners(result);
            }

            currentBitmap = null;
        }
    }

    public void notifyBitmapListeners(String result) {
        for (IBitmapResolverListener listener : bitmapListeners) {
            listener.qrResolved(result);
        }
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

    public void updateProgressBar(double step, double steps) {
        updateProgressBar((int) (step * (100d / steps)));
    }

    public void updateProgressBar(final int value) {
        runOnUiThread(() -> progressBar.setProgress(value));
    }

    @Override
    public void setFrame(Bitmap bitmap) {
        synchronized (qrLockToken) {
            currentBitmap = bitmap;
        }
        notifyThread();
    }

    @Override
    public boolean canWrite() {
        return !isLocked;
    }

    /**
     * Starts job
     */
    public void startClick(View v) {
        demoThread = new Thread() {
            public void run() {
                runOnUiThread(() -> findViewById(R.id.startBtn).setEnabled(false));
                stockTakingDispatcher.startStocktaking();
                runOnUiThread(() -> findViewById(R.id.startBtn).setEnabled(true));
            }
        };

        demoThread.start();
    }


    public void takeOffClick(View v) {
        drone.takeOff();
    }

    /**
     * Used for emergency landing
     */
    public void landClick(View v) {
        System.out.println("LANDING BY HAND");

        try {
            //we have to kill that thread immediately
            demoThread.stop();
        } catch (Exception e) {
        }

        //reset drone angle
        drone.resetGRYF();
        drone.land();
    }

    /**
     * Cuts out motors and drone falls!
     */
    public void emergency(View v) {
        drone.emergency();
    }
}