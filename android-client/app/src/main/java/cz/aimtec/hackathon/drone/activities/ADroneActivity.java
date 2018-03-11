package cz.aimtec.hackathon.drone.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.drone.BebopDrone;
import cz.aimtec.hackathon.drone.drone.IBebopListener;

/**
 * Abstract activity maintaining drone connection.
 * All inherited classes must provide IBebopListener for reacting on drone events.
 */
public abstract class ADroneActivity extends AppCompatActivity
{
    //drone instance
    protected BebopDrone drone;

    protected ProgressDialog connectionProgressDialog;
    protected ProgressDialog mDownloadProgressDialog;

    protected int mNbMaxDownload;
    protected int mCurrentDownloadIndex;

    protected IBebopListener bebopListener;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        bebopListener = initBebopListener();

        Intent intent = getIntent();

        if (drone == null)
        {
            ARDiscoveryDeviceService service = intent.getParcelableExtra(ConnectingActivity.EXTRA_DEVICE_SERVICE);

            //creating drone instance, it needs current activity
            drone = new BebopDrone(this, service);

            //some activities have this textview
            TextView tv = findViewById(R.id.droneNameView);
            if (tv != null)
            {
                tv.setText("Drone: " + service.getName());
            }
        }

        drone.setMaxRotationSpeed(90);
        drone.setMaxVerticalSpeed(3);
        drone.setMaxTilt(20);
    }

    public void makeToast(String message)
    {
        runOnUiThread(() -> Toast.makeText(ADroneActivity.this, message, Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // show a loading view while the bebop drone is connecting
        if ((drone != null) && !(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(drone.getConnectionState())))
        {
            connectionProgressDialog = new ProgressDialog(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
            connectionProgressDialog.setIndeterminate(true);
            connectionProgressDialog.setMessage("Connecting ...");
            connectionProgressDialog.setCancelable(false);
            connectionProgressDialog.show();

            // if the connection to the Bebop fails, finish the activity
            if (!drone.connect())
            {
                finish();
            } else
            {
                drone.setProtection(true);
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (drone != null)
        {
            connectionProgressDialog = new ProgressDialog(this, R.style.ThemeOverlay_AppCompat_Dialog_Alert);
            connectionProgressDialog.setIndeterminate(true);
            connectionProgressDialog.setMessage("Disconnecting ...");
            connectionProgressDialog.setCancelable(false);
            connectionProgressDialog.show();

            if (!drone.disconnect())
            {
                finish();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        drone.dispose();
        super.onDestroy();
    }

    public IBebopListener getBebopListener()
    {
        return bebopListener;
    }

    /***
     * Called once at construction time. Extending class must fill its implementation or some adapter.
     */
    protected abstract IBebopListener initBebopListener();

    public abstract Context getCurrentContext();

    public void dismissConnectionDialog()
    {
        connectionProgressDialog.dismiss();
    }
}
