package sendesign.btmirror;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.io.IOException;

public class WifiSetup extends AppCompatActivity {

    protected RecyclerView mRecyclerView;
    protected RecyclerView.Adapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    public static TextView wifiSelected;
    private String ssid;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifisetup);
        String[] wifiList = ConnectedThread.wifiList;
        String cleanSet = "";
        if (wifiList != null) { //if a list of networks has been recieved from the pi
            for (int i = 0; i < wifiList.length; i++) {    //loop through the list
                if (wifiList[i].length() > 2) {         //if the string is not empty or a space
                    cleanSet += wifiList[i];        //append it to cleanset
                    if(i != wifiList.length - 1){   //if wifilist[i] isnt the last
                        cleanSet += "\n";   //append a new line char
                    }
                }
            }
        }
        wifiList = cleanSet.split("\n");
        ConnectedThread.wifiList = wifiList;
        Button cancel = findViewById(R.id.wifiCancelbutton);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WifiSetup.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        wifiSelected = findViewById(R.id.currentWifiSelection);

        mRecyclerView = findViewById(R.id.wifiList);
        mLayoutManager = new LinearLayoutManager(WifiSetup.this);
        mAdapter = new MyAdapter(wifiList);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        final EditText psw = findViewById(R.id.password);
        psw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                psw.bringToFront();

            }
        });
        final Button config = findViewById(R.id.wifiConfButton);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = psw.getText().toString();
                ssid = ConnectedThread.ssid;
                String toSend = (ssid + "\n" + password);
                try {
                    MainActivity.mmOutStream.write(toSend.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(WifiSetup.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
