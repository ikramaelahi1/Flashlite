package com.example.flashlight;

import android.app.DownloadManager;
import android.app.VoiceInteractor;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;
import android.net.wifi.WifiManager;

import java.security.Permission;

public class MainActivity extends AppCompatActivity {
 SeekBar seekbar;
 boolean success;
 Switch aSwitch;
 Switch blue;
 Switch wif;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        seekbar = (SeekBar)findViewById(R.id.seekBar);
        aSwitch = (Switch)findViewById(R.id.torch);
        blue = (Switch)findViewById(R.id.bluetooth);
        wif = (Switch)findViewById(R.id.wifi1);
        seekbar.setMax(255);
        seekbar.setProgress(getBrightness());
        getPermission();

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
       if(fromUser && success){
          setBrightness(progress);
          }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
           if(!success){
               Toast.makeText(MainActivity.this, "permission not granted!", Toast.LENGTH_SHORT).show();
           }
            }
        });
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
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
                }else{
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
                if(isChecked){

                    if (bluetoothAdapter == null) {
                        Toast.makeText(MainActivity.this, "Bluetooth is not supported!", Toast.LENGTH_SHORT).show();
                    }

                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, RESULT_OK);
                    }

                }else {
                    bluetoothAdapter.disable();

                    }

            }
        });
        wif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    WifiManager wifi1 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifi1.setWifiEnabled(true);
                }else{
                    WifiManager wifi11 = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifi11.setWifiEnabled(false);
                }
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
