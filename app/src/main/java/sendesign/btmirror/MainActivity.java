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
import android.util.Log;
import android.widget.Adapter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String MAC = "";
        UUID uuid = UUID.fromString(getResources().getString(R.string.UUID));
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); //get bluetooth adapter
        final BluetoothSocket mSocket;
        if (!mBluetoothAdapter.isEnabled()) {          //If bluetooth is not enadbled, enable it
            mBluetoothAdapter.enable();
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();  //check if already paired
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
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
    }
}

