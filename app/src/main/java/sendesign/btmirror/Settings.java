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
        When the configure button is pushed the setting will be saved and if a bluetooth connection is established they will be sent
 */
public class Settings extends AppCompatActivity {
    private SharedPreferences prefs = null;   //create a shared preference for storing settings
    private SharedPreferences.Editor editor;
    String modules[];
    public String zipcode;
    public String options[] = {"zipCodeGiven", "UseC", "24Hour", "secondsShown"};   //Keys for Boolean options
    public Boolean defaults[] = {false, false, false, false};  //Default Settings in order with options[]
    public Boolean currentOptions[] = null;
    public Boolean savedOptions[] = null;
    private CheckBox boxArr[];
    private int optionCount = 4;   //For indexing through Shared Preferences
    private Resources resources = null;
    private int boxCnt = 3; //number of checkboxes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.default_settings);
        resources = getResources();

        prefs = this.getPreferences(Context.MODE_PRIVATE);  //retrieve default preference file for storing layout as key value pairs {(string) "L1", (int)1}
        editor = prefs.edit(); //open a SharedPreferences editor
        savedOptions = defaults; //initialize the
        for(int i = 0; i < optionCount; i++){
            savedOptions[i] = prefs.getBoolean(options[i], defaults[i]);
        }
        zipcode = prefs.getString("zipcode", "Enter a 5 Digit zipcode");

        modules = resources.getStringArray(R.array.modules); //get the array containing module name strings (WARNING: The order of the string array can not be changed, add new modules to the end)
        TextView weatherTitle = findViewById(R.id.weatherTitle);  //initialize all the titles of sections using strings from strings.xml
        weatherTitle.setText(modules[2]);
        TextView timeTitle = findViewById(R.id.timeTitle);
        timeTitle.setText(modules[3]);
        TextView title = findViewById(R.id.settingsText);
        title.setText(R.string.settingText);
        final TextView currentZip = findViewById(R.id.currentZip);
        currentZip.setText(zipcode);
        currentOptions = savedOptions; //initialize currentOptions[]

        Button back = findViewById(R.id.SettingsBack);  //initialize both buttons and set their listeners
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
                currentZip.setText(zipcode);  //Update the text in the zipcode textview after config sets the zipcode variable
            }
        });

        /*
           below this is the setup for the checkboxes, first initialize an array to hold their boolean values, then create a listener for each box.

           To add another checkbox first add it to the layout, add name to the end of options[] and give it a default setting in defaults[], increase boxCnt and option count,
           lastly add a chunk of code below (after the previously added checkboxes but before the for loop) that follows the same format
         */
        boxArr = new CheckBox[boxCnt];   //Create an array to store each checkBox

        CheckBox celsius = findViewById(R.id.useCelCheck);
        celsius.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {  //On state change
                currentOptions[1] = isChecked;      //if box is now checked set appropriate boolean
            }
        });
        boxArr[0] = celsius;

        CheckBox hourFormat = findViewById(R.id.hourFormat);
        hourFormat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentOptions[2] = isChecked;
            }
        });
        boxArr[1] = hourFormat;

        CheckBox showSeconds = findViewById(R.id.showSecondToggle);
        showSeconds.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentOptions[3] = isChecked;
            }
        });
        boxArr[2] = showSeconds;


        /*
        template for checkboxes:

        CheckBox name = findViewById(R.id.nameFromLayout);
        name.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                currentOptions[i] = isChecked;
            }
        });
        boxArr[j] = name;
         */
        for(int i = 0; i < boxCnt; i++){    //Iterates through current options to set the checkboxes (which default to unchecked) to their appropriate value
            if(currentOptions[i+1]){      //use +1 because zipCodeGiven has no checkbox
                boxArr[i].setChecked(true);
            }
            else{
                boxArr[i].setChecked(false);
            }
        }
        editor.apply();
    }

    @Override
    protected void onResume(){
        super.onResume();
        savedOptions = defaults;
        for(int i = 0; i < optionCount; i++){  //Gets the saved settings from the Shared pref
            savedOptions[i] = prefs.getBoolean(options[i], defaults[i]);
        }
        zipcode = prefs.getString("zipcode", "Zipcode");
        for(int i = 0; i < boxCnt; i++){    //Iterates through current options to set the checkboxes (which default to unchecked) to their appropriate value
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
        String ziptmp = zip.getText().toString();     //get text entered under zipcode prompt
        if(ziptmp.length() == 5){                      //if correct length
            zipcode = ziptmp;                         //set zipCode
            currentOptions[0] = true;              //set zipCodeGiven flag to true
        }
        else if (!currentOptions[0]){         //if no zipCode has been given
            zipcode = "Enter a 5 Digit zipcode";    //Set zipcode to default text
        }
        for(int i = 0; i < optionCount; i++){     //Store current options
            editor.putBoolean(options[i], currentOptions[i]);
            editor.apply();
        }
        if (currentOptions[0]){     //If zipcode has been given, store it
            editor.putString("zipcode", zipcode);
            editor.apply();
        }
        /*
        The string data contains the weather and general setting that come after the layout setting in config.json on the Rpi
        the format is laid out in strings.xml
         */
        String data = "\n  \"weather\":\n   {\n";
        data += ("    \"useC\": " + savedOptions[1].toString() + ",\n");
        data += ("    \"zipcode\": " + zipcode + "\n   },");
        data += ("\n  \"general\":\n   {\n");
        data += ("    \"military\": " + savedOptions[2].toString() + ",\n");
        data += ("    \"showSec\": " + savedOptions[3].toString());
        data += "\n   }\n}";
        MainActivity.settingsStr = data; //Save the settings in MainActivity's shared prefs
        String strToWrite = MainActivity.layoutStr + data;
        if(MainActivity.BTFound) {
            byte dataByte[] = strToWrite.getBytes();
            try {
                if(mmOutStream != null){  //Prevents a null pointer exception
                    mmOutStream.write(dataByte);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Toast.makeText(this, resources.getText(R.string.applied), Toast.LENGTH_SHORT).show(); //Feedback to user
        }
        else{
            Toast.makeText(this, resources.getText(R.string.savedLocallyStr), Toast.LENGTH_SHORT).show();
        }
    }
}
