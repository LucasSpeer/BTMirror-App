package sendesign.btmirror;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.voice.VoiceInteractionSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    public String MAC = "";
    //final public UUID uuid = UUID.fromString(getResources().getString(R.string.UUID));
    final public BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //get bluetooth adapter
    public BluetoothSocket mSocket = null;            //create a new socket
    public InputStream mmInStream = null;             //Initialize IO streams
    public OutputStream mmOutStream = null;
    public byte[] mmBuffer;                                  // mmBuffer store for the stream

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        From this block comment to the next checks and enable the bluetooth hardware
        and attempts to establish a connection with the smartmirror acting as the host
        and the phone as the client
         */
/*
        if (!mBluetoothAdapter.isEnabled()) {                                       //If bluetooth is not enadbled, enable it
            mBluetoothAdapter.enable();
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();  //check if already paired
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();                 // MAC address
                if (deviceName == "SmartMirror") {
                    MAC = deviceHardwareAddress;
                }
            }
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MAC);
        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the SmartMirror
            tmp = device.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mSocket = tmp;
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();

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

        InputStream tmpIn = null;               //get the input and output streams required for serial interfacing
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

        /*center
        end bluetooth connection setup
        below is the basic app functionality code, buttons and status text etc.
         */
        Button layout = (Button)findViewById(R.id.layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LayoutConfig.class);
                startActivity(intent);
            }
        });
        Button settings = (Button)findViewById(R.id.settingsButton);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

    }
    @Override
            protected void onResume(){
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {          //If bluetooth is not enadbled, enable it
            mBluetoothAdapter.enable();
        }

    }



    // Call this from the main activity to send data to the remote device.
    void write(byte[] bytes, OutputStream mmOutStream) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);
        }
    }
}