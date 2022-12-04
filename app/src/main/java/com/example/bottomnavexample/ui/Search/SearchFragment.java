package com.example.bottomnavexample.ui.Search;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottomnavexample.R;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.MODE_PRIVATE;

public class SearchFragment extends Fragment implements LocationListener {


    FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback locationCallback;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;
    private MaterialSearchBar materialSearchBar;
    private RecyclerAdapter recyclerAdapter;
    private DocumentReference myplace;
    private static final String NAME = "Name";
    public static Location mloc;
    FirebaseFirestore fstore;
    SQLiteDatabase db;
    public static String uid;
    private boolean refresh;
    public static boolean stoprefresh=true;


    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_search, container, false);
        fstore= FirebaseFirestore.getInstance();
        db = getActivity().openOrCreateDatabase("Sign_in",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS uid(uid TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS name(name TEXT);");
        final RecyclerView recyclerView= root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        Places.initialize(getActivity(), getString(R.string.google_maps_api));
        materialSearchBar = root.findViewById(R.id.searchBar);
        placesClient = Places.createClient(getActivity());
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getDeviceLocation();
        searchbar();
        final ArrayList<String> PlacesArray= new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM uid", null);
        final Cursor cursor1 = db.rawQuery("SELECT name FROM name", null);
        while (cursor.moveToNext()){
            myplace=fstore.collection("users").document(cursor.getString(0));
            uid=cursor.getString(0);
            while (cursor1.moveToNext()){
                    myplace=fstore.collection("users").document(cursor.getString(0)).collection("SavedPlaces").document(cursor1.getString(0));
                    myplace.get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot doc) {
                                    if ((doc.exists())){
                                        String name=doc.getString(NAME);
                                        if(!PlacesArray.contains(name))
                                        {
                                            PlacesArray.add(name);
                                        }
                                        recyclerAdapter = new RecyclerAdapter(getActivity(), PlacesArray);
                                        recyclerView.setAdapter(recyclerAdapter);
                                    }else{
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
            }
        return root;
    }

    private void geoLocate() {
        String searchString = materialSearchBar.getText();
        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> list = new ArrayList<>();
        try {
            list = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
        }
        if (list.size() > 0) {
            Address address = list.get(0);
            String uri = "http://maps.google.com/maps?saddr=" + mloc.getLatitude()+ "," + mloc.getLongitude() + "&daddr=" + address.getLatitude() + "," + address.getLongitude();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
            hideSoftKeyboard();
        }
    }
    private void hideSoftKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void searchbar(){
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }
            @Override
            public void onSearchConfirmed(CharSequence text) {
                getActivity().startSearch(text.toString(), true, null, true);
            }
            @Override
            public void onButtonClicked(int buttonCode) {
                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
                    materialSearchBar.disableSearch();
                }
            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
            }
            @Override
            public void onSearchConfirmed(CharSequence text) {
                geoLocate();
            }
            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
                        .setCountry("gr")
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setSessionToken(token)
                        .setQuery(s.toString())
                        .build();
                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
                    @Override
                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
                        if (task.isSuccessful()) {
                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
                            if (predictionsResponse != null) {
                                predictionList = predictionsResponse.getAutocompletePredictions();
                                List<String> suggestionsList = new ArrayList<>();
                                for (int i = 0; i < predictionList.size(); i++) {
                                    AutocompletePrediction prediction = predictionList.get(i);
                                    suggestionsList.add(prediction.getFullText(null).toString());
                                }
                                materialSearchBar.updateLastSuggestions(suggestionsList);
                                if (!materialSearchBar.isSuggestionsVisible()) {
                                    materialSearchBar.showSuggestionsList();
                                }
                            }
                        } else {
                            materialSearchBar.showSuggestionsList();
                        }
                    }
                });
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
            @Override
            public void OnItemClickListener(int position, View v) {
                if (position >= predictionList.size()) {
                    return;
                }
                AutocompletePrediction selectedPrediction = predictionList.get(position);
                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
                materialSearchBar.setText(suggestion);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        materialSearchBar.clearSuggestions();
                    }
                }, 1000);
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null)
                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                final String placeId = selectedPrediction.getPlaceId();
                final List<Place.Field> placeFields = Arrays.asList(Place.Field.TYPES, Place.Field.VIEWPORT, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.NAME, Place.Field.ID, Place.Field.RATING, Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
                    @Override
                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                        Place place = fetchPlaceResponse.getPlace();
                        LatLng latLngOfPlace = place.getLatLng();
                        if (latLngOfPlace != null) {
                            String uri = "http://maps.google.com/maps?saddr=" + mloc.getLatitude()+ "," + mloc.getLongitude() + "&daddr=" + latLngOfPlace.latitude + "," + latLngOfPlace.longitude;
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            startActivity(intent);
                            hideSoftKeyboard();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof ApiException) {
                            ApiException apiException = (ApiException) e;
                            apiException.printStackTrace();
                            int statusCode = apiException.getStatusCode();
                        }
                    }
                });
            }
            @Override
            public void OnItemDeleteListener(int position, View v) {
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
    public void onResume() {
        super.onResume();
        refresh=Edit_Delete.refrsh;
        stoprefresh=!stoprefresh;
        if(refresh && stoprefresh){
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
    }


}