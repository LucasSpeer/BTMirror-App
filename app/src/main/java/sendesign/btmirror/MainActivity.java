package sendesign.btmirror;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
public class MainActivity extends AppCompatActivity {

    public static String MAC;
    public static UUID uuid = null;
    @SuppressWarnings("WeakerAccess")
    public static BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice BTdevice;
    public static InputStream mmInStream = null;                                                    //Initialize IO streams
    public static OutputStream mmOutStream = null;
    public static Boolean BTFound = false;
    public static String BTStatus;
    public static String layoutStr = "";
    public static String settingsStr = "";
    private SharedPreferences prefs = null;                                                         //create a shared preference for storing settings
    private SharedPreferences.Editor editor;
    public BluetoothHandler BTHandler;
    public BroadcastReceiver receiver;
    private Resources resources;
    private String conStatusText[];               //from strings.xml conStatText[] = {"Connection Status :", "Attempting to Connect", "Successful", "Failed", "\nMac Address: "};
    private TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Resources res = getResources();
        resources = res;
        prefs = this.getPreferences(Context.MODE_PRIVATE);                                          //retrieve default preference file for storing layout as key value pairs {(string) "L1", (int)1}
        editor = prefs.edit();
        conStatusText = resources.getStringArray(R.array.ConStatText);
        statusText = findViewById(R.id.conStatus);
        layoutStr = prefs.getString("layoutStr", res.getString(R.string.defLayout));
        settingsStr = prefs.getString("settingsStr", res.getString(R.string.defSettings));
        final Button layout = findViewById(R.id.layout);                                            //Layout config button
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LayoutConfig.class);
                startActivity(intent);
            }
        });
        Button settings = findViewById(R.id.settingsButton);                                        //Configure Modules Button
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();                                  //get bluetooth adapter
        if (!mBluetoothAdapter.isEnabled()) {                                                       //If bluetooth is not enabled, enable it
            mBluetoothAdapter.enable();
        }
        findDevices();
        uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");                             //UUID which must be the same as on the RaspPi
        final Button retry = findViewById(R.id.retryButton);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                                            //Retry Button
                updateStatus();                                            //first update the status text
                if (!BTStatus.equals("connected")) {                                                  //create the Handler and and run it
                    BTHandler = new BluetoothHandler(BTdevice);
                    BTHandler.run();
                }
                if (!BTStatus.equals("connected")) {
                    Toast.makeText(getApplicationContext(), R.string.connFailed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.connSucceeded, Toast.LENGTH_SHORT).show();
                }
                updateStatus();                                            //and update the status Text again, This step and the identical line in this listener may be unnecessary thanks to the broadcast receiver
            }
        });
        updateStatus();
        if (!BTStatus.equals("notPaired") && !BTStatus.equals("connected") && BTStatus != null) {
            BTHandler = new BluetoothHandler(BTdevice);
            BTHandler.run();                                                                        //and attempt to connect
        }
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @SuppressLint("SetTextI18n")
    private void findDevices() {
        /*
        findDevices() first gets the list of devices paired, then checks if any is named SmartMirror.
        If one is found the status text is updated/hidden and the device is saved.
        If none is found a
     */
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();                  //check if already paired
        TextView deviceList = findViewById(R.id.devList);
        TextView listTitle = findViewById(R.id.devListTitle);
        String devStr = "";
        int i = 0;
        if(BTFound == false) {
            if (pairedDevices.size() > 0) {                                                             // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    devStr += device.getName() + " - " + device.getAddress() + "\n";
                    i++;
                    String deviceName = device.getName();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (Objects.equals(deviceName, "SmartMirror")) {                             //Mirror found among paired devices
                            MAC = device.getAddress();
                            BTStatus = "paired";
                            BTdevice = device;
                            deviceList.setVisibility(View.INVISIBLE);                                   //If a SmartMirror is found among the paired devices hide the list of paired devices
                            listTitle.setVisibility(View.INVISIBLE);

                        } else {
                            BTStatus = "notPaired";
                        }
                    }
                }
                deviceList.setText(devStr);
            } else {
                deviceList.setText(R.string.devlisterror);                                              //error - no devices found
            }
        }
        else{
            deviceList.setVisibility(View.INVISIBLE);                                   //If a SmartMirror is found among the paired devices hide the list of paired devices
            listTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void updateStatus() {
        String statText;
        if (BTStatus.equals("paired")) {
            statText = conStatusText[0] + conStatusText[5] + conStatusText[4] + MAC;              //"Connection Status: Paired, Listening"
        } else if (BTStatus.equals("notPaired")) {
            statText = conStatusText[0] + conStatusText[3];
        } else if (BTStatus.equals("connected")) {
            statText = conStatusText[0] + conStatusText[2] + conStatusText[4] + MAC;
        } else {
            statText = "typo";
        }
        statusText.setText(statText);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.putString("layoutStr", layoutStr);
        editor.putString("settingsStr", settingsStr);
        editor.apply();

    }
}