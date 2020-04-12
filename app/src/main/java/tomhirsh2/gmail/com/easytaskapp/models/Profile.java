package tomhirsh2.gmail.com.easytaskapp.models;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import tomhirsh2.gmail.com.easytaskapp.R;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById (R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_profile);
    }
}
