package sendesign.btmirror;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import static sendesign.btmirror.MainActivity.mmOutStream;

/*
    Author: Lucas
    Settings Activity
        shared pref, checkboxes, and dynamic titles are all working
        bluetooth still needs to be added
 */
public class Settings extends AppCompatActivity {
    private SharedPreferences prefs = null;                                                         //create a shared preference for storing settings
    private SharedPreferences.Editor editor;
    String modules[];
    public String zipcode;
    public String options[] = {"zipCodeGiven", "UseC", "24Hour", "secondsShown"};                   //Keys for Boolean options
    public Boolean defaults[] = {false, false, false, false};                                       //Default Settings in order with options[]
    public Boolean currentOptions[] = null;
    public Boolean savedOptions[] = null;
    private CheckBox boxArr[];
    private int optionCount = 4;                                                                    //For indexing through Shared Preferences
    private Resources resources = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_settings);
        resources = getResources();
        prefs = this.getPreferences(Context.MODE_PRIVATE);                                          //retrieve default preference file for storing layout as key value pairs {(string) "L1", (int)1}
        editor = prefs.edit();
        savedOptions = defaults;
        for(int i = 0; i < optionCount; i++){
            savedOptions[i] = prefs.getBoolean(options[i], defaults[i]);
        }
        zipcode = prefs.getString("zipcode", "Enter a 5 Digit zipcode");
        modules = resources.getStringArray(R.array.modules);
        TextView weatherTitle = findViewById(R.id.weatherTitle);
        weatherTitle.setText(modules[2]);
        TextView timeTitle = findViewById(R.id.timeTitle);
        timeTitle.setText(modules[3]);
        TextView title = findViewById(R.id.settingsText);
        title.setText(R.string.settingText);
        final TextView currentZip = findViewById(R.id.currentZip);
        currentZip.setText(zipcode);
        currentOptions = savedOptions;
        Button back = findViewById(R.id.SettingsBack);
        back.setOnClickListener(new View.OnClickListener() {                                        //Back button to Return to main menu
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        Button config = findViewById(R.id.settingsConf);
        config.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                config();
                currentZip.setText(zipcode);                                                        //Update the text in the zipcode textview after config sets the zipcode variable
            }
        });

        boxArr = new CheckBox[3];                                                                   //Create an array to store each checkBox
        CheckBox celsius = findViewById(R.id.useCelCheck);
        celsius.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {            //On state change
                if(isChecked){                                                                      //if box is now checked
                    currentOptions[1] = true;                                                       //set appropriate boolean
                }
                else {
                    currentOptions[1] = false;
                }
            }
        });
        boxArr[0] = celsius;
        CheckBox hourFormat = findViewById(R.id.hourFormat);
        hourFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    currentOptions[2] = true;
                } else {
                    currentOptions[2] = false;
                }
            }
        });
        boxArr[1] = hourFormat;
        CheckBox showSeconds = findViewById(R.id.showSecondToggle);
        showSeconds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    currentOptions[3] = true;
                } else {
                    currentOptions[3] = false;
                }
            }
        });
        boxArr[2] = showSeconds;
        for(int i = 0; i < 3; i++){                                                                 //Iterates through current options to set the checkboxes (which default to unchecked) to their appropriate value
            if(currentOptions[i+1]){                                                                //use +1 because zipCodeGiven has no checkbox
                boxArr[i].setChecked(true);
            }
            else{
                boxArr[i].setChecked(false);
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        savedOptions = defaults;
        for(int i = 0; i < optionCount; i++){                                                       //Gets the saved settings from the Shared pref
            savedOptions[i] = prefs.getBoolean(options[i], defaults[i]);
        }
        zipcode = prefs.getString("zipcode", "Zipcode");
        for(int i = 0; i < 3; i++){                                                                 //Iterates through current options to set the checkboxes (which default to unchecked) to their appropriate value
            if(currentOptions[i+1]){
                boxArr[i].setChecked(true);
            }
            else{
                boxArr[i].setChecked(false);
            }
        }
    }
    private void config(){
        EditText zip = findViewById(R.id.zipcode);
        String ziptmp = zip.getText().toString();                                                   //get text entered under zipcode prompt
        if(ziptmp.length() == 5){                                                                   //if correct length
            zipcode = ziptmp;                                                                       //set zipCode
            currentOptions[0] = true;                                                               //set zipCodeGiven flag to true
        }
        else if (!currentOptions[0]){                                                               //if no zipCode has been given
            zipcode = "Enter a 5 Digit zipcode";                                                    //Set zipcode to default text
        }
        for(int i = 0; i < optionCount; i++){                                                       //Store current options
            editor.putBoolean(options[i], currentOptions[i]);
            editor.apply();
        }
        if (currentOptions[0]){                                                                     //If zipcode has been given, store it
            editor.putString("zipcode", zipcode);
            editor.apply();
        }
        String data = "{\n";
        data += ("\"zipcode\" :" + zipcode + "\n");
        for(int i = 0; i < optionCount; i++){
            data += ("\"" + options[i] + "\": " + savedOptions[i].toString());
            if(i != optionCount - 1){
                data += ",";
            }
            data += "\n";
        }
        data += "}";
        if(MainActivity.BTFound) {
            byte dataByte[] = data.getBytes();
            try {
                mmOutStream.write(dataByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, resources.getText(R.string.yesBT), Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, resources.getText(R.string.noBT), Toast.LENGTH_SHORT).show();
        }
    }
}
