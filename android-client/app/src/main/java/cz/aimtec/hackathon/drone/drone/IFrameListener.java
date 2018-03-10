package cz.aimtec.hackathon.drone.drone;

import android.graphics.Bitmap;

/**
 * Created by pavd on 07.03.2018.
 */

public interface IFrameListener
{
    void setFrame(Bitmap bitmap);

    boolean canWrite();
}
