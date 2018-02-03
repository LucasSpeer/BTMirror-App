package sendesign.btmirror;
/*
    *Layout config activity
    *Functions:
    * 6 spinners w/ title that contain the list of available modules
    * 2 buttons, back(to main menu), and config(set chosen lay)
    * SharedPreference file to store key value pairs locally
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;


public class LayoutConfig extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private SharedPreferences prefs = null;                                                         //create a shared preference for storing layout
    private boolean isDefault = true;
    final private int defaultLayout[] = {1, 2, 3, 0, 0, 0};                                         //position in R.arrays.modules, 0 = none
    private int currentLayout[] = null;                                                             //Integer Array to hold current Layout
    private Spinner spinArr[] = null;                                                               //Lets us globally reference each spinner by its location (TL=0, TR=1, ML=2, MR=3,...)
    private String moduleList[] = null;                                                             //the integer held in the layout arrays correspond to the position in this array of strings containing the list of modules, populated from strings.xml
    final private String spots[] = {"l1", "r1", "l2", "r2", "l3", "r3"};                           //strings for use as keys with savedPreferences
    private String spotsFull[] = null;
    private int modCnt = 0;                                                                         //Number of modules, updated dynamically from Modules.xml
    private long ids[];
    private int firstRunCnt;                                                                        //onItemSelected is triggered the first time each spinner is set, this counter variable works with a loop to counteract that in the onItemSelectedListener
    private String currentText;
    private Resources resources = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_config);
        resources = getResources();
        modCnt = resources.getInteger(R.integer.modCount);
        firstRunCnt = 0;
        @SuppressWarnings("UnnecessaryLocalVariable") Spinner spinners[] =                                             //initialize spinners (ignore the redundancy warning)
                {findViewById(R.id.LS1), findViewById(R.id.RS1), findViewById(R.id.LS2)
                        , findViewById(R.id.RS2), findViewById(R.id.LS3), findViewById(R.id.RS3)};
        spinArr = spinners;                                                                         //Set global spinner array

        prefs = LayoutConfig.this.getPreferences(Context.MODE_PRIVATE);                             //retrieve default preference file for storing layout as key value pairs {(string) "L1", (int)1}
        moduleList = resources.getStringArray(R.array.modules);                                     //Populate global module string array moduleList with the values from strings.xml modules[] =  {None, Greeting, Weather, Time, News, Email}

        int savedLayout[] = defaultLayout;
        for (int i = 0; i < savedLayout.length; i++) {
            savedLayout[i] = prefs.getInt(spots[i], defaultLayout[i]);                              //create an array from saved key value pairs where key = spots[i] (a string) and defaultLayout[i] is the value returned when no key is found
        }
        currentLayout = savedLayout;                                                                //set the current layout to our newly retrieved layoutArray
        spotsFull = resources.getStringArray(R.array.spotNames);
        setCurrentText();

        final Button back = findViewById(R.id.LayoutBack);                                          //back and config buttons
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LayoutConfig.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        final Button config = findViewById(R.id.config);
        config.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Config();
            }
        });

        long tmpIDarr[] = new long[modCnt];                                                         //Temporary array to get the individual IDs of each spinner to be used in the onItemSelectedListener
        for (int i = 0; i < spinArr.length; i++) {
            spinArr[i].setOnItemSelectedListener(this);
            tmpIDarr[i] = spinArr[i].getId();
        }
        ids = tmpIDarr;
        setAllSpinners(currentLayout);

    }


    @Override
    protected void onResume() {
        super.onResume();
        firstRunCnt = 0;
        int savedLayout[] = defaultLayout;
        for (int i = 0; i < savedLayout.length; i++) {
            savedLayout[i] = prefs.getInt(spots[i], defaultLayout[i]);                              //create an array from saved key value pairs where key = spots[i] (a string) and defaultLayout[i] is the value returned when no key is found
        }
        currentLayout = savedLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {              //Function On spinner clicked and item Selected
        String chosenModule = parent.getSelectedItem().toString();                                  //get String of item selected
        int chosenModInt = 0;
        int newLayout[] = currentLayout;                                                            //Save Layout from before update and set new Layout equal to it(for size mostly)
        int modUpdatingInt = 0;                                                                     //Will be set to a value corresponding to which spinner was clicked (Left top = 0, Right top = 1, Left middle = 2,...)
        int oldMod = 0;
        if (firstRunCnt >= modCnt) {                                                                //if the spinners have been initialized
            for (int i = 0; i < modCnt; i++) {                                                      //sets chosen module integer correspond to the position of the selected item in moduleList
                if (Objects.equals(chosenModule, moduleList[i])) {                                                //Compares the string selected to the list of module names
                    chosenModInt = i;
                }
                if (ids[i] == parent.getId()) {                                                     //Compare the id of the Spinner the was selected with the array of spinner IDs to determine which spinner was chosen
                    modUpdatingInt = i;
                    oldMod = newLayout[i];
                }
            }
            newLayout[modUpdatingInt] = chosenModInt;                                               //Update the spot chosen in the temporary layout array
            for (int k = 0; k < modCnt; k++) {                                                      //Iterate through each spot of the layout(Before the update) and switch any spot that has the same module as the one just selected
                if (currentLayout[k] == chosenModInt && k != modUpdatingInt && chosenModule != moduleList[0]) {                                 //with the one previously in the spinner that was clicked (unless "none" was selected)
                    newLayout[k] = oldMod;
                    String newStringArr[] = getStringForSpinArr(k, newLayout);
                    ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(spinArr[k].getContext(), R.layout.support_simple_spinner_dropdown_item, newStringArr); //Create a new array adapter for the changed spot
                    spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
                    spinArr[k].setAdapter(spinnerArrayAdapter);
                    firstRunCnt = modCnt;                                                             //Not 100% sure this is necessary. Remove at own risk
                }
            }
            currentLayout = newLayout;

        }
        firstRunCnt++;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    /*
    Currently unused, resets current layout to the default. Maybe for a button or whatever.
     */
    private void setDefaultLayout() {                                                               //function to reset layout to defaults (currently unused - todo add a "reset to defaults" button)
        currentLayout = defaultLayout;
    }

    /*
    A function to set the text containing the SAVED text, called during onCreate() and when Config is clicked
     */
    private void setCurrentText() {
        TextView currentConf = findViewById(R.id.currentConfig);
        currentText = this.getString(R.string.currentConfig);
        int odd = 0;                                                                                //Index that always should = 0 or 1. Used to add spaces after the left side or a new line after the right side's text
        for (int i = 0; i < modCnt; i++) {
            currentText += (spotsFull[i] + moduleList[currentLayout[i]]);
            if (odd == 0) {
                currentText += "   ";
                odd++;
            } else {
                currentText += "\n";
                odd = 0;
            }
        }
        currentConf.setText(currentText);
    }

    private void setAllSpinners(int layout[]) {
        for (int i = 0; i < modCnt; i++) {
            setSpinner(spinArr[i], layout[i]);
        }
    }

    /*
        setSpinner(...) configures a single spinner(called spinner), chosenModule is the of chosen module IDs (0-6), spotChanging is an integer to represent the position changing (spots[spotChanging])
     */
    private void setSpinner(final Spinner spinner, final int chosenModule) {
        int sel = chosenModule;
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(spinner.getContext(), R.layout.support_simple_spinner_dropdown_item);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        for (int i = 0; i < modCnt; i++) {
            if (sel == modCnt) {
                sel = 0;
            }
            spinnerArrayAdapter.add(moduleList[sel]);
            sel++;
        }
        spinner.setAdapter(spinnerArrayAdapter);
    }

    /*
    The purpose of the following function is to create and assign a new array adapter for each spinner based on the selected spinner
    Called from the onItemSelectedListener
     */
    private String[] getStringForSpinArr(int spinChanging, int layout[]) {
        int mod = layout[spinChanging];
        String resultArr[] = new String[modCnt];
        for (int i = 0; i < modCnt; i++) {
            if (mod == modCnt) {
                mod = 0;
            }
            resultArr[i] = moduleList[mod];
            mod++;
        }
        return resultArr;
    }

    /*
    The Config() function is called from the onClickListener of the Configure button
    Sets the value for the keys defined in spots[], into the default preferences file for this activity see - https://developer.android.com/reference/android/content/SharedPreferences.html
    Need to add function to send the new layout to the mirror via the bluetooth connection
     */
    private void Config() {
        SharedPreferences.Editor editor = prefs.edit();
        for (int i = 0; i < modCnt; i++) {
            editor.putInt(spots[i], currentLayout[i]);
            editor.apply();
        }
        setCurrentText();                                                                           //Updates the containing the saved currentLayout
        /*
        The string data contains the layout settings and it's format is outlined in strings.xml
         */
        String data = "{\n  \"layout\":\n   {\n";
        for(int i = 0; i < currentLayout.length; i++){
            data += ("    \"" + spots[i] + "\": " + moduleList[currentLayout[i]]);
            if(i != currentLayout.length - 1){
                data += ",";
            }
            data += "\n";
        }
        data += "   },";
        MainActivity.layoutStr = data;
        String strToSend = data + MainActivity.settingsStr;
        if(MainActivity.BTFound) {
            byte dataByte[] = strToSend.getBytes();
            OutputStream mOutputStream = MainActivity.mmOutStream;
            try {
                mOutputStream.write(dataByte);
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
