package sendesign.btmirror;

import android.app.IntentService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import java.util.Objects;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Handler;

/**
 * Created by Lucas on 11/19/17.
 * handles the bluetooth connection process. Once connected it hands the Bluetooth Socket to ConnectedThread, which handles the data transfer
 */

public class BluetoothHandler extends Thread{

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int PERIOD = 5000;
    final private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private UUID uuid;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;
    private ConnectedThread BTthread;

    public BluetoothHandler(BluetoothDevice device) {
        BluetoothSocket tmp = null;                                                                 // Use a temporary object that is later assigned to mmSocket because mmSocket is final.
        mmDevice = device;
        uuid = MainActivity.uuid;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = mmDevice.createRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;

    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mBluetoothAdapter.cancelDiscovery();
        Intent intent = new Intent();
        intent.setAction("update");
        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        BTthread = new ConnectedThread(mmSocket);
        MainActivity.BTStatus = "connected";
        //BTthread.run();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
        BTthread.cancel();
    }
}
