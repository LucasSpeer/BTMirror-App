package sendesign.btmirror;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.View;

/**
 * Created by pook on 12/2/17.
 */

public class ModuleList extends ListActivity{
    String[] moduleList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_list);
        Resources resources = getResources();
        moduleList = resources.getStringArray(R.array.modules);
        setListAdapter(new ArrayAdapter<String>(ModuleList.this, android.R.layout.simple_list_item_1, moduleList));
    }
    @Override
    public void onListItemClick(ListView list, View v, int position, long id){
        super.onListItemClick(list, v, position, id);
        //Toast.makeText(ModuleList.this, moduleList[position],Toast.LENGTH_SHORT).show();
        String selected = moduleList[position];
        Intent intent = new Intent(ModuleList.this, Settings.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("modSelected", selected);
        startActivity(intent);
    }
}
