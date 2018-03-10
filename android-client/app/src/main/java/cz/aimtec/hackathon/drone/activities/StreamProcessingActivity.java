package cz.aimtec.hackathon.drone.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.drone.DefaultBebopAdapter;
import cz.aimtec.hackathon.drone.drone.IBebopListener;
import cz.aimtec.hackathon.drone.views.StreamProcessingView;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

public class StreamProcessingActivity extends ADroneActivity
{

    protected StreamProcessingView videoStreamView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stream_processing);

        initIHM();
    }

    @Override
    public Context getCurrentContext()
    {
        return StreamProcessingActivity.this;
    }


    private void initIHM()
    {
        videoStreamView = findViewById(R.id.videoStreamView);
        //videoStreamView.setSurfaceTextureListener(videoStreamView);
    }


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

    public void saveScreen(View v)
    {
        Bitmap b = videoStreamView.getBitmap();
        if (b != null)
            decodeBarcode(b);
    }

    protected void decodeBarcode(Bitmap bitmap)
    {
        BarcodeDetector detector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational())
        {
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);

        if (barcodes.size() > 0)
        {
            Barcode thisCode = barcodes.valueAt(0);
            makeToast(thisCode.rawValue);
            System.out.println(thisCode.rawValue);
        }
    }
}