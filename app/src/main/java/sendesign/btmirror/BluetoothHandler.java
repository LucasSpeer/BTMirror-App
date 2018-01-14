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
    private final BluetoothSocket mmSocket;
    private ConnectedThread BTthread;

    public BluetoothHandler(BluetoothDevice device) {
        BluetoothSocket tmp = null;                                                                 // Use a temporary object that is later assigned to mmSocket because mmSocket is final.
        UUID uuid = MainActivity.uuid;
        try {
            tmp = device.createRfcommSocketToServiceRecord(uuid);                                   //Get a BluetoothSocket to connect with the given BluetoothDevice. uuid must match rfcomm-server.py on the Rpi
        } catch (IOException e) {
            Log.e(TAG, "Socket's create() method failed", e);
        }
        mmSocket = tmp;

    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();                                                        //Cancel discovery because it otherwise slows down the connection.
        Intent intent = new Intent();
        intent.setAction("update");
        try {
            mmSocket.connect();                                                                     //Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception
        } catch (IOException connectException) {
            try {
                mmSocket.close();                                                                   //Unable to connect; close the socket and return.
            } catch (IOException closeException) {
                Log.e(TAG, "Could not close the client socket", closeException);
            }
            return;
        }
        BTthread = new ConnectedThread(mmSocket);                                                   //Create a new thread to handle the connection.
        MainActivity.BTStatus = "connected";                                                        //Update the status for the main menu text
        //BTthread.run();                                                                           //TODO: modify ConnectedThread.run() to no hang and to handle incoming messages from the SmartMirror
    }

    public void cancel() {
        try {
            mmSocket.close();                                                                       // Closes the client socket and causes the thread to finish.
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
        BTthread.cancel();
    }
}
