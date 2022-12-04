package com.example.bottomnavexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements  LocationListener {
    LocationManager locationManager;
    static final int REQ_CODE = 654;
    private String uid;
    FirebaseFirestore fstore;
    SQLiteDatabase db;
    View myview,naview;
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
        myview= findViewById(R.id.nav_host_fragment);
        naview= findViewById(R.id.nav_view);
        getSupportActionBar().hide();
        BottomNavigationView navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,R.id.navigation_Navigation,  R.id.navigation_search, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        fstore= FirebaseFirestore.getInstance();
        db = openOrCreateDatabase("Sign_in",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS uid(uid TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS name(name TEXT);");
        gpson();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putBoolean("darkmode",true);
        boolean english=preferences.getBoolean("bntChecked",true);
        boolean darkmode=preferences.getBoolean("darkmode",true);
        if (english){
            setAppLocale("en");
        }
        else{
            setAppLocale("gr");
        }
        if (darkmode){
            SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            Sensor lightSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            if(lightSensor != null){
                mySensorManager.registerListener(
                        lightSensorListener,
                        lightSensor,
                        SensorManager.SENSOR_DELAY_NORMAL);
            } else {
            }
        }else{
            myview.getRootView().setBackgroundColor(Color.WHITE);
            naview.getRootView().setBackgroundColor(Color.WHITE);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);    }
    @SuppressLint("MissingPermission")
    public void gpson() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)  {
            ActivityCompat.requestPermissions(
                    this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ_CODE);
            uid=UUID.randomUUID().toString();
            db.execSQL("INSERT INTO uid values" +
                        "('"
                        +uid
                        +"');");
            Cursor cursor = db.rawQuery("SELECT * FROM uid", null);
            while (cursor.moveToNext()) {
                DocumentReference documentReference=fstore.collection("users").document(cursor.getString(0));
                Map<String,Object> user=new HashMap<>();
                documentReference.set(user);
            }
        }else{
            locationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this);
           }
    }
    @Override
    public void onLocationChanged(Location location) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(String provider) {

    }
    @Override
    public void onProviderDisabled(String provider) {

    }
    private final SensorEventListener lightSensorListener
            = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        @Override
        public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                    if (event.values[0] >= 1000) {
                        myview.getRootView().setBackgroundColor(Color.WHITE);
                        naview.getRootView().setBackgroundColor(Color.WHITE);
                    } else {
                        myview.getRootView().setBackgroundColor(Color.DKGRAY);
                        naview.getRootView().setBackgroundColor(Color.DKGRAY);
                    }
                }
        }
    };
    private void setAppLocale(String locale){
        Resources resources=getResources();
        DisplayMetrics displayMetrics=resources.getDisplayMetrics();
        Configuration configuration=resources.getConfiguration();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            configuration.setLocale(new Locale(locale.toLowerCase()));
        }else{
            configuration.locale=new Locale(locale.toLowerCase());
        }
        resources.updateConfiguration(configuration,displayMetrics);
    }
}
