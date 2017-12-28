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
import android.os.ParcelUuid;
import android.service.voice.VoiceInteractionSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.ServerSocket;
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
    public static BluetoothServerSocket serverSocket;
    public BluetoothHandler BTHandler;
    private static Resources resources;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Resources res = getResources();
        resources = res;
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
                if(BTFound && BTHandler.isConnected()){
                    btStatus.setText(conStatusText[0] + conStatusText[2] + conStatusText[4] + MAC);
                }
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
        uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("SmartMirror", uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        findDevices(btStatus, conStatusText, retry, resources);
        BTHandler = new BluetoothHandler();
        if (BTHandler.isConnected()){
            btStatus.setText(conStatusText[0] + conStatusText[2] + conStatusText[4] + MAC);//"Connection Status: Successful"
        }
        mmOutStream = BluetoothHandler.mmOutStream;
        mmInStream = BluetoothHandler.mmInStream;
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
                    if (Objects.equals(deviceName, "SmartMirror")) {                             //Mirror found among paired devices
                        MAC = device.getAddress();
                        BTFound = true;
                        ParcelUuid uuidparcel[] = device.getUuids();
                        btStatus.setText(conStatusText[0] + conStatusText[5] + conStatusText[4] + MAC);//"Connection Status: Paired, Listening"
                        //retry.setVisibility(View.INVISIBLE);                                        //Make retry button and list of connected devices invisible
                        deviceList.setVisibility(View.INVISIBLE);
                        listTitle.setVisibility(View.INVISIBLE);

                    } else {
                        btStatus.setText(conStatusText[0] + conStatusText[3]);                      //"Connection Status: Not Paired"
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

    }
}