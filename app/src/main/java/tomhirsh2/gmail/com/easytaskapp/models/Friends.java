package tomhirsh2.gmail.com.easytaskapp.models;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.os.Bundle;

import tomhirsh2.gmail.com.easytaskapp.R;

public class Friends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById (R.id.toolbar_friends);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_friends);
    }
}
