package sendesign.btmirror;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.autofill.AutofillValue;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

public class WifiDialogFragment extends DialogFragment {
    public String titleStr;
    public String SSID;
    public AlertDialog builder;
    private EditText psw;
    private RecyclerView wifiButtons;
    public static final Handler mhandler = MainActivity.handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.wififragment_layout, container, false);
        psw = v.findViewById(R.id.password);
        wifiButtons = v.findViewById(R.id.wifiList);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getContext());
        wifiButtons.setLayoutManager(manager); //create a layout manager and apply it to the recycler(list)View

        final String wifiList[] = ConnectedThread.wifiList;

        final RecyclerView.Adapter adapter = new RecyclerView.Adapter() {
            public TextView tv;
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                return null;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return wifiList.length;
            }
        };
        wifiButtons.setAdapter(adapter);

        return v;
    }
    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.wifiRequest)
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        MainActivity.wifiSSID = ConnectedThread.wifiList[position];
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                })
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

    public void doOkayClick() {
        // get the entered password
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

