package com.example.bottomnavexample.ui.Search;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bottomnavexample.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Edit_Delete extends Activity {
    private final String name= RecyclerAdapter.name;
    private final String uid= SearchFragment.uid;
    private static final String NAME = "Name";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private String newname;
    double lat,lon;
    private EditText editText1;
    private Button btn1,btn2;
    SQLiteDatabase db;
    FirebaseFirestore fstore;
    private DocumentReference myplace;
    public static boolean refrsh=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit__delete);
        editText1=findViewById(R.id.editText1);
        btn1=findViewById(R.id.button1);
        btn2=findViewById(R.id.button2);

        db = openOrCreateDatabase("Sign_in",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS uid(uid TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS name(name TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS name1(name TEXT);");
        fstore= FirebaseFirestore.getInstance();
        final Cursor cursor = db.rawQuery("SELECT name FROM name", null);
        DisplayMetrics dm =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width =dm.widthPixels;
        int height=dm.heightPixels;

        getWindow().setLayout((int) (width*.7),(int) (height*.5));
        WindowManager.LayoutParams params=getWindow().getAttributes();
        params.gravity= Gravity.CENTER;
        params.x =0;
        params.y =-20;
        getWindow().setAttributes(params);

        editText1.setText(name);
        newname=editText1.getText().toString();
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                newname=editText1.getText().toString();
                while (cursor.moveToNext()){
                    if (cursor.getString(0).equals(name)){
                        myplace=fstore.collection("users").document(uid).collection("SavedPlaces").document(name);
                        myplace.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot doc) {
                                if (doc.exists()){
                                    String name1=doc.getString(NAME);
                                    double latitude=doc.getDouble(LATITUDE);
                                    double longitude=doc.getDouble(LONGITUDE);
                                    if(name1.equals(name)){
                                        lat=latitude;
                                        lon=longitude;
                                        myplace.delete();
                                        DocumentReference documentReference=fstore.collection("users").document(uid).collection("SavedPlaces").document(newname);
                                        Map<String,Object> location=new HashMap<>();
                                        location.put("Name",newname);
                                        location.put("Latitude",lat);
                                        location.put("Longitude",lon);
                                        documentReference.set(location);
                                        db.execSQL("INSERT INTO name values" +
                                                "('"
                                                +editText1.getText().toString()
                                                +"');");
                                    }
                                    else{
                                    }
                                    Toast.makeText(Edit_Delete.this,R.string.SuccessfulEdited, Toast.LENGTH_LONG).show();
                                    refrsh=true;
                                    finish();
                                }
                            }
                        });
                    }else{
                        db.execSQL("INSERT INTO name1 values" +
                                "('"
                                +cursor.getString(0)
                                +"');");
                    }

                }
                db.execSQL("DROP TABLE name");
                db.execSQL("CREATE TABLE IF NOT EXISTS name(name TEXT);");
                Cursor cursor1=db.rawQuery("SELECT name FROM name1", null);
                while(cursor1.moveToNext()){
                    db.execSQL("INSERT INTO name values" +
                            "('"
                            +cursor1.getString(0)
                            +"');");
                }
                db.execSQL("DROP TABLE name1");
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                myplace=fstore.collection("users").document(uid).collection("SavedPlaces").document(name);
                myplace.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            myplace.delete();
                            Toast.makeText(Edit_Delete.this,R.string.SuccessfulDeleted, Toast.LENGTH_LONG).show();
                            refrsh=true;
                            finish();
                        }
                    }
                });
            }
        });
    }
    private void hideSoftKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
