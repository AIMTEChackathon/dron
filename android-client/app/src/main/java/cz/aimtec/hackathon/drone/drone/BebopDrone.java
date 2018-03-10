package cz.aimtec.hackathon.drone.drone;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import cz.aimtec.hackathon.drone.activities.ADroneActivity;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIASTREAMING_VIDEOSTREAMMODE_MODE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_VIDEOSTABILIZATIONMODE_MODE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_FAMILY_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.arutils.ARUTILS_DESTINATION_ENUM;
import com.parrot.arsdk.arutils.ARUTILS_FTP_TYPE_ENUM;
import com.parrot.arsdk.arutils.ARUtilsException;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.util.ArrayList;
import java.util.List;

public class BebopDrone
{
    private static final String TAG = "BebopDrone";

    private final List<IBebopListener> mListeners;

    private final Handler mHandler;
    private final Context mContext;

    // extra for take off + land
    private Object flyingNotifyToken;
    //for relative moveToRelativePosition
    private Object moveNotifyToken;

    private ARDeviceController mDeviceController;
    private SDCardModule mSDCardModule;
    private ARCONTROLLER_DEVICE_STATE_ENUM mState;
    private ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM mFlyingState;
    private String mCurrentRunId;
    private ARDiscoveryDeviceService mDeviceService;
    private ARUtilsManager mFtpListManager;
    private ARUtilsManager mFtpQueueManager;

    public BebopDrone(Context context, @NonNull ARDiscoveryDeviceService deviceService)
    {
        mContext = context;
        mListeners = new ArrayList<>();
        mDeviceService = deviceService;

        // needed because some callbacks will be called on the main thread
        mHandler = new Handler(context.getMainLooper());

        mState = ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED;

        IBebopListener listener = ((ADroneActivity) context).getBebopListener();
        addListener(listener);


        // if the product type of the deviceService match with the types supported
        ARDISCOVERY_PRODUCT_ENUM productType = ARDiscoveryService.getProductFromProductID(mDeviceService.getProductID());
        ARDISCOVERY_PRODUCT_FAMILY_ENUM family = ARDiscoveryService.getProductFamily(productType);
        if (ARDISCOVERY_PRODUCT_FAMILY_ENUM.ARDISCOVERY_PRODUCT_FAMILY_ARDRONE.equals(family))
        {

            ARDiscoveryDevice discoveryDevice = createDiscoveryDevice(mDeviceService);
            if (discoveryDevice != null)
            {
                mDeviceController = createDeviceController(discoveryDevice);
                discoveryDevice.dispose();
            }

            try
            {
                mFtpListManager = new ARUtilsManager();
                mFtpQueueManager = new ARUtilsManager();

                mFtpListManager.initFtp(mContext, deviceService, ARUTILS_DESTINATION_ENUM.ARUTILS_DESTINATION_DRONE, ARUTILS_FTP_TYPE_ENUM.ARUTILS_FTP_TYPE_GENERIC);
                mFtpQueueManager.initFtp(mContext, deviceService, ARUTILS_DESTINATION_ENUM.ARUTILS_DESTINATION_DRONE, ARUTILS_FTP_TYPE_ENUM.ARUTILS_FTP_TYPE_GENERIC);

                mSDCardModule = new SDCardModule(mFtpListManager, mFtpQueueManager);
                mSDCardModule.addListener(mSDCardModuleListener);
            } catch (ARUtilsException e)
            {
                Log.e(TAG, "Exception", e);
            }

        } else
        {
            Log.e(TAG, "DeviceService type is not supported by BebopDrone");
        }
    }

    public void dispose()
    {
        if (mDeviceController != null)
            mDeviceController.dispose();
        if (mFtpListManager != null)
            mFtpListManager.closeFtp(mContext, mDeviceService);
        if (mFtpQueueManager != null)
            mFtpQueueManager.closeFtp(mContext, mDeviceService);
    }

    public void setFlyingNotifyToken(Object t)
    {
        flyingNotifyToken = t;
    }

    public void setMoveNotifyToken(Object t)
    {
        moveNotifyToken = t;
    }

    private void pauseExecutor(Object notifyToken)
    {
        if (notifyToken != null)
        {
            try
            {
                synchronized (notifyToken)
                {
                    notifyToken.wait();
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
                System.err.println("Unable to wait!");
            }
        }
    }

    private void notifyExecutor(Object notifyToken)
    {
        try
        {
            if (notifyToken != null)
            {
                synchronized (notifyToken)
                {
                    notifyToken.notifyAll();
                }
            }
        } catch (Exception e)
        {
            System.err.println("Unable to notify!");
            e.printStackTrace();
            land();
        }
    }

    //region Listener functions
    public void addListener(IBebopListener listener)
    {
        mListeners.add(listener);
    }

    public void removeListener(IBebopListener listener)
    {
        mListeners.remove(listener);
    }
    //endregion Listener

    /**
     * Connect to the drone
     *
     * @return true if operation was successful.
     * Returning true doesn't mean that device is connected.
     * You can be informed of the actual connection through {@link IBebopListener#onDroneConnectionChanged}
     */
    public boolean connect()
    {
        boolean success = false;
        if ((mDeviceController != null) && (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mState)))
        {
            ARCONTROLLER_ERROR_ENUM error = mDeviceController.start();
            if (error == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)
            {
                success = true;
            }
        }
        return success;
    }

    /**
     * Disconnect from the drone
     *
     * @return true if operation was successful.
     * Returning true doesn't mean that device is disconnected.
     * You can be informed of the actual disconnection through {@link IBebopListener#onDroneConnectionChanged}
     */
    public boolean disconnect()
    {
        boolean success = false;
        if ((mDeviceController != null) && (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mState)))
        {
            ARCONTROLLER_ERROR_ENUM error = mDeviceController.stop();
            if (error == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK)
            {
                success = true;
            }
        }
        return success;
    }

    /**
     * Get the current connection state
     *
     * @return the connection state of the drone
     */
    public ARCONTROLLER_DEVICE_STATE_ENUM getConnectionState()
    {
        return mState;
    }

    /**
     * Get the current flying state
     *
     * @return the flying state
     */
    public ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM getFlyingState()
    {
        return mFlyingState;
    }

    public boolean isRunning()
    {
        return (mDeviceController != null) && (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING));
    }

    /**
     * Tells whether drone is on the ground. This method is not full opposite to isInTheAir!
     */
    public boolean isOnTheGround()
    {
        return mFlyingState == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED
                || mFlyingState == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_USERTAKEOFF;
    }

    /**
     * Tells whether drone is in the air. This method is not full opposite to isOnTheGround!
     */
    public boolean isInTheAir()
    {
        return mFlyingState == ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_FLYING;
    }

    public void setMaxVerticalSpeed(double meterPerSec)
    {
        setMaxVerticalSpeed((float)meterPerSec);
    }

    public void setMaxVerticalSpeed(float meterPerSec)
    {
        mDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxVerticalSpeed(meterPerSec);
    }

    public void setMaxRotationSpeed(double degreesPerSec)
    {
        setMaxRotationSpeed((float) degreesPerSec);
    }

    public void setMaxRotationSpeed(float degreesPerSec)
    {
        mDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxRotationSpeed(degreesPerSec);
    }

    /**
     * Forwards/backwards tilt
     * @param degrees
     */
    public void setMaxTilt(float degrees)
    {
        mDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxTilt(degrees);
    }

    public void takeOff()
    {
        if (isRunning())
        {
            mDeviceController.getFeatureARDrone3().sendPilotingTakeOff();
            pauseExecutor(flyingNotifyToken);
        }
    }

    public void land()
    {
        if (isRunning())
        {
            mDeviceController.getFeatureARDrone3().sendPilotingLanding();
            pauseExecutor(flyingNotifyToken);
        }
    }

    public void emergency()
    {
        if (isRunning())
        {
            mDeviceController.getFeatureARDrone3().sendPilotingEmergency();
        }
    }

    public void takePicture()
    {
        if (isRunning())
        {
            mDeviceController.getFeatureARDrone3().sendMediaRecordPictureV2();
            System.err.println("Photo command sent");
        }
    }

    public void enableVideoStream(boolean enable)
    {
        mDeviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable(enable ? (byte) 1 : 0);
    }

    public void enableStabilizationMode(boolean enable)
    {
        ARCOMMANDS_ARDRONE3_PICTURESETTINGS_VIDEOSTABILIZATIONMODE_MODE_ENUM mode;
        if (enable)
        {
            mode = ARCOMMANDS_ARDRONE3_PICTURESETTINGS_VIDEOSTABILIZATIONMODE_MODE_ENUM.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_VIDEOSTABILIZATIONMODE_MODE_ROLL_PITCH;
        } else
        {
            mode = ARCOMMANDS_ARDRONE3_PICTURESETTINGS_VIDEOSTABILIZATIONMODE_MODE_ENUM.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_VIDEOSTABILIZATIONMODE_MODE_NONE;
        }

        mDeviceController.getFeatureARDrone3().sendPictureSettingsVideoStabilizationMode(mode);
    }

    public void setStreamForQrReading()
    {
        mDeviceController.getFeatureARDrone3().sendMediaStreamingVideoStreamMode(ARCOMMANDS_ARDRONE3_MEDIASTREAMING_VIDEOSTREAMMODE_MODE_ENUM.ARCOMMANDS_ARDRONE3_MEDIASTREAMING_VIDEOSTREAMMODE_MODE_HIGH_RELIABILITY);
        enableStabilizationMode(false);
    }

    /**
     * Send a command <code>PilotingMoveBy</code>
     * Move the drone to a relative position and rotate heading by a given angle.
     * Moves are relative to the current drone orientation, (drone's reference).
     * Also note that the given rotation will not modify the moveToRelativePosition (i.e. moves are always rectilinear).
     * @param x Wanted displacement along the front axis [m]
     * @param y Wanted displacement along the right axis [m]
     * @param z Wanted displacement along the down axis [m]
     * @param rad Wanted rotation of heading [rad]
     * return executing error
     */
    public void moveToRelativePosition(float x, float y, float z, float rad)
    {
        if (isRunning())
        {
            mDeviceController.getFeatureARDrone3().sendPilotingMoveBy(x, y, z, rad);
            pauseExecutor(moveNotifyToken);
        }
    }

    /**
     * Send a command <code>PilotingMoveBy</code>
     * Move the drone to a relative position and rotate heading by a given angle.
     * Moves are relative to the current drone orientation, (drone's reference).
     * Also note that the given rotation will not modify the moveToRelativePosition (i.e. moves are always rectilinear).
     * @param x Wanted displacement along the front axis [m]
     * @param y Wanted displacement along the right axis [m]
     * @param z Wanted displacement along the down axis [m]
     * @param rad Wanted rotation of heading [rad]
     * return executing error
     */
    public void moveToRelativePosition(double x, double y, double z, double rad)
    {
        moveToRelativePosition((float) x, (float) y, (float) z, (float) rad);
    }

    public void moveForward(double x)
    {
        moveToRelativePosition(x, 0, 0, 0);
    }

    public void moveBackward(double x)
    {
        moveToRelativePosition(-x, 0, 0, 0);
    }

    public void moveLeft(double y)
    {
        moveToRelativePosition(0, -y, 0, 0);
    }

    public void moveRight(double y)
    {
        moveToRelativePosition(0, y, 0, 0);
    }

    public void moveUp(double z)
    {
        moveToRelativePosition(0, 0, -z, 0);
    }

    public void moveDown(double z)
    {
        moveToRelativePosition(0, 0, z, 0);
    }

    public void turnLeft(double r)
    {
        moveToRelativePosition(0, 0, 0, r);
    }

    public void turnRight(double r)
    {
        moveToRelativePosition(0, 0, 0, -r);
    }

    public void setProtection(boolean protectionOn)
    {
        mDeviceController.getFeatureARDrone3().sendSpeedSettingsHullProtection((byte) (protectionOn ? 1 : 0));
    }

    /**
     * Never use when drone is being in the air!
     */
    public void doFlatTrim()
    {
        //is running and is on the ground
        if (isRunning() && isOnTheGround())
        {
            mDeviceController.getFeatureARDrone3().sendPilotingFlatTrim();
        } else
        {
            ((ADroneActivity) mContext).makeToast("Drone not on the ground!");
        }
    }

    /**
     * Set the forward/backward angle of the drone
     * Note that {@link BebopDrone#setFlag(byte)} should be set to 1 in order to take in account the pitch value
     *
     * @param pitch value in percentage from -100 to 100
     */
    public void setPitch(byte pitch)
    {
        if ((mDeviceController != null) && (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING)))
        {
            mDeviceController.getFeatureARDrone3().setPilotingPCMDPitch(pitch);
        }
    }

    /**
     * Set the side angle of the drone
     * Note that {@link BebopDrone#setFlag(byte)} should be set to 1 in order to take in account the roll value
     *
     * @param roll value in percentage from -100 to 100
     */
    public void setRoll(byte roll)
    {
        if ((mDeviceController != null) && (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING)))
        {
            mDeviceController.getFeatureARDrone3().setPilotingPCMDRoll(roll);
        }
    }

    public void setYaw(byte yaw)
    {
        if ((mDeviceController != null) && (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING)))
        {
            mDeviceController.getFeatureARDrone3().setPilotingPCMDYaw(yaw);
        }
    }

    public void setGaz(byte gaz)
    {
        if ((mDeviceController != null) && (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING)))
        {
            mDeviceController.getFeatureARDrone3().setPilotingPCMDGaz(gaz);
        }
    }

    /**
     * Take in account or not the pitch and roll values
     *
     * @param flag 1 if the pitch and roll values should be used, 0 otherwise
     */
    public void setFlag(byte flag)
    {
        if ((mDeviceController != null) && (mState.equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING)))
        {
            mDeviceController.getFeatureARDrone3().setPilotingPCMDFlag(flag);
        }
    }

    /**
     * Download the last flight medias
     * Uses the run id to download all medias related to the last flight
     * If no run id is available, download all medias of the day
     */
    public void getLastFlightMedias()
    {
        String runId = mCurrentRunId;
        if ((runId != null) && !runId.isEmpty())
        {
            mSDCardModule.getFlightMedias(runId);
        } else
        {
            Log.e(TAG, "RunID not available, fallback to the day's medias");
            mSDCardModule.getTodaysFlightMedias();
        }
    }

    public void cancelGetLastFlightMedias()
    {
        mSDCardModule.cancelGetFlightMedias();
    }

    private ARDiscoveryDevice createDiscoveryDevice(@NonNull ARDiscoveryDeviceService service)
    {
        ARDiscoveryDevice device = null;
        try
        {
            device = new ARDiscoveryDevice(mContext, service);
        } catch (ARDiscoveryException e)
        {
            Log.e(TAG, "Exception", e);
            Log.e(TAG, "Error: " + e.getError());
        }

        return device;
    }

    private ARDeviceController createDeviceController(@NonNull ARDiscoveryDevice discoveryDevice)
    {
        ARDeviceController deviceController = null;
        try
        {
            deviceController = new ARDeviceController(discoveryDevice);

            deviceController.addListener(mDeviceControllerListener);
            deviceController.addStreamListener(mStreamListener);
        } catch (ARControllerException e)
        {
            Log.e(TAG, "Exception", e);
        }

        return deviceController;
    }

    //region notify listener block
    private void notifyConnectionChanged(ARCONTROLLER_DEVICE_STATE_ENUM state)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onDroneConnectionChanged(state);
        }
    }

    private void notifyBatteryChanged(int battery)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onBatteryChargeChanged(battery);
        }
    }

    private void notifyPilotingStateChanged(ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onPilotingStateChanged(state);
        }
    }

    private void notifyPictureTaken(ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onPictureTaken(error);
        }
    }

    private void notifyConfigureDecoder(ARControllerCodec codec)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.configureDecoder(codec);
        }
    }

    private void notifyFrameReceived(ARFrame frame)
    {
        //use if you want use image data from current stream frame
        //frame.getMetadata().getByteData()

        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onFrameReceived(frame);
        }
    }

    private void notifyMatchingMediasFound(int nbMedias)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onMatchingMediasFound(nbMedias);
        }
    }

    private void notifyDownloadProgressed(String mediaName, int progress)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onDownloadProgressed(mediaName, progress);
        }
    }

    private void notifyDownloadComplete(String mediaName)
    {
        List<IBebopListener> listenersCpy = new ArrayList<>(mListeners);
        for (IBebopListener listener : listenersCpy)
        {
            listener.onDownloadComplete(mediaName);
        }
    }
    //endregion notify listener block

    private final SDCardModule.Listener mSDCardModuleListener = new SDCardModule.Listener()
    {
        @Override
        public void onMatchingMediasFound(final int nbMedias)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyMatchingMediasFound(nbMedias);
                }
            });
        }

        @Override
        public void onDownloadProgressed(final String mediaName, final int progress)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyDownloadProgressed(mediaName, progress);
                }
            });
        }

        @Override
        public void onDownloadComplete(final String mediaName)
        {
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyDownloadComplete(mediaName);
                }
            });
        }
    };

    private final ARDeviceControllerListener mDeviceControllerListener = new ARDeviceControllerListener()
    {
        @Override
        public void onStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARCONTROLLER_ERROR_ENUM error)
        {
            mState = newState;
            if (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mState))
            {
                mDeviceController.startVideoStream();
            } else if (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mState))
            {
                mSDCardModule.cancelGetFlightMedias();
            }
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    notifyConnectionChanged(mState);
                }
            });
        }

        @Override
        public void onExtensionStateChanged(ARDeviceController deviceController, ARCONTROLLER_DEVICE_STATE_ENUM newState, ARDISCOVERY_PRODUCT_ENUM product, String name, ARCONTROLLER_ERROR_ENUM error)
        {
        }

        @Override
        public void onCommandReceived(ARDeviceController deviceController, ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, ARControllerDictionary elementDictionary)
        {
            // if event received is the battery update
            if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    final int battery = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            notifyBatteryChanged(battery);
                        }
                    });

                    System.err.println("BATTERY " + battery + "%");
                }
            }
            // if event received is the flying state update
            else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state = ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));

                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mFlyingState = state;
                            notifyPilotingStateChanged(state);
                        }
                    });

                    if(state==ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_HOVERING||
                            state==ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_LANDED)
                    {
                        notifyExecutor(flyingNotifyToken);
                    }

                    System.err.println("FLYING STATE CHANGED");
                    System.out.println(state);
                }


                //System.out.println(mState);
            }
            // if event received is the picture notification
            else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error = ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.getFromValue((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR));
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            notifyPictureTaken(error);
                        }
                    });
                }
            }
            // if event received is the run id
            else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    final String runID = (String) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED_RUNID);
                    mHandler.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mCurrentRunId = runID;
                        }
                    });
                }

            } else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    float dX = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DX)).doubleValue();
                    float dY = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DY)).doubleValue();
                    float dZ = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DZ)).doubleValue();
                    float dPsi = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_DPSI)).doubleValue();
                    ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM error = ARCOMMANDS_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR_ENUM.getFromValue((Integer) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND_ERROR));

                }

                //notify to console about moveBy method ended
                System.err.println("MOVE BY END");
                System.out.println(mState);
                notifyExecutor(moveNotifyToken);
            } else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    float speedX = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDX)).doubleValue();
                    float speedY = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDY)).doubleValue();
                    float speedZ = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDZ)).doubleValue();
                }
                //System.out.println("SPEED CHANGED");
                //Add your custom logging of speed HERE
            } else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    float roll = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_ROLL)).doubleValue();
                    float pitch = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_PITCH)).doubleValue();
                    float yaw = (float) ((Double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_YAW)).doubleValue();
                }
                //System.out.println("ATTITUDE CHANGED");
                //Add your custom logging of attitude HERE
            } else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED) && (elementDictionary != null))
            {
                ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null)
                {
                    double latitude = (double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE);
                    double longitude = (double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE);
                    double altitude = (double) args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE);
                }
                //System.out.println("POSITION CHANGED");
                //Add your custom logging of position HERE
            }
            //TODO zde lze dale reagovat na prijate udalosti z SDK
        }
    };

    private final ARDeviceControllerStreamListener mStreamListener = new ARDeviceControllerStreamListener()
    {
        @Override
        public ARCONTROLLER_ERROR_ENUM configureDecoder(ARDeviceController deviceController, final ARControllerCodec codec)
        {
            notifyConfigureDecoder(codec);
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
        }

        @Override
        public ARCONTROLLER_ERROR_ENUM onFrameReceived(ARDeviceController deviceController, final ARFrame frame)
        {
            notifyFrameReceived(frame);
            return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
        }

        @Override
        public void onFrameTimeout(ARDeviceController deviceController)
        {
        }
    };
}
