package com.example.bottomnavexample.ui.Navigation;

import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.bottomnavexample.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;

public class NavigationFragment extends Fragment implements OnMapReadyCallback, LocationListener, View.OnClickListener {

    GoogleMap mGoogleMap;
    MapView mMapView;
    FusedLocationProviderClient mFusedLocationProviderClient;
    private Location  mloc;
    private Boolean first_time=true;
    public static MarkerOptions place;
    private LocationCallback locationCallback;
    private Button btn1,btn2;


    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_navigation, container, false);
        Places.initialize(getActivity(), getString(R.string.google_maps_api));
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mMapView = root.findViewById(R.id.mapp);
        mMapView.onCreate(null);
        mMapView.onResume();
        mMapView.getMapAsync(this);
        btn1=root.findViewById(R.id.button1);
        btn2=root.findViewById(R.id.button2);
        btn1.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mGoogleMap=googleMap;
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        if (mMapView != null && mMapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mMapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 40, 40);
            layoutParams.height=150;
            layoutParams.width=150;
        }
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());
        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });
        task.addOnFailureListener(getActivity(), new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(getActivity(), 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mGoogleMap.clear();
                MarkerOptions markerOptions = new MarkerOptions()
                        .title("Selected")
                        .position(latLng);
                mGoogleMap.addMarker(markerOptions);
                place=markerOptions;
                btn1.setVisibility(View.VISIBLE);
                btn2.setVisibility(View.VISIBLE);
                btn1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i =new Intent(getActivity(), Save.class);
                        startActivity(i);
                    }
                });
                btn2.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        mGoogleMap.clear();
                        btn1.setVisibility(View.GONE);
                        btn2.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mloc = task.getResult();
                            if (mloc != null) {
                                if(first_time){
                                    CameraPosition cameraPosition=new CameraPosition.Builder().target(new LatLng(mloc.getLatitude(), mloc.getLongitude())).zoom(18).tilt(45).build();
                                    mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                    first_time=false;
                                }
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mloc = locationResult.getLastLocation();
                                        CameraPosition cameraPosition=new CameraPosition.Builder().target(new LatLng(mloc.getLatitude(), mloc.getLongitude())).zoom(18).tilt(45).build();
                                        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                            }
                        } else {

                        }
                    }
                });
    }
    @Override
    public void onLocationChanged(Location location) {
        mloc=location;
        moveCamera(new LatLng(mloc.getLatitude(),mloc.getLongitude()));
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

    @Override
    public void onClick(View v) {

    }
    private void moveCamera(LatLng latLng) {
        mGoogleMap.clear();
        CameraPosition cameraPosition=new CameraPosition.Builder().target(latLng).zoom(18).tilt(45).build();
        mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

}