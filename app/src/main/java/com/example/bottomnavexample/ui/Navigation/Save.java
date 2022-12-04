package com.example.bottomnavexample.ui.Navigation;

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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Save extends Activity {
    private MarkerOptions place1;
    private EditText editText;
    private Button button;
    private String uid;
    FirebaseFirestore fstore;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save);
        editText=findViewById(R.id.editText1);
        button=findViewById(R.id.button1);
        fstore= FirebaseFirestore.getInstance();
        db = openOrCreateDatabase("Sign_in",MODE_PRIVATE,null);
        db.execSQL("CREATE TABLE IF NOT EXISTS uid(uid TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS name(name TEXT);");
        place1= NavigationFragment.place;
        DisplayMetrics dm =new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width =dm.widthPixels;
        int height=dm.heightPixels;
        getWindow().setLayout((int) (width*.6),(int) (height*.2));
        WindowManager.LayoutParams params=getWindow().getAttributes();
        params.gravity= Gravity.CENTER;
        params.x =0;
        params.y =-20;
        getWindow().setAttributes(params);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = db.rawQuery("SELECT * FROM uid", null);
                while (cursor.moveToNext()){
                    uid=cursor.getString(0);
                }
                if (editText.getText().toString().equals(""))
                    Toast.makeText(Save.this,getString(R.string.PleaseInput), Toast.LENGTH_SHORT).show();
                else{
                        DocumentReference documentReference=fstore.collection("users").document(uid).collection("SavedPlaces").document(editText.getText().toString());
                        Map<String,Object> location=new HashMap<>();
                        location.put("Name",editText.getText().toString());
                        location.put("Latitude",place1.getPosition().latitude);
                        location.put("Longitude",place1.getPosition().longitude);
                        documentReference.set(location);
                        hideSoftKeyboard();
                        db.execSQL("INSERT INTO name values" +
                                "('"
                                +editText.getText().toString()
                                +"');");
                        Toast.makeText(Save.this,getString(R.string.SuccessfulSaved), Toast.LENGTH_LONG).show();
                        finish();
                 }
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
}
