package com.example.bottomnavexample.ui.Settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.bottomnavexample.R;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private RadioButton r1,r2,r3,r4;
    private Switch switch1,switch2;
    private TextView textView1;
    private String s1="Language:";
    private Button btn1;
    SharedPreferences preferences;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        textView1=root.findViewById(R.id.textView1);
        btn1=root.findViewById(R.id.button1);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = getActivity().getBaseContext().getPackageManager().
                        getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        });
        r1=root.findViewById(R.id.radioButton);
        r2=root.findViewById(R.id.radioButton2);
        r3=root.findViewById(R.id.celsius);
        r4=root.findViewById(R.id.Fahrenheit);
        boolean iscelsius=preferences.getBoolean("celsius",true);
        if (iscelsius){
            r3.setChecked(true);
            r4.setChecked(false);
        }
        else{
            r3.setChecked(false);
            r4.setChecked(true);
        }
        r3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r3.setChecked(true);
                r4.setChecked(false);
                preferences.edit().putBoolean("celsius",r3.isChecked()).apply();
                refresh();
            }
        });
        r4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r3.setChecked(false);
                r4.setChecked(true);
                preferences.edit().putBoolean("celsius",r3.isChecked()).apply();
                refresh();
            }
        });
        switch1=root.findViewById(R.id.switch1);
        boolean darkmode=preferences.getBoolean("darkmode",true);
        if (darkmode){
            switch1.setChecked(true);
        }
        else{
            switch1.setChecked(false);
        }
        switch1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    preferences.edit().putBoolean("darkmode",switch1.isChecked()).apply();
                    refresh();
            }
        });
        switch2=root.findViewById(R.id.switch2);
        boolean music=preferences.getBoolean("music",true);
        if (music){
            switch2.setChecked(true);
        }else
            switch2.setChecked(false);
        switch2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    preferences.edit().putBoolean("music",switch2.isChecked()).apply();
                    refresh();
            }
        });
        if(textView1.getText().toString().equals(s1)){
            r2.setChecked(true);
            r1.setChecked(false);
            preferences.edit().putBoolean("bntChecked", r2.isChecked()).apply();
        }

        else{
            r1.setChecked(true);
            r2.setChecked(false);
            preferences.edit().putBoolean("bntChecked", r2.isChecked()).apply();
        }

        r1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r1.setChecked(true);
                r2.setChecked(false);
                setAppLocale("gr");
                refresh();
            }
        });
        r2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r1.setChecked(false);
                r2.setChecked(true);
                setAppLocale("en");
                refresh();
            }
        });
        return root;
    }
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
    private void refresh(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(this).attach(this).commit();
    }
}