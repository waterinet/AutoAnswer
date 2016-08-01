package com.chinanetcenter.autoanswer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_STATUS = "status";
    private static final String KEY_WTIME = "wtime";
    private boolean on;
    private int timeWait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        readConfig();
        setSwitch();
        setSpinner();

        if (on && !ListenerService.isRunning) {
            start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        writeConfig();
    }

    public void start() {
        Log.i("MainActivity: ", "start " + Integer.toString(timeWait));
        Intent intent = new Intent(this, ListenerService.class);
        intent.putExtra(ListenerService.KEY_WTIME, timeWait);
        startService(intent);
    }

    public void stop() {
        Intent intent = new Intent(this, ListenerService.class);
        stopService(intent);
    }

    private void setSpinner() {
        Spinner spinner1 = (Spinner)findViewById(R.id.spinner1);
        if (spinner1 != null) {
            final String arr[] = new String[]{" 5秒", "10秒", "15秒"};
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, arr);
            spinner1.setAdapter(arrayAdapter);
            spinner1.setSelection(timeWait / 5 - 1);
            spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int newTime = (position + 1) * 5;
                    updateService(on, newTime);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }
    }

    private void setSwitch() {
        Switch switch1 = (Switch)findViewById(R.id.switch1);
        if (switch1 != null) {
            switch1.setChecked(on);
            switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateService(isChecked, timeWait);
                }
            });
        }
    }

    private void updateService(boolean status, int wtime) {
        boolean needUpdate = false;
        if (on != status) {
            on = status;
            needUpdate = true;
        }
        if (timeWait != wtime) {
            timeWait = wtime;
            needUpdate = true;
        }
        if (needUpdate) {
            if (ListenerService.isRunning) {
                stop();
            }
            if (on) {
                start();
            }
        }
    }

    private void readConfig() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        on = prefs.getBoolean(KEY_STATUS, false);
        timeWait = prefs.getInt(KEY_WTIME, 10);
    }

    private void writeConfig() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_STATUS, on);
        editor.putInt(KEY_WTIME, timeWait);
        editor.apply();
    }
}
