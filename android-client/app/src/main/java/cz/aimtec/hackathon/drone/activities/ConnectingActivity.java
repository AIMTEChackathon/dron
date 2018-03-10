package cz.aimtec.hackathon.drone.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.drone.DroneDiscoverer;
import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.aimtec.hackathon.drone.R;
import cz.aimtec.hackathon.drone.connectivity.DBConnector;
import cz.aimtec.hackathon.drone.connectivity.SewioConnector;
import cz.aimtec.hackathon.drone.drone.DroneDiscoverer;
import cz.aimtec.hackathon.drone.models.Package;
import cz.aimtec.hackathon.drone.models.VoiceCommand;
import cz.msebera.android.httpclient.Header;

public class ConnectingActivity extends AppCompatActivity
{

    //private static final Class<?> DEFAULT_CLASS =  BarcodeActivity.class;
    private static final Class<?> DEFAULT_CLASS =  ControlActivity.class;
    //private static final Class<?> DEFAULT_CLASS = VideoActivity.class;
    //private static final Class<?> DEFAULT_CLASS = StreamProcessingActivity.class;
    //private static final Class<?> DEFAULT_CLASS = DemoActivity.class;

    // this block loads the native libraries
    // it is mandatory
    static
    {
        ARSDK.loadSDKLibs();
    }

    public static final String EXTRA_DEVICE_SERVICE = "EXTRA_DEVICE_SERVICE";

    /**
     * List of runtime permission we need.
     */
    private static final String[] PERMISSIONS_NEEDED = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    /**
     * Code for permission request result handling.
     */
    private static final int REQUEST_CODE_PERMISSIONS_REQUEST = 1;

    public DroneDiscoverer mDroneDiscoverer;

    private final List<ARDiscoveryDeviceService> mDronesList = new ArrayList<>();

    protected WifiManager mWifiManager;

    //wifi scanner
    private final BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context c, Intent intent)
        {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                List<ScanResult> scanResults = mWifiManager.getScanResults();
                // add your logic here
                System.out.println("--------WIFIs--------");
                for (ScanResult sr : scanResults)
                {
                    System.out.println(sr.toString());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);


        SewioConnector connector = new SewioConnector();
        connector.getModels(new SewioConnector.AsyncSewioResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Object parsedJsonObject, String responseText) {
                runOnUiThread(() -> Toast.makeText(ConnectingActivity.this, "Received response: " + responseText, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                runOnUiThread(() -> Toast.makeText(ConnectingActivity.this, "Received response: " + statusCode + response, Toast.LENGTH_LONG).show());
            }
        });

        DBConnector dbConnector = new DBConnector();
        dbConnector.deleteAllPackages(new DBConnector.AsyncDBResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // do nothing
            }
            @Override
            public void onSuccess(int statusCode, Object parsedJsonObject, String responseText) {
                // do nothing
            }
        });

        dbConnector.postPackage(this, new Package("1000001", "A12", 2), new DBConnector.AsyncDBResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // do nothing
            }
            @Override
            public void onSuccess(int statusCode, Object parsedJsonObject, String responseText) {
                // do nothing
            }
        });

        dbConnector.getVoiceCommands(new DBConnector.AsyncDBResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Object parsedJsonObject, String responseText) {
                VoiceCommand voiceCommand = (VoiceCommand) parsedJsonObject;
                runOnUiThread(() -> Toast.makeText(ConnectingActivity.this, "received command" + voiceCommand.getCommand(), Toast.LENGTH_LONG ));
            }
        });


        mDroneDiscoverer = new DroneDiscoverer(this);

        Set<String> permissionsToRequest = new HashSet<>();
        for (String permission : PERMISSIONS_NEEDED)
        {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission))
                {
                    Toast.makeText(this, "Please allow permission " + permission, Toast.LENGTH_LONG).show();
                    finish();
                    return;
                } else
                {
                    permissionsToRequest.add(permission);
                }
            }
        }
        if (permissionsToRequest.size() > 0)
        {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    REQUEST_CODE_PERMISSIONS_REQUEST);
        }
    }

    protected void doConnect(ARDiscoveryDeviceService service)
    {
        Intent intent = new Intent(ConnectingActivity.this, DEFAULT_CLASS);
        intent.putExtra(EXTRA_DEVICE_SERVICE, service);
        startActivity(intent);
    }

    public void autoConnectClick(View v)
    {
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings");
        intent.setComponent(cn);
        intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        //mWifiManager = (WifiManager) v.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //mWifiManager.startScan();
        //findViewById(R.id.autoConnectBtn).setEnabled(false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        // setup the drone discoverer and register as listener
        mDroneDiscoverer.setup();
        mDroneDiscoverer.addListener(mDiscovererListener);

        // start discovering
        mDroneDiscoverer.startDiscovering();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        // clean the drone discoverer object
        mDroneDiscoverer.stopDiscovering();
        mDroneDiscoverer.cleanup();
        mDroneDiscoverer.removeListener(mDiscovererListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean denied = false;
        if (permissions.length == 0)
        {
            // canceled, finish
            denied = true;
        } else
        {
            for (int i = 0; i < permissions.length; i++)
            {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED)
                {
                    denied = true;
                }
            }
        }

        if (denied)
        {
            Toast.makeText(this, "At least one permission is missing.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private final DroneDiscoverer.Listener mDiscovererListener = new DroneDiscoverer.Listener()
    {
        @Override
        public void onDronesListUpdated(List<ARDiscoveryDeviceService> dronesList)
        {
            mDronesList.clear();
            mDronesList.addAll(dronesList);
            if (dronesList.size() > 0)
            {
                //we got just one drone so take it
                doConnect(dronesList.get(0));
            }
        }
    };
}
