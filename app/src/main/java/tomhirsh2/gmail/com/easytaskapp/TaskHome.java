package tomhirsh2.gmail.com.easytaskapp;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import tomhirsh2.gmail.com.easytaskapp.services.LocationJobService;


public class TaskHome extends AppCompatActivity {

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

    public static String KEY_ID = "id";
    public static String KEY_TASK = "task";
    public static String KEY_DATE = "date";
    public static String KEY_TIME = "time";
    public static String KEY_PRIORITY = "priority";
    public static String KEY_LOCATION = "location";

    private static final String TAG = "TaskHome";
    TextView text;
    CheckBox checkBox;

    static TaskHome instance;
    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationProviderClient;

    public static TaskHome getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_home);

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
                        Toast.makeText(TaskHome.this, "Accept this to track your location", Toast.LENGTH_LONG).show();
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

        /*
        checkBox = (CheckBox) findViewById(R.id.task_check);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkBox.isChecked()) {
                    text = findViewById(R.id.task_name);
                    if(!text.getPaint().isStrikeThruText()) {
                        text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }

                }
            }
        });
        */
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

        /*
        text = findViewById(R.id.task_name);
        checkBox = findViewById(R.id.task_check);

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!text.getPaint().isStrikeThruText()) {
                    text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                else {
                    text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        });
        */
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

            if(overDueList.size()>0){
                overDueText.setVisibility(View.VISIBLE);
            }else{
                overDueText.setVisibility(View.GONE);
            }

            if(todayList.size()>0){
                todayText.setVisibility(View.VISIBLE);
            }else{
                todayText.setVisibility(View.GONE);
            }

            if(tomorrowList.size()>0){
                tomorrowText.setVisibility(View.VISIBLE);
            }else{
                tomorrowText.setVisibility(View.GONE);
            }

            if(upcomingList.size()>0){
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
            while (cursor.isAfterLast() == false) {

                HashMap<String, String> mapToday = new HashMap<String, String>();
                mapToday.put(KEY_ID, cursor.getString(0).toString());
                mapToday.put(KEY_TASK, cursor.getString(1).toString());
                mapToday.put(KEY_DATE, Function.Epoch2DateString(cursor.getString(2).toString(), "dd-MM-yyyy"));
                mapToday.put(KEY_TIME, Function.Epoch2TimeString(cursor.getString(3).toString(), "kk:mm"));
                mapToday.put(KEY_PRIORITY, cursor.getString(4).toString());
                mapToday.put(KEY_LOCATION, cursor.getString(5).toString());
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
}