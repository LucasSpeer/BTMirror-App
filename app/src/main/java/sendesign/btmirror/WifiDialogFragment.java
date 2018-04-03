package sendesign.btmirror;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.widget.EditText;

import java.io.IOException;

public class WifiDialogFragment extends DialogFragment {
    public String titleStr;
    public String SSID;
    public AlertDialog builder;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        titleStr = getTitleString(getContext());

        builder = new AlertDialog.Builder(getActivity())
                .setView(R.layout.wififragment_layout)
                .setTitle(titleStr)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User pressed okay
                        doOkayClick();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        doNegativeClick();
                    }
                })
                .create();
        return builder;
    }
    public String getTitleString(Context context) {
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (manager != null) { //Catch for wifi manager being null
            if (manager.isWifiEnabled()) {
                WifiInfo wifiInfo = manager.getConnectionInfo();
                if (wifiInfo != null) {
                    NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState());
                    if (state == NetworkInfo.DetailedState.CONNECTED || state == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
                        SSID = wifiInfo.getSSID();
                        return (R.string.wifiRequest + SSID);   // If a network is connected return "Enter a password for: $SSID"
                    }
                }
                return getString(R.string.wifiOff); //Wifi is off return "Please connect to your home WiFi network and retry"
            }
        }
        return null;
    }
    public void doOkayClick() {
        // get the entered password
        EditText psw = builder.findViewById(R.id.password);
        String pswInput = psw.getText().toString();
        String toSend = SSID + "\n" + pswInput;
        if (MainActivity.mmOutStream != null){
            try {
                MainActivity.mmOutStream.write(toSend.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void doNegativeClick() {
        // Do stuff here.
    }
}
