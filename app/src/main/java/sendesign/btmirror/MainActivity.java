package sendesign.btmirror;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
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
    public BluetoothHandler BTHandler;
    public BroadcastReceiver receiver;
    private Resources resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Resources res = getResources();
        resources = res;
        final TextView statusText = findViewById(R.id.conStatus);
        final String conStatusText[] = resources.getStringArray(R.array.ConStatText);               //from strings.xml conStatText[] = {"Connection Status :", "Attempting to Connect", "Successful", "Failed", "\nMac Address: "};
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

        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();                                  //get bluetooth adapter
        if (!mBluetoothAdapter.isEnabled()) {                                                       //If bluetooth is not enabled, enable it
            mBluetoothAdapter.enable();
        }

        uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");                             //UUID which must be the same as on the RaspPi
        final Button retry = findViewById(R.id.retryButton);
        findDevices(statusText, conStatusText, retry, resources);                                   //get bluetooth devices and check if paired
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {                                                           //Retry Button
                updateStatus(conStatusText, statusText);                                            //first update the status text
                if(!BTStatus.equals("connected")){
                    BTHandler = new BluetoothHandler(BTdevice);                                     //if BT isn't connected attempt to reinitialize the BT Handler
                    BTHandler.run();
                }
                updateStatus(conStatusText, statusText);                                            //and update the status Text again, This step and the identical line in this listener may be unnecessary thanks to the broadcast receiver
            }
        });
        updateStatus(conStatusText, statusText);
        receiver = new BroadcastReceiver() {                                      //This broadcast receiver listens for updates from BluetoothHandler and ConnectedThread to update the BT status text
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("update"))
                {
                    updateStatus(conStatusText, statusText);
                }
            }
        };
        IntentFilter filter = new IntentFilter();                                                   //The broadcast receiver needs a intent filter to be registered
        filter.addAction("update");
        registerReceiver(receiver, filter);
        BTHandler = new BluetoothHandler(BTdevice);
        BTHandler.run();
    }

    /*
        findDevices() first gets the list of devices paired, then checks if any is named SmartMirror.
        If one is found the status text is updated/hidden and the device is saved.
        If none is found a
     */
    @SuppressLint("SetTextI18n")
    private void findDevices(TextView btStatus, String conStatusText[], Button retry, Resources resources) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();                  //check if already paired
        TextView deviceList = findViewById(R.id.devList);
        TextView listTitle = findViewById(R.id.devListTitle);
        String devStr = "";
        int i = 0;
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
                        deviceList.setVisibility(View.INVISIBLE);
                        listTitle.setVisibility(View.INVISIBLE);

                    } else {
                        BTStatus = "notPaired";
                    }
                }
            }
            deviceList.setText(devStr);
        } else {
            deviceList.setText(R.string.devlisterror);       //error - no devices found
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {                                                       //If bluetooth is not enabled, enable it
            mBluetoothAdapter.enable();
        }
        final TextView btStatus = findViewById(R.id.conStatus);
        final String conStatusText[] = resources.getStringArray(R.array.ConStatText);
        updateStatus(conStatusText, btStatus);
    }

    private void updateStatus(String conStatusText[], TextView btStatus){
        String statusText;
        if(BTStatus.equals("paired")){
            statusText = conStatusText[0] + conStatusText[5] + conStatusText[4] + MAC;              //"Connection Status: Paired, Listening"
        }
        else if(BTStatus.equals("notPaired")){
            statusText = conStatusText[0] + conStatusText[3];
        }
        else if (BTStatus.equals("connected")){
            statusText = conStatusText[0] + conStatusText[2] + conStatusText[4] + MAC;
        }
        else{
            statusText = "typo";
        }
        btStatus.setText(statusText);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        if (BTStatus.equals("connected")) {
            BTHandler.cancel();
        }
    }
}