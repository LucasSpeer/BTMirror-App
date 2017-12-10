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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.lang.reflect.Method;

public class LayoutConfig extends AppCompatActivity {
    private SharedPreferences prefs = null;                                                         //create a shared preference for storing layout
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }
    final Context context = getApplicationContext();
    private boolean isDefault = true;
    final private int defaultLayout[] = {1, 2, 3, 0, 0, 0};                                         //position in R.arrays.modules, 0 = none
    public static int currentLayout[] = null;
    public static Spinner spinArr[] = null;
    public static String moduleList[] = null;
    final private String spots[] = {"l1", "r1", "l2", "r2", "l3", "r3" };
    public static int modCount = 0;                                                                       //Number of modules

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout_config);
        Resources resources = getResources();
        modCount = R.integer.modCount;

        Spinner spinners[] = {findViewById(R.id.LS1), findViewById(R.id.RS1), findViewById(R.id.LS2)//initialize spinners (ignore the redundancy warning)
                , findViewById(R.id.RS2), findViewById(R.id.LS3), findViewById(R.id.RS3)};
        spinArr = spinners;                                                                         //Set global spinner array

        prefs = LayoutConfig.this.getPreferences(Context.MODE_PRIVATE);                             //retrieve default preference file for storing layout as key value pairs {(string) "L1", (int)1}
        moduleList = resources.getStringArray(R.array.modules);                                     //Populate global module string array moduleList with the values from strings.xml modules[] =  {None, Greeting, Weather, Time, News, Email}

        int savedLayout[] = {0, 0, 0, 0, 0, 0};
        for (int i = 0; i < modCount; i++) {
            savedLayout[i] = prefs.getInt(spots[i], defaultLayout[i]);                              //create an array from saved key value pairs where key = spots[i] (a string) and defaultLayout[i] is the value returned when no key is found
        }
        currentLayout = savedLayout;                                                                //set the current layout to our newly retrieved layoutArray



        Button back = findViewById(R.id.LayoutBack);                                                //back and config buttons
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
    }

    private void setDefaultLayout() {                                                               //function to reset layout to defaults (currently unused - todo add a "reset to defaults" button)
        currentLayout = defaultLayout;
    }

    private static void setAllSpinners(int layout[]) {
        for (int i = 0; i < modCount; i++) {
            setSpinner(spinArr[i], layout[i], i);
        }
    }
    /*
        setSpinner(...) configures a single spinner(called spinner), chosenModule is the of chosen module IDs (0-6), spotChanging is an integer to represent the position changing (spots[spotChanging])
     */
    private static void setSpinner(final Spinner spinner, final int chosenModule, final int spotChanging) {
        int sel = chosenModule;
        ArrayAdapter<CharSequence> spinnerArrayAdapter = new <CharSequence>ArrayAdapter(spinner.getContext(), R.layout.support_simple_spinner_dropdown_item);
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        for (int i = 0; i < modCount; i++) {
            if (sel == modCount) {
                sel = 0;
            }
            spinnerArrayAdapter.add(moduleList[sel]);
            Intent spinnerHandler = new Intent(spinner.getContext(), UpdateSpinners.class);
            spinnerHandler.putExtra("name", currentLayout[i]);
            sel++;
        }
        spinner.setAdapter(spinnerArrayAdapter);
    }
    public static Method updateLayout(int[] layout){
        for(int i =0; i < modCount; i++){
            updateLayoutSpot(layout[i], i);
        }
        return null;
    }
    public static void updateLayoutSpot(int changeTo, int modToChange){
        int tmp[] = currentLayout;                                                                  //temp variable to hold previously held module layout
        if(currentLayout[modToChange] != changeTo){                                                 //if clicked value is different from previously held value
            currentLayout[modToChange] = changeTo;                                                  //set currentLayout of the clicked module to the selected value
        }
        for(int i = 0; i < modCount; i++){                                                                 //Iterate through old preferences and change any matching spot to whatever was in the changed spot before the change
            if(tmp[i] == changeTo && i != modToChange && changeTo != 0){                            //unless the module selected is "None" or we are looking at the spot that was just updated
                currentLayout[i] = tmp[modToChange];

            }
        }
        setAllSpinners(currentLayout);
    }
    private void Config(){
        SharedPreferences.Editor editor = prefs.edit();
        for(int i = 0; i < modCount; i++){
            editor.putInt(spots[i], currentLayout[i]);
            editor.apply();
        }
    }
    private void notifyAllSpiners(){
        int sel = 0;
        for (int i = 0; i < modCount; i++) {
            ArrayAdapter<CharSequence> spinnerArrayAdapter = new <CharSequence>ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item);
            spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
            sel = currentLayout[i];
            for (int j = 0; j < modCount; j++) {
                if (sel == modCount) {
                    sel = 0;
                }
                spinnerArrayAdapter.add(moduleList[j]);
                sel++;
            }
            spinArr[i].setAdapter(spinnerArrayAdapter);
        }
    }
}
