package sendesign.btmirror;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Lucas on 11/19/17.
 * handles the bluetooth connection process. Once connected it hands the Bluetooth Socket to ConnectedThread, which handles the data transfer
 */

public class BluetoothHandler extends Thread{

    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int PERIOD = 5000;
    final private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private static BluetoothSocket mmSocket;
    private static BluetoothDevice BTdevice = null;
    private ConnectedThread BTthread;
    private Backgrounder task = null;

    public BluetoothHandler(BluetoothDevice device) {
        task = new Backgrounder();
        task.execute();
        BTdevice = device;
    }

    public void run() {
        mBluetoothAdapter.cancelDiscovery();                                                        //Cancel discovery because it otherwise slows down the connection.
        Intent intent = new Intent();
        intent.setAction("update");
        try {
            if(mmSocket != null){
                mmSocket.connect();                                                                 //Connect to the remote device through the socket. This call blocks until it succeeds or throws an exception

            }
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
        BTthread.start();                                                                           //TODO: modify ConnectedThread.run() to no hang and to handle incoming messages from the SmartMirror
    }

    public void cancel() {
        try {
            mmSocket.close();                                                                       // Closes the client socket and causes the thread to finish.
        } catch (IOException e) {
            Log.e(TAG, "Could not close the client socket", e);
        }
        BTthread.cancel();
        if (task != null) {
            task.cancel(false);
        }
    }
    static class Backgrounder extends AsyncTask {
        /*
            this function creates a background task for the bluetooth connection to keep the UI responsive to activate call:
                task = new bluetoothTask();
                task.execute();

            make sure to
             */

        @Override
        protected Object doInBackground(Object[] objects) {
            BluetoothSocket tmp = null;                                                                 // Use a temporary object that is later assigned to mmSocket because mmSocket is final.
            UUID uuid = MainActivity.uuid;
            try {
                tmp = BTdevice.createRfcommSocketToServiceRecord(uuid);                                   //Get a BluetoothSocket to connect with the given BluetoothDevice. uuid must match rfcomm-server.py on the Rpi
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;

            return null;
        }
    }
}
