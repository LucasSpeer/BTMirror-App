package sendesign.btmirror;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

import static android.content.ContentValues.TAG;

/**
 * Created by Lucas on 11/19/17.
 * handles the data transmission
 */

public class BluetoothHandler extends Service{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int PERIOD=5000;
    private View root=null;

    final private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket mSocket;
    private InputStream mmInStream;
    private static OutputStream mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream
    private byte[] dataToWrite;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dataToWrite = intent.getByteArrayExtra("data");
        BTconnect(MainActivity.MAC);
        try {
            mmOutStream = mSocket.getOutputStream();
        } catch (IOException e){
            e.printStackTrace();
        }
        if(mSocket.isConnected()){
            run();
        }

        return (START_NOT_STICKY);
    }
    @Override
    public void onDestroy() {

    }
    @Override
    public IBinder onBind(Intent intent) {
        return(null);
    }


    public void run() {
        write(dataToWrite);
    }

    // Call this from the main activity to send data to the remote device.
    public static void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when sending data", e);

            Bundle bundle = new Bundle();
            bundle.putString("toast",
                    "Couldn't send data to the other device");
        }
    }

    // Call this method from the main activity to shut down the connection.
    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Could not close the connect socket", e);
        }
    }
    private void BTconnect(String MAC) {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MAC);
        BluetoothSocket tmp = null;
        try {
            tmp = device.createRfcommSocketToServiceRecord(MainActivity.uuid);                                   // Get a BluetoothSocket to connect with the SmartMirror
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
}