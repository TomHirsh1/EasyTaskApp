package tomhirsh2.gmail.com.easytaskapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;

public class GoogleMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentUserLocationMarker;
    private static final int Request_User_Location_Code = 99;

    TaskDBHelper mydb;
    Activity activity;

    static String chosenAddress = "Location is not set"; // this will be saved for each task
    static double latitudeValue, longitudeValue;        // as well as these values
    boolean isLocationChosen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_maps);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
        checkUserLocationPermission();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mydb = new TaskDBHelper(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }
    public Boolean checkUserLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Request_User_Location_Code);
            }
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case Request_User_Location_Code:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                            if( googleApiClient == null){
                                buildGoogleApiClient();
                            }
                            mMap.setMyLocationEnabled(true);
                    }
                }
                else{
                    Toast.makeText(this, getResources().getString(R.string.PermissionDenied), Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    protected synchronized void buildGoogleApiClient(){
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
        if(currentUserLocationMarker != null){
            currentUserLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("User Current Location");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentUserLocationMarker = mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 18.0f));
        //mMap.moveCamera(CameraUpdateFactory.zoomBy(17));

        if(googleApiClient != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient,this);
        }

        int dbSize = mydb.getSize();
        if(dbSize > 0) {
            displayChosenLocations(dbSize);
        }
    }

    private void displayChosenLocations(int dbSize) {
        String locationString;
        double latitudeVal, longitudeVal;
        String id, snippet;

        for(int i = 1; i <= dbSize; i++) {
            id = Integer.toString(i);
            Cursor task = mydb.getDataSpecific(id);
            task.moveToFirst();
            latitudeVal = task.getDouble(6);
            longitudeVal = task.getDouble(7);
            locationString = task.getString(5);
            snippet = task.getString(1);
            task.close();

            if(!locationString.equals("Location is not set")) {
                MarkerOptions userMarkerOptions = new MarkerOptions();
                if(latitudeVal != 0 && longitudeVal != 0) {
                    LatLng latLng = new LatLng(latitudeVal, longitudeVal);
                    userMarkerOptions.position(latLng);
                    userMarkerOptions.title(locationString);
                    userMarkerOptions.snippet(snippet);
                    userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    mMap.addMarker(userMarkerOptions);
                }
            }
        }
    }

    public void onClick(View v){
        if(!isLocationChosen) {
            switch (v.getId()) {
                case R.id.search_address:
                    EditText addressField = (EditText) findViewById(R.id.location_search);
                    String searchAddress = addressField.getText().toString();
                    List<Address> addressList = null;
                    MarkerOptions userMarkerOptions = new MarkerOptions();
                    if (!TextUtils.isEmpty(searchAddress)) {
                        Geocoder geocoder = new Geocoder(this);
                        try {
                            addressList = geocoder.getFromLocationName(searchAddress, 6);
                            if (addressList != null) {
                                for (int i = 0; i < addressList.size(); i++) {
                                    Address userAddress = addressList.get(i);
                                    LatLng latLng = new LatLng(userAddress.getLatitude(), userAddress.getLongitude());

                                    chosenAddress = userAddress.getAddressLine(i);
                                    latitudeValue = userAddress.getLatitude();
                                    longitudeValue = userAddress.getLongitude();
                                    userMarkerOptions.position(latLng);
                                    userMarkerOptions.title(userAddress.getAddressLine(i));
                                    userMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                                    mMap.addMarker(userMarkerOptions);
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
                                    isLocationChosen = true;
                                    hideSoftKeyboard();
                                    Toast.makeText(this, getResources().getString(R.string.LocationWasSaved), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, getResources().getString(R.string.LocationNotFound), Toast.LENGTH_LONG).show();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.PleaseWriteAnyLocationName), Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
      if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}