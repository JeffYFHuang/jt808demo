package com.cva.jt808tracker;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.util.Log;
import android.telephony.TelephonyManager;
import android.content.pm.PackageManager;
import android.Manifest.permission;

import com.cva.jt808.ClientConstants;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

import com.cva.jt808.util.IntegerUtils;
import com.cva.jt808.util.LogUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = LogUtils.makeTag(MainActivity.class);

    @BindView(R.id.btn_start_tracking)
    Button btnStartTracking;

    @BindView(R.id.btn_stop_tracking)
    Button btnStopTracking;

    @BindView(R.id.txt_status)
    TextView txtStatus;

    @BindView(R.id.server)
    TextView serverName;

    @BindView(R.id.port)
    TextView serverPort;

    Activity activity = MainActivity.this;
    public BackgroundService gpsService;
    public boolean mTracking = false;
    String wantPermission = permission.READ_PHONE_STATE;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private byte[] mPhone = null;

    private SharedPreferences mPrefs;
    private String            mHost;
    private int               mPort;
    private String            mAuthCode;

    private byte[] getPhone() {
        TelephonyManager phoneMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.i(TAG, "GetPhoneNumber ! " + ActivityCompat.checkSelfPermission(activity, wantPermission));
        if (ActivityCompat.checkSelfPermission(activity, wantPermission) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        String phone = phoneMgr.getLine1Number().substring(1,13);
        Log.i(TAG, phone);
        long l = 0;
        try {
            l = Long.parseLong(phone);
            Log.i(TAG, "GetPhoneNumber !!" + l);
        } catch (NumberFormatException nfe) {
            System.out.println("NumberFormatException: " + nfe.getMessage());
            return null;
        }
        return IntegerUtils.toBcd(l);
    }

    private void requestPermission(String permission){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)){
            Toast.makeText(activity, "Phone state permission allows us to get phone number. Please allow it for additional functionality.", Toast.LENGTH_LONG).show();
        }
        ActivityCompat.requestPermissions(activity, new String[]{permission},PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[0] == permission.READ_PHONE_STATE)
                        mPhone = getPhone();
                } else {
                    Toast.makeText(activity,"Permission Denied. We can't get phone number.", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private boolean checkPermission(String permission){
        if (Build.VERSION.SDK_INT >= 23) {
            int result = ContextCompat.checkSelfPermission(activity, permission);
            if (result == PackageManager.PERMISSION_GRANTED){
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.i(TAG, "Service onCreate");
        mPrefs = getSharedPreferences(ClientConstants.PREF_FILE_NAME, MODE_PRIVATE);
        mHost = mPrefs.getString(ClientConstants.PREF_KEY_HOST, ClientConstants.PREF_DEFAULT_HOST);
        mPort = mPrefs.getInt(ClientConstants.PREF_KEY_PORT, ClientConstants.PREF_DEFAULT_PORT);

        serverName.setText(mHost);
        serverPort.setText(String.valueOf(mPort));

        if (!checkPermission(wantPermission)) {
            requestPermission(wantPermission);
        } else {
            mPhone = getPhone();
        }

        final Intent intent = new Intent(this.getApplication(), BackgroundService.class);
        this.getApplication().startService(intent);
//        this.getApplication().startForegroundService(intent);
        this.getApplication().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @OnTextChanged(R.id.server)
    public void setServerName() {
        mPrefs.edit().putString(ClientConstants.PREF_KEY_HOST, serverName.getText().toString()).commit();
    }

    @OnTextChanged(R.id.port)
    public void setServerPort() {
        mPrefs.edit().putInt(ClientConstants.PREF_KEY_PORT, Integer.parseInt(serverPort.getText().toString())).commit();
    }

    @OnClick(R.id.btn_start_tracking)
    public void startLocationButtonClick() {
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        gpsService.startTracking();
                        mTracking = true;
                        toggleButtons();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            openSettings();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();
    }

    @OnClick(R.id.btn_stop_tracking)
    public void stopLocationButtonClick() {
        mTracking = false;
        gpsService.stopTracking();
        toggleButtons();
    }

    private void toggleButtons() {
        btnStartTracking.setEnabled(!mTracking);
        btnStopTracking.setEnabled(mTracking);
        txtStatus.setText( (mTracking) ? "TRACKING" : "GPS Ready" );
    }

    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction( Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            String name = className.getClassName();
            if (name.endsWith("BackgroundService")) {
                gpsService = ((BackgroundService.LocationServiceBinder) service).getService();
                gpsService.setPhone(mPhone);
                btnStartTracking.setEnabled(true);
                txtStatus.setText("GPS Ready");
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            if (className.getClassName().equals("BackgroundService")) {
                gpsService = null;
            }
        }
    };
}
