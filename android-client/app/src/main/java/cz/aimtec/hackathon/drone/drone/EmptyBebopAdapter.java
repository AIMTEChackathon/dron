package cz.aimtec.hackathon.drone.drone;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARFrame;

/**
 * Created by pavd on 20.02.2018.
 */

public class EmptyBebopAdapter implements IBebopListener
{
    @Override
    public void onDroneConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state)
    {

    }

    @Override
    public void onBatteryChargeChanged(int batteryPercentage)
    {

    }

    @Override
    public void onPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state)
    {

    }

    @Override
    public void onPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error)
    {

    }

    @Override
    public void configureDecoder(ARControllerCodec codec)
    {

    }

    @Override
    public void onFrameReceived(ARFrame frame)
    {

    }

    @Override
    public void onMatchingMediasFound(int nbMedias)
    {

    }

    @Override
    public void onDownloadProgressed(String mediaName, int progress)
    {

    }

    @Override
    public void onDownloadComplete(String mediaName)
    {

    }

    @Override
    public void positionChanged(double latitude, double longitude, double altitude) {

    }

    @Override
    public void speedChanged(double latitude, double longitude, double altitude) {

    }

    @Override
    public void attitudeChanged(double roll, double pitch, double yaw){

    }

    @Override
    public void altitudeChanged(double altitude){

    }

}
