package cz.aimtec.hackathon.drone.drone;

import android.widget.TextView;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.activities.ADroneActivity;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;

/**
 * Created by pavd on 20.02.2018.
 */

public class DefaultBebopAdapter extends EmptyBebopAdapter
{
    protected ADroneActivity activity;

    public DefaultBebopAdapter(ADroneActivity activity)
    {
        this.activity = activity;
    }

    @Override
    public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state)
    {
        switch (state)
        {
            case ARCONTROLLER_DEVICE_STATE_RUNNING:
                activity.dismissConnectionDialog();
                break;

            case ARCONTROLLER_DEVICE_STATE_STOPPED:
                // if the deviceController is stopped, go back to the previous activity
                activity.dismissConnectionDialog();
                activity.finish();
                break;

            default:
                break;
        }
    }

    @Override
    public void onPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error)
    {
        activity.makeToast("Picture taken");
    }

    @Override
    public void onDownloadComplete(String mediaName)
    {
        activity.makeToast("Download completed");
    }

    @Override
    public void onBatteryChargeChanged(int batteryPercentage)
    {
        TextView tv = (TextView)activity.findViewById(R.id.batteryView);
        if(tv != null)
        {
            tv.setText(String.format("%d%%", batteryPercentage));
        }
    }
}
