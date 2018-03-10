package cz.aimtec.hackathon.drone.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.ProgressBar;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.drone.DefaultBebopAdapter;
import cz.aimtec.hackathon.drone.drone.DemoExecutor;
import cz.aimtec.hackathon.drone.drone.IFrameListener;
import cz.aimtec.hackathon.drone.drone.IBebopListener;
import cz.aimtec.hackathon.drone.drone.IBitmapResolverListener;
import cz.aimtec.hackathon.drone.views.StreamProcessingView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo activity for demonstration of drone features
 * QR reading, video stream, set of commands
 */
public class DemoActivity extends ADroneActivity implements IFrameListener
{
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
    //drone demo code
    private DemoExecutor demoExecutor;
    //thread that executes demo
    protected Thread demoThread;

    protected Thread qrThread = new Thread()
    {
        public void run()
        {

            while (true)
            {
                //wait for stream frame notify
                pauseThread();
                //locking thread
                isLocked = true;
                //trying to decode
                decodeBarcode();

                //wait for a while
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                //unlocking thread
                isLocked = false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        videoStreamView = findViewById(R.id.videoStreamView);
        progressBar = findViewById(R.id.demoProgressBar);

        qrLockToken = new Object();
        isLocked = false;

        bitmapListeners = new ArrayList<>();

        demoExecutor = new DemoExecutor(drone, this);
        //demo executor is ours bitmap listener
        addBitmapListener(demoExecutor);

        barCodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        //start qr resolving thread
        qrThread.start();
    }

    @Override
    public Context getCurrentContext()
    {
        return DemoActivity.this;
    }

    public void addBitmapListener(IBitmapResolverListener listener)
    {
        bitmapListeners.add(listener);
    }

    /***
     * Our reactions on bebop events
     */
    @Override
    protected IBebopListener initBebopListener()
    {
        return new DefaultBebopAdapter(this)
        {
            @Override
            public void configureDecoder(ARControllerCodec codec)
            {
                videoStreamView.configureDecoder(codec);
            }

            @Override
            public void onFrameReceived(ARFrame frame)
            {
                videoStreamView.displayFrame(frame);
            }
        };
    }

    public void decodeBarcode()
    {
        synchronized (qrLockToken)
        {
            if (currentBitmap == null)
            {
                return;
            }

            if (!barCodeDetector.isOperational())
            {
                return;
            }

            Frame frame = new Frame.Builder().setBitmap(currentBitmap).build();
            SparseArray<Barcode> barcodes = barCodeDetector.detect(frame);

            if (barcodes.size() > 0)
            {
                Barcode thisCode = barcodes.valueAt(0);
                String result = thisCode.rawValue;

                this.makeToast(result);

                System.out.println(result);

                notifyBitmapListeners(result);
            }

            currentBitmap = null;
        }
    }

    public void notifyBitmapListeners(String result)
    {
        for (IBitmapResolverListener listener : bitmapListeners)
        {
            listener.qrResolved(result);
        }
    }

    /**
     * Wakes up QR thread from waiting
     */
    public void notifyThread()
    {
        try
        {
            if (qrLockToken != null)
            {
                synchronized (qrLockToken)
                {
                    qrLockToken.notifyAll();
                }
            }
        } catch (Exception e)
        {
            System.err.println("Nothing to notify");
        }
    }

    /**
     * Puts current thread to waiting, used by QR thread
     */
    public void pauseThread()
    {
        try
        {
            synchronized (qrLockToken)
            {
                qrLockToken.wait();
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public void updateProgressBar(double step, double steps)
    {
        updateProgressBar((int) (step * (100d / steps)));
    }

    public void updateProgressBar(final int value)
    {
        runOnUiThread(() -> progressBar.setProgress(value));
    }

    @Override
    public void setFrame(Bitmap bitmap)
    {
        synchronized (qrLockToken)
        {
            currentBitmap = bitmap;
        }
        notifyThread();
    }

    @Override
    public boolean canWrite()
    {
        return !isLocked;
    }

    /**
     * Starts demo 1
     */
    public void startClick(View v)
    {
        demoThread = new Thread()
        {
            public void run()
            {
                runOnUiThread(() -> findViewById(R.id.startBtn).setEnabled(false));
                demoExecutor.run();
                runOnUiThread(() -> findViewById(R.id.startBtn).setEnabled(true));
            }
        };

        demoThread.start();
    }

    /**
     * Used for emergency landing
     */
    public void landClick(View v)
    {
        System.out.println("LANDING BY HAND");

        try
        {
            //we have to kill that thread immediately
            demoThread.stop();
        } catch (Exception e)
        {
        }

        //reset drone angle
        demoExecutor.resetGRYF();
        drone.land();
    }

    /**
     * Cuts out motors and drone falls!
     */
    public void emergency(View v)
    {
        drone.emergency();
    }

    /**
     * Starts demo2
     */
    public void demo2Click(View v)
    {
        demoThread = new Thread()
        {
            public void run()
            {
                runOnUiThread(() -> findViewById(R.id.demoButton).setEnabled(false));
                demoExecutor.run();
                runOnUiThread(() -> findViewById(R.id.demoButton).setEnabled(true));
            }
        };

        demoThread.start();
    }
}