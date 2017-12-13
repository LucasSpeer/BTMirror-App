package sendesign.btmirror;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.lang.reflect.Array;
import java.net.URL;
import java.util.Objects;

/**
 * Created by Lucas on 12/9/17.
 * For use with LayoutConfig activity to handle creating of spinner on Item Selected Listeners
 */

public class UpdateSpinners implements Runnable {
    final Spinner[] spinArr = LayoutConfig.spinArr;
    private String moduleList[] = LayoutConfig.moduleList;
    private int currentLayout[] = LayoutConfig.currentLayout;
    private int modCount = LayoutConfig.modCnt;
    Context context = null;

    /*
    this function handles the selection of a new module from a spinner in the LayoutConfig Activity
     */
    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);        //Sends process to background

        final AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {    //Initialize listener for each spinner
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String chosenModule = parent.getSelectedItem().toString();
                    int chosenModInt;
                    int cnt = 0;
                    int newLayout[] = currentLayout;
                    int modUpdating = 0;
                    for(int i = 0; i < modCount; i++){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            if(Objects.equals(chosenModule, moduleList[i])){
                                chosenModInt = i;
                                newLayout[modUpdating] = chosenModInt;
                                for(int j = 0; j < modCount; j++){
                                    if(currentLayout[j] == chosenModInt && j != modUpdating){
                                        newLayout[j] = currentLayout[modUpdating];
                                    }
                                }

                            }
                        }
                    }

                    LayoutConfig.updateLayout(newLayout, view.getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }
}
