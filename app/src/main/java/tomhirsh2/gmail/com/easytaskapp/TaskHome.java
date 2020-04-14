package tomhirsh2.gmail.com.easytaskapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import tomhirsh2.gmail.com.easytaskapp.models.Friends;
import tomhirsh2.gmail.com.easytaskapp.models.Profile;
import tomhirsh2.gmail.com.easytaskapp.services.LocationJobService;


public class TaskHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Activity activity;
    TaskDBHelper mydb;
    NoScrollListView taskListOverDue, taskListToday, taskListTomorrow, taskListUpcoming;
    NestedScrollView scrollView;
    ProgressBar loader;
    TextView overDueText, todayText, tomorrowText, upcomingText;
    ArrayList<HashMap<String, String>> overDueList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> todayList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> tomorrowList = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> upcomingList = new ArrayList<HashMap<String, String>>();

    private static final int TIME_INTERVAL = 2000; // # milliseconds, desired time passed between two back presses.
    private long mBackPressed;

    public static String KEY_ID = "id";
    public static String KEY_TASK = "task";
    public static String KEY_DATE = "date";
    public static String KEY_TIME = "time";
    public static String KEY_PRIORITY = "priority";
    public static String KEY_LOCATION = "location";

    private static final String TAG = "TaskHome";

    static TaskHome instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    public static TaskHome getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);

        setupNavDrawer();
        final DrawerLayout drawer = findViewById(R.id.drawer_layout);

        findViewById(R.id.imageMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });
        //setContentView(R.layout.task_home);

        instance = this;

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(TaskHome.this, getResources().getString(R.string.AcceptThisToTrackYourLocation), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

        activity = TaskHome.this;
        mydb = new TaskDBHelper(activity);
        scrollView = (NestedScrollView) findViewById(R.id.scrollView);
        loader = (ProgressBar) findViewById(R.id.loader);
        taskListOverDue = (NoScrollListView) findViewById(R.id.taskListOverDue);
        taskListToday = (NoScrollListView) findViewById(R.id.taskListToday);
        taskListTomorrow = (NoScrollListView) findViewById(R.id.taskListTomorrow);
        taskListUpcoming = (NoScrollListView) findViewById(R.id.taskListUpcoming);

        overDueText = (TextView) findViewById(R.id.overDueText);
        todayText = (TextView) findViewById(R.id.todayText);
        tomorrowText = (TextView) findViewById(R.id.tomorrowText);
        upcomingText = (TextView) findViewById(R.id.upcomingText);

        Toolbar toolbar = findViewById (R.id.toolbar_task);
        setSupportActionBar(toolbar);

        // Welcome message right after everything is load up
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.Welcome), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
            return;
        } else if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
            //logOutAlertMessage();
        } else {
            Toast.makeText(getBaseContext(), getResources().getString(R.string.ExitBackButton), Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    private void setupNavDrawer() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drawerPerson:
                startActivity(new Intent(this, Profile.class));
                return true;
            case R.id.drawerFriends:
                startActivity(new Intent(this, Friends.class));
                return true;
            case R.id.drawerSetting:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.drawerLanguage:
                showChangeLanguageDialog();
                return true;
            case R.id.drawerAbout:
                startActivity(new Intent(this, About.class));
                return true;
            case R.id.drawerLogout:
                logOutAlertMessage();
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void logOutAlertMessage() {
        androidx.appcompat.app.AlertDialog.Builder alertDlg = new androidx.appcompat.app.AlertDialog.Builder(this);
        alertDlg.setMessage(getResources().getString(R.string.LogoutConfirm));
        alertDlg.setCancelable(false);
        alertDlg.setPositiveButton(getResources().getString(R.string.Yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.SeeYou), Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
        alertDlg.setNegativeButton(getResources().getString(R.string.No), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do
            }
        });
        alertDlg.create().show();
    }

    private void showChangeLanguageDialog() {
        final String[] listLanguages = {"English", "עברית"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(TaskHome.this);
        mBuilder.setTitle(getResources().getString(R.string.ChooseLanguage));
        String currentLanguage = Locale.getDefault().getDisplayLanguage();
        int languageVal = -1;
        if(currentLanguage.equals("English")) {
            languageVal = 0;
        }
        if(currentLanguage.equals("עברית")) {
            languageVal = 1;
        }
        mBuilder.setSingleChoiceItems(listLanguages, languageVal, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which == 0) {
                    // English
                    changeTaskDetailsToEnglish();
                    setLocale("en");
                    recreate();
                }
                else if(which == 1) {
                    // Hebrew
                    changeTaskDetailsToHebrew();
                    setLocale("iw");
                    recreate();
                }
                // dismiss alert dialog when language selected(or not)
                dialog.dismiss();
            }
        });
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }

    // set language to selected one
    private void setLocale(String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        // save data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("Language", language);
        editor.apply();
    }

    // load language saved in shared preferences
    public void loadLocale() {
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = preferences.getString("Language", "");
        setLocale(language);
    }

    private void updateLocation() {
        buildLocationRequest();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, getPendingIntent());
    }

    // this function used to update location from background
    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, MyLocationService.class);
        intent.setAction(MyLocationService.ACTION_PROCESS_UPDATE);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // this function used to update location from background
    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(0);
    }

    // this function used to update location from background
    public void notifyMyTasks(final String value) {
        TaskHome.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "This runs on foreground", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void scheduleJob(View v) {
        ComponentName componentName = new ComponentName(this, LocationJobService.class);
        JobInfo info = new JobInfo.Builder(1, componentName)
                .setRequiresDeviceIdle(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if(resultCode == JobScheduler.RESULT_SUCCESS) {
            Log.d(TAG, "Job Scheduled");
        } else {
            Log.d(TAG, "Job scheduling failed");
        }
    }

    public void cancelJob(View v) {
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(1);
        Log.d(TAG, "Job cancelled");
    }

    public void openAddTask(View v) {
        Intent i = new Intent(this, AddTask.class);
        startActivity(i);
    }

    public void populateData() {
        mydb = new TaskDBHelper(activity);
        scrollView.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);

        LoadTask loadTask = new LoadTask();
        loadTask.execute();
    }

    @Override
    public void onResume() {
        super.onResume();
        populateData();
    }

    class LoadTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            overDueList.clear();
            todayList.clear();
            tomorrowList.clear();
            upcomingList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            /* ===== OVERDUE ========*/
            Cursor overDue = mydb.getDataOverDue();
            loadDataList(overDue, overDueList);
            /* ===== OVERDUE ========*/

            /* ===== TODAY ========*/
            Cursor today = mydb.getDataToday();
            loadDataList(today, todayList);
            /* ===== TODAY ========*/

            /* ===== TOMORROW ========*/
            Cursor tomorrow = mydb.getDataTomorrow();
            loadDataList(tomorrow, tomorrowList);
            /* ===== TOMORROW ========*/

            /* ===== UPCOMING ========*/
            Cursor upcoming = mydb.getDataUpcoming();
            loadDataList(upcoming, upcomingList);
            /* ===== UPCOMING ========*/

            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            loadListView(taskListOverDue,overDueList);
            loadListView(taskListToday,todayList);
            loadListView(taskListTomorrow,tomorrowList);
            loadListView(taskListUpcoming,upcomingList);

            if(overDueList.size() > 0){
                overDueText.setVisibility(View.VISIBLE);
            }else{
                overDueText.setVisibility(View.GONE);
            }

            if(todayList.size() > 0){
                todayText.setVisibility(View.VISIBLE);
            }else{
                todayText.setVisibility(View.GONE);
            }

            if(tomorrowList.size() > 0){
                tomorrowText.setVisibility(View.VISIBLE);
            }else{
                tomorrowText.setVisibility(View.GONE);
            }

            if(upcomingList.size() > 0){
                upcomingText.setVisibility(View.VISIBLE);
            }else{
                upcomingText.setVisibility(View.GONE);
            }

            loader.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
    }


    public void loadDataList(Cursor cursor, ArrayList<HashMap<String, String>> dataList) {
        if(cursor != null ) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                HashMap<String, String> mapToday = new HashMap<String, String>();
                mapToday.put(KEY_ID, cursor.getString(0));
                mapToday.put(KEY_TASK, cursor.getString(1));
                mapToday.put(KEY_DATE, Function.Epoch2DateString(cursor.getString(2), "dd-MM-yyyy"));
                mapToday.put(KEY_TIME, Function.Epoch2TimeString(cursor.getString(3), "kk:mm"));
                mapToday.put(KEY_PRIORITY, cursor.getString(4));
                mapToday.put(KEY_LOCATION, cursor.getString(5));
                dataList.add(mapToday);
                cursor.moveToNext();
            }
        }
    }


    public void loadListView(ListView listView, final ArrayList<HashMap<String, String>> dataList) {
        ListTaskAdapter adapter = new ListTaskAdapter(activity, dataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(activity, AddTask.class);
                i.putExtra("isUpdate", true);
                i.putExtra("id", dataList.get(+position).get(KEY_ID));
                startActivity(i);
            }
        });
    }


    // the following functions are utility for changing task details according to selected language:
    private void changeTaskDetailsToEnglish() {
        mydb = new TaskDBHelper(getApplicationContext());
        int dbSize = mydb.getSize();
        if(dbSize > 0) {
            String id, priority, location;
            for(int i = 1; i <= dbSize; i++) {
                id = Integer.toString(i);
                Cursor task = mydb.getDataSpecific(id);
                task.moveToFirst();
                priority = task.getString(4);
                location = task.getString(5);
                task.close();

                if(priority.equals(getResources().getString(R.string.Low))) {
                    mydb.updatePriority(id, "Low");
                }
                if(priority.equals(getResources().getString(R.string.Moderate))) {
                    mydb.updatePriority(id, "Moderate");
                }
                if(priority.equals(getResources().getString(R.string.High))) {
                    mydb.updatePriority(id, "High");
                }
                //if(location.equals(getResources().getString(R.string.LocationIsNotSet))) {
                //    mydb.updateLocation(id, "Location is not set");
                //}
            }
        }
    }
    private void changeTaskDetailsToHebrew() {
        mydb = new TaskDBHelper(getApplicationContext());
        int dbSize = mydb.getSize();
        if(dbSize > 0) {
            String id, priority, location;
            for(int i = 1; i <= dbSize; i++) {
                id = Integer.toString(i);
                Cursor task = mydb.getDataSpecific(id);
                task.moveToFirst();
                priority = task.getString(4);
                location = task.getString(5);
                task.close();

                if(priority.equals(getResources().getString(R.string.Low))) {
                    mydb.updatePriority(id, "נמוכה");
                }
                if(priority.equals(getResources().getString(R.string.Moderate))) {
                    mydb.updatePriority(id, "בינונית");
                }
                if(priority.equals(getResources().getString(R.string.High))) {
                    mydb.updatePriority(id, "גבוהה");
                }
                //if(location.equals(getResources().getString(R.string.LocationIsNotSet))) {
                //    mydb.updateLocation(id, "מיקום לא הוגדר");
                //}
            }
        }
    }
}