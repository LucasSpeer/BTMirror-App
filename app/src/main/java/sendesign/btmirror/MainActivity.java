/*
  * Auth: Lucas Speer
  * repository: github.com/lucasspeer/BTMirror-App
  * for use with github.com/lucasspeer/SmartMirror as part of our (Lucas Speer & Isaac Matzke's) senior design project
  * The main menu and initial activity for the BTSM (BlueTooth SmartMirror) app
  * This app acts as a settings menu and configuration for the SmartMirror running on a raspberry pi
  * This activity attemps to trigger a backgrounded RFCOMM serial connection over bluetooth
  * Initial commit date: 11/19/17
 */

package sendesign.btmirror;

import android.annotation.SuppressLint;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
    public static InputStream mmInStream = null;     //Initialize IO streams
    public static OutputStream mmOutStream = null;
    public static Boolean BTFound = false;
    public static String BTStatus;
    public static String layoutStr = "";
    public static String settingsStr = "";
    private SharedPreferences prefs = null;      //create a shared preference for storing settings
    private SharedPreferences.Editor editor;
    public BluetoothHandler BTHandler;
    public BroadcastReceiver receiver;
    private Resources resources;
    private static String conStatusText[];               //from strings.xml conStatText[] = {"Connection Status :", "Attempting to Connect", "Successful", "Failed", "\nMac Address: "};
    private TextView statusText;
    public static final Handler handler = new Handler();
    public static String wifiSSID;
    public static String wifiKey;
    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Resources res = getResources();
        resources = res;
        prefs = this.getPreferences(Context.MODE_PRIVATE);    //retrieve default preference file for storing layout as key value pairs {(string) "L1", (int)1}
        editor = prefs.edit();
        conStatusText = resources.getStringArray(R.array.ConStatText);
        statusText = findViewById(R.id.conStatus);
        fragmentManager = getFragmentManager();
        layoutStr = prefs.getString("layoutStr", res.getString(R.string.defLayout));
        settingsStr = prefs.getString("settingsStr", res.getString(R.string.defSettings));
        BTStatus = "notPaired";
        final Button layout = findViewById(R.id.layout);     //Layout config button
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LayoutConfig.class);
                startActivity(intent);
            }
        });
        Button settings = findViewById(R.id.settingsButton);    //Configure Modules Button
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });
        TextView deviceList = findViewById(R.id.devList);
        TextView listTitle = findViewById(R.id.devListTitle);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();      //get bluetooth adapter
        if (!mBluetoothAdapter.isEnabled()) {        //If bluetooth is not enabled, enable it
            mBluetoothAdapter.enable();
        }
        if (BTStatus.equals("notPaired")) {      //if no SmartMirror has been found already,
            findDevices();
        } else {
            deviceList.setVisibility(View.INVISIBLE);      //If a SmartMirror is paired, hide the list of paired devices
            listTitle.setVisibility(View.INVISIBLE);
        }
        uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");      //UUID which must be the same as on the RaspPi
        final Button retry = findViewById(R.id.retryButton);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {           //Retry Button
                findDevices();
                if (!BTStatus.equals("connected")) {    //create the Handler and and run it
                    BTHandler = new BluetoothHandler(BTdevice);
                    BTHandler.run();
                }
                if (!BTStatus.equals("connected")) {
                    Toast.makeText(getApplicationContext(), R.string.connFailed, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.connSucceeded, Toast.LENGTH_SHORT).show();
                }
                updateStatus();      //and update the status Text
            }
        });
        if (!BTStatus.equals("notPaired") && !BTStatus.equals("connected")) {
            BTHandler = new BluetoothHandler(BTdevice);
            BTHandler.run();      //and attempt to connect
        }
        editor.apply();
        updateStatus();
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
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();   //check if already paired
        TextView deviceList = findViewById(R.id.devList);
        TextView listTitle = findViewById(R.id.devListTitle);
        String devStr = "";
        int i = 0;
        if(!BTFound) {
            if (pairedDevices.size() > 0) {       // There are paired devices. Get the name and address of each paired device.
                for (BluetoothDevice device : pairedDevices) {
                    devStr += device.getName() + " - " + device.getAddress() + "\n";
                    i++;
                    String deviceName = device.getName();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        if (Objects.equals(deviceName, "SmartMirror")) {        //Mirror found among paired devices
                            MAC = device.getAddress();
                            BTStatus = "paired";
                            BTdevice = device;
                            deviceList.setVisibility(View.INVISIBLE);    //If a SmartMirror is found among the paired devices hide the list of paired devices
                            listTitle.setVisibility(View.INVISIBLE);

                        } else {
                            BTStatus = "notPaired";
                        }
                    }
                }
                deviceList.setText(devStr);
            } else {
                deviceList.setText(R.string.devlisterror);  //error - no devices found
            }
        }
        else{
            deviceList.setVisibility(View.INVISIBLE);        //If a SmartMirror is found among the paired devices hide the list of paired devices
            listTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void updateStatus() {
        String statText;
        switch (BTStatus) {
            case "paired":
                statText = conStatusText[0] + conStatusText[5];        //"Connection Status: Paired, Listening"
                break;
            case "notPaired":
                statText = conStatusText[0] + conStatusText[3];
                break;
            case "connected":
                statText = conStatusText[0] + conStatusText[2];
                break;
            default:
                statText = "typo";
                break;
        }
        statusText.setText(statText);
    }

    public static void showDialog(){
        DialogFragment wifiFragment = new WifiDialogFragment();

        wifiFragment.show(fragmentManager, "dialog");
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        editor.putString("layoutStr", layoutStr);
        editor.putString("settingsStr", settingsStr);
        editor.putString("BTStatus", BTStatus);
        editor.apply();   //When mainActivity is destroyed, save current settings

    }
}