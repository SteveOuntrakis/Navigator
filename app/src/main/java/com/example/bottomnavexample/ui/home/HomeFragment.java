package com.example.bottomnavexample.ui.home;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.androdocs.httprequest.HttpRequest;
import com.example.bottomnavexample.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class HomeFragment extends Fragment implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private Sensor ambienttemp;
    Location mloc;
    FusedLocationProviderClient mFusedLocationProviderClient;
    private String finalAddress  = "Athens,gr";
    private String API = "b4390b51e7dc20fabf563df8ec34375f";
    private TextView addressTxt, updated_atTxt, tempTxt;
    private ImageButton btn1;
    SharedPreferences preferences;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        ambienttemp = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        addressTxt = root.findViewById(R.id.address);
        updated_atTxt = root.findViewById(R.id.updated_at);
        tempTxt = root.findViewById(R.id.temp);
        isConnected();
        new weatherTask().execute();

        btn1= root.findViewById(R.id.music);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
                startActivity(intent);
            }
        });
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean music=preferences.getBoolean("music",true);
        if(music){
            btn1.setVisibility(View.VISIBLE);
        }
        else
            btn1.setVisibility(View.GONE);

        return root;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, ambienttemp, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mloc=location;
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

    class weatherTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + finalAddress + "&units=metric&appid=" + API);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            if (isConnected()){
                try {
                    JSONObject jsonObj = new JSONObject(result);
                    JSONObject main = jsonObj.getJSONObject("main");
                    Long updatedAt = jsonObj.getLong("dt");
                    String updatedAtText = getString(R.string.updated)+" " + new SimpleDateFormat("hh:mm ", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                    String temp = main.getString("temp");
                    Double tempdouble = Double.parseDouble(temp);
                    Double fahre=tempdouble+33.8;
                    temp=String.valueOf(tempdouble.intValue());
                    String fah=String.valueOf(fahre.intValue());
                    String address = jsonObj.getString("name");
                    addressTxt.setText(address);
                    updated_atTxt.setText(updatedAtText);
                    boolean iscelsius=preferences.getBoolean("celsius",true);
                    if(iscelsius)
                    {
                        tempTxt.setText(temp+ "°C");
                    }else
                    {
                        tempTxt.setText(fah+ "°F");
                    }
                } catch (JSONException e) {
                    tempTxt.setText("no internet access");
                }
            }else{
                tempTxt.setText("No internet");
            }


        }
    }
    public boolean isConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}