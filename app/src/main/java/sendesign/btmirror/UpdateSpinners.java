package sendesign.btmirror;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.lang.reflect.Array;
import java.net.URL;

/**
 * Created by pook on 12/9/17.
 */

public class UpdateSpinners extends IntentService {
    final Spinner[] spinArr = LayoutConfig.spinArr;
    private String moduleList[] = LayoutConfig.moduleList;
    private int currentLayout[] = LayoutConfig.currentLayout;
    private int modCount = LayoutConfig.modCount;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public UpdateSpinners(String name) {
        super(name);
    }
    @Override
    protected void onHandleIntent(@Nullable final Intent intent) {
        final AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {    //Initialize listener for each spinner
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                try {
                    String chosenModule = parent.getSelectedItem().toString();
                    int chosenModInt = 0;
                    int cnt = 0;
                    int newLayout[] = currentLayout;
                    int modUpdating = 0;
                    intent.getIntExtra("name", modUpdating);
                    for(int i = 0; i < modCount; i++){
                        if(chosenModule == moduleList[i]){
                            chosenModInt = i;
                            newLayout[modUpdating] = chosenModInt;
                            for(int j = 0; j < modCount; j++){
                                if(currentLayout[j] == chosenModInt && j != modUpdating){
                                    newLayout[j] = currentLayout[modUpdating];
                                }
                            }

                        }
                    }

                    LayoutConfig.updateLayout(newLayout);
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
