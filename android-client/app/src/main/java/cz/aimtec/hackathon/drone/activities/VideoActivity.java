package cz.aimtec.hackathon.drone.activities;

import android.content.Context;
import android.os.Bundle;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.drone.DefaultBebopAdapter;
import cz.aimtec.hackathon.drone.drone.IBebopListener;
import cz.aimtec.hackathon.drone.views.H264VideoView;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

/**
 * Simple activity for receiving video stream by H264VideoView
 * */
public class VideoActivity extends ADroneActivity
{

    protected H264VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        initIHM();
    }

    @Override
    public Context getCurrentContext()
    {
        return VideoActivity.this;
    }


    private void initIHM()
    {
        videoView = (H264VideoView) findViewById(R.id.videoView);
    }


    @Override
    protected IBebopListener initBebopListener()
    {
        return new DefaultBebopAdapter(this)
        {
            @Override
            public void configureDecoder(ARControllerCodec codec)
            {
                videoView.configureDecoder(codec);
            }

            @Override
            public void onFrameReceived(ARFrame frame)
            {
                videoView.displayFrame(frame);
            }

        };
    }
}
