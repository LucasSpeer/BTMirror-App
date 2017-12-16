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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Handler;

import static android.content.ContentValues.TAG;

/**
 * Created by Lucas on 11/19/17.
 * will eventually handle the data transmission, currently has no use
 */

public class BluetoothHandler extends Service{
    private static final String TAG = "MY_APP_DEBUG_TAG";
    private static final int PERIOD=5000;
    private View root=null;

    final private BluetoothAdapter mBluetoothAdapter = MainActivity.mBluetoothAdapter;
    private BluetoothSocket mSocket = MainActivity.mSocket;
    private InputStream mmInStream = MainActivity.mmInStream;
    private static OutputStream mmOutStream = MainActivity.mmOutStream;
    private byte[] mmBuffer; // mmBuffer store for the stream


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        run();
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
        mmBuffer = new byte[1024];
        int numBytes; // bytes returned from read()

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                numBytes = mmInStream.read(mmBuffer);
            } catch (IOException e) {
                Log.d(TAG, "Input stream was disconnected", e);
                break;
            }
        }
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
}