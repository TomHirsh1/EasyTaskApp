package tomhirsh2.gmail.com.easytaskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById (R.id.toolbar_about);
        setSupportActionBar(toolbar);

        //Element element = new Element();
        //element.setTitle(getResources().getString(R.string.ContactUs));

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.drawable.logo_with_slogan)
                .setDescription(getResources().getString(R.string.Description))
                .addItem(new Element().setTitle(getResources().getString(R.string.Version)))
                .addItem(new Element().setTitle(getResources().getString(R.string.ContactUs)))
                .create();

        setContentView(aboutPage);
    }
}