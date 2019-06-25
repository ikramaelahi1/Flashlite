package com.example.flashlight;

import android.Manifest;
import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.net.wifi.WifiManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Permission;

public class MainActivity extends AppCompatActivity {
    static final String STATUS_ON = "Mobile Data: Enable";
    static final String STATUS_OFF = "Mobile Data: Disable";

    static final String TURN_ON = "Enable";
    static final String TURN_OFF = "Disable";

    SeekBar seekbar;
 boolean success;
 Switch aSwitch;
 Switch blue;
 Switch wif;
 Switch dat;


    public static String CHANGE_NETWORK_STATE = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        aSwitch = (Switch) findViewById(R.id.torch);
        blue = (Switch) findViewById(R.id.bluetooth);
        wif = (Switch) findViewById(R.id.wifi1);
        dat = (Switch) findViewById(R.id.data);
        seekbar.setMax(255);
        seekbar.setProgress(getBrightness());
        getPermission();
        CHANGE_NETWORK_STATE = "android.permission.CHANGE_NETWORK_STATE";

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && success) {
                    setBrightness(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!success) {
                    Toast.makeText(MainActivity.this, "permission not granted!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        String cameraId = null; // Usually back camera is at 0 position.
                        try {
                            cameraId = camManager.getCameraIdList()[0];
                            camManager.setTorchMode(cameraId, true);   //Turn ON
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        CameraManager camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        String cameraId = null; // Usually back camera is at 0 position.
                        try {
                            cameraId = camManager.getCameraIdList()[0];
                            camManager.setTorchMode(cameraId, false);   //Turn OFF
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        });
        blue.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (isChecked) {

                    if (bluetoothAdapter == null) {
                        Toast.makeText(MainActivity.this, "Bluetooth is not supported!", Toast.LENGTH_SHORT).show();
                    }

                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, RESULT_OK);
                    }

                } else {
                    bluetoothAdapter.disable();

                }

            }
        });
        wif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WifiManager wifi1 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifi1.setWifiEnabled(true);
                } else {
                    WifiManager wifi11 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifi11.setWifiEnabled(false);
                }
            }
        });
        dat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean state=isMobileDataEnable();
                // toggle the state
                if(state)toggleMobileDataConnection(true);
                else toggleMobileDataConnection(false);
            }
        });

        }
    private void setBrightness(int brightness){
        if(brightness<0){
            brightness=0;
        }else if(brightness>255){
            brightness=255;
        }
        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
    }
    private int getBrightness(){
        int brightness=100;
        try{
            ContentResolver contentResolver = getApplicationContext().getContentResolver();
            brightness = Settings.System.getInt(contentResolver,Settings.System.SCREEN_BRIGHTNESS);
        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return brightness;
    }
    private void getPermission(){
        boolean value;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            value = Settings.System.canWrite(getApplicationContext());

            if (value) {
                success = true;
            }else{
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package: "+ getApplicationContext().getPackageName()));
                startActivityForResult(intent,1000);
            }
        }
    }
    private boolean isMobileDataEnable() {


        boolean mobileDataEnabled=false; // Assume disabled
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED) {
            String[] arr = {Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.ACCESS_NETWORK_STATE};

            ActivityCompat.requestPermissions(MainActivity.this, arr, 69);}
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
        } catch (Exception e) {
            Toast.makeText(this, "problem accured!", Toast.LENGTH_SHORT).show();
        }
        return mobileDataEnabled;
    }
    public boolean toggleMobileDataConnection(boolean ON)
    {
        try {
            //create instance of connectivity manager and get system connectivity service
            final ConnectivityManager conman = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            //create instance of class and get name of connectivity manager system service class
            final Class conmanClass  = Class.forName(conman.getClass().getName());
            //create instance of field and get mService Declared field
            final Field iConnectivityManagerField= conmanClass.getDeclaredField("mService");
            //Attempt to set the value of the accessible flag to true
            iConnectivityManagerField.setAccessible(true);
            //create instance of object and get the value of field conman
            final Object iConnectivityManager = iConnectivityManagerField.get(conman);
            //create instance of class and get the name of iConnectivityManager field
            final Class iConnectivityManagerClass=  Class.forName(iConnectivityManager.getClass().getName());
            //create instance of method and get declared method and type
            final Method setMobileDataEnabledMethod= iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled",Boolean.TYPE);
            //Attempt to set the value of the accessible flag to true
            setMobileDataEnabledMethod.setAccessible(true);
            //dynamically invoke the iConnectivityManager object according to your need (true/false)
            setMobileDataEnabledMethod.invoke(iConnectivityManager, ON);
        } catch (Exception e){
        e.getMessage();
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1000){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                boolean value=Settings.System.canWrite(getApplicationContext());
                if(value){
                    success=true;
                }else{
                    Toast.makeText(this, "permission not granted!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
