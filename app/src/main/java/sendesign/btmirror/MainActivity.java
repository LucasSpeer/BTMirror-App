package sendesign.btmirror;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.service.voice.VoiceInteractionSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    public static String MAC;
    public static UUID uuid = null;
    @SuppressWarnings("WeakerAccess")
    final public static BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();         //get bluetooth adapter
    public static BluetoothSocket mSocket = null;                                                         //create a new socket
    public static InputStream mmInStream = null;                                                          //Initialize IO streams
    public static OutputStream mmOutStream = null;
    public static Boolean BTFound = false;
    public byte[] mmBuffer;                                                                         // mmBuffer store for the stream
    public boolean googleConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Resources resources = getResources();
        final TextView btStatus = findViewById(R.id.conStatus);
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
        final Button retry = findViewById(R.id.retryButton);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDevices(btStatus, conStatusText, retry, resources);
            }
        });

        /*
        From this block comment to the next checks and enable the bluetooth hardware
        and attempts to establish a connection with the smart mirror acting as the host
        and the phone as the client
         */

        if (!mBluetoothAdapter.isEnabled()) {                                                       //If bluetooth is not enabled, enable it
            mBluetoothAdapter.enable();
        }
        String uuidStr = resources.getString(R.string.UUID);
        uuid = UUID.fromString(uuidStr);
        findDevices(btStatus, conStatusText, retry, resources);


    }

    @SuppressLint("SetTextI18n")
    private void findDevices(TextView btStatus, String conStatusText[], Button retry, Resources resources) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();                  //check if already paired
        TextView deviceList = findViewById(R.id.devList);
        TextView listTitle = findViewById(R.id.devListTitle);
        int size = pairedDevices.size();
        String devStr = "";
        int i = 0;
        if (pairedDevices.size() > 0) {                                                             // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                devStr += device.getName() + " - " + device.getAddress() + "\n";
                i++;
                String deviceName = device.getName();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (Objects.equals(deviceName, "SmartMirror")) {                                                  //Mirror found among paired devices
                        MAC = device.getAddress();
                        btStatus.setText(conStatusText[0] + conStatusText[2] + conStatusText[4] + MAC); //"Connection Status: Successful"
                        //"MAC Address: 'MA:CA:DD:RE:SS:HE:RE"
                        BTconnect(MAC);
                        if(!mSocket.isConnected()) {
                            btStatus.setText(conStatusText[0] + conStatusText[5] + conStatusText[4] + MAC);//"Connection Status: Paired, not Connected"
                            BTFound = false;
                        }
                        else {
                            retry.setVisibility(View.INVISIBLE);                                        //Make retry button and list of connected devices invisible
                            BTFound = true;
                        }
                        deviceList.setVisibility(View.INVISIBLE);
                        listTitle.setVisibility(View.INVISIBLE);

                    } else {
                        btStatus.setText(conStatusText[0] + conStatusText[3]);                      //"Connection Status: Failed"
                    }
                }
            }
            deviceList.setText(devStr);
        } else {
            deviceList.setText(R.string.devlisterror);       //error - no devices found
        }
    }
    private void BTconnect(String MAC) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MAC);
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);                                   // Get a BluetoothSocket to connect with the SmartMirror
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mSocket = tmp;
        mBluetoothAdapter.cancelDiscovery();                                                        // Cancel discovery because it otherwise slows down the connection
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }

        InputStream tmpIn = null;                                                                   //get the input and output streams required for serial interfacing
        OutputStream tmpOut = null;
        try {
            tmpIn = mSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {                                                       //If bluetooth is not enabled, enable it
            mBluetoothAdapter.enable();
        }

    }
}