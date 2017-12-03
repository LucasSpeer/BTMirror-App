package sendesign.btmirror;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Intent intent = getIntent();
        String settingTitle = intent.getStringExtra("modSelected");     //Receives the module selected from the ModuleList activity
        TextView title = findViewById(R.id.settingsText);
        title.setText(settingTitle);                                           //And sets the title of the activity to the selected module here
        Button back = findViewById(R.id.SettingsBack);
        back.setOnClickListener(new View.OnClickListener() {                   //Back button to Return to main menu
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
