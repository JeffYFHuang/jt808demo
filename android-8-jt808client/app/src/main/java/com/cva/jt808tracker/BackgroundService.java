package com.cva.jt808tracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.cva.jt808.ClientConstants;
import com.cva.jt808.ClientStateCallback;
import com.cva.jt808.JT808Client;
import com.cva.jt808.msg.AuthenticateRequest;
import com.cva.jt808.msg.LocationMessage;
import com.cva.jt808.msg.MessageBuilder;
import com.cva.jt808.msg.RegisterReply;
import com.cva.jt808.msg.RegisterRequest;
import com.cva.jt808.msg.ServerGenericReply;
import com.cva.jt808.util.LogUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service implements ClientStateCallback{
    private static final String TAG = LogUtils.makeTag(BackgroundService.class);
    private final LocationServiceBinder binder = new LocationServiceBinder();;
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager notificationManager;

    private final int LOCATION_INTERVAL = 0;
    private final int LOCATION_DISTANCE = 0;

    private Timer mTimer = null;

    private boolean isRunning  = false;
    private SharedPreferences mPrefs;
    private String            mHost;
    private int               mPort;
    private String            mAuthCode;

    private JT808Client mJT808Client;
    private RegisterRequest.Builder mRegisterReqBuilder;
    private AuthenticateRequest.Builder mAuthReqBuilder;
    private LocationMessage.Builder mLocationMessageBuilder;

    private byte[] mPhone = null;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener
    {
        private Location lastLocation = null;
        private final String TAG = "LocationListener";
        private Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation = location;
            Log.i(TAG, "LocationChanged: "+location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + status);
        }

        public Location getLocation()
        {
            return mLastLocation;
        }

        public void setLocationTime(long ts) {
            mLastLocation.setTime(ts);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.i(TAG, "Service onCreate");
        mRegisterReqBuilder = new RegisterRequest.Builder();
        mLocationMessageBuilder  = new LocationMessage.Builder();

        mJT808Client = new JT808Client();

        startForeground(12345678, getNotification());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mJT808Client.close();
        stopSendLocation();
        //mJT808Client = null;
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void setPhone(byte[] phone) {
        if (phone != null && phone.length == 6)
            mPhone = phone;
    }

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener );

        } catch (java.lang.SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        mPrefs = getSharedPreferences(ClientConstants.PREF_FILE_NAME, MODE_PRIVATE);
        mHost = mPrefs.getString(ClientConstants.PREF_KEY_HOST, ClientConstants.PREF_DEFAULT_HOST);
        mPort = mPrefs.getInt(ClientConstants.PREF_KEY_PORT, ClientConstants.PREF_DEFAULT_PORT);
        mAuthCode = mPrefs.getString(ClientConstants.PREF_KEY_AUTH_CODE, null);

        connectToServer(mHost, mPort);
    }

    public void stopTracking() {
        this.onDestroy();
    }

    private Notification getNotification() {

        NotificationChannel channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
        return builder.build();
    }

    
    public class LocationServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    private void connectToServer(String host, int port){
        mJT808Client.connect(host, port, this);
    }

    private void registerClient(){
        MessageBuilder builder = new RegisterRequest.Builder();
        builder.phone(mPhone);
        RegisterRequest request = ((RegisterRequest.Builder)builder).build();
        mJT808Client.registerClient(request);
    }

    private void authenticate(String authCode) {
        if(authCode == null){
            Toast.makeText(this, "鉴权码为空", Toast.LENGTH_SHORT).show();
            return;
        }
        MessageBuilder builder = new AuthenticateRequest.Builder(authCode);
        builder.phone(mPhone);
        AuthenticateRequest request = ((AuthenticateRequest.Builder)builder).build();
        mJT808Client.authenticate(request);
    }

    @Override
    public void connectSuccess() {
        Toast.makeText(this, "连接成功", Toast.LENGTH_SHORT).show();
        if(mAuthCode == null){
            registerClient();
        }else{
            authenticate(mAuthCode);
        }
    }

    @Override
    public void connectFail() {
        Toast.makeText(this, "连接失败", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionClosed() {
        Toast.makeText(this, "已关闭连接", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void registerComplete(RegisterReply reply) {
        switch (reply.getResult()) {
            case RegisterReply.RESULT_OK:
                mAuthCode = reply.getAuthCode();
                Log.d(TAG, "authCode=" + mAuthCode);
                mPrefs.edit().putString(ClientConstants.PREF_KEY_AUTH_CODE, mAuthCode).commit();
                //begin auth
                authenticate(mAuthCode);

                Log.i(TAG, "Client registered SUCCESS !!!");
                break;
            case RegisterReply.RESULT_VEH_NOT_FOUND:
                Log.w(TAG, "Registration failed - vehicle not found");
                break;
            case RegisterReply.RESULT_VEH_REGISTERED:
                Log.w(TAG, "Registration failed - vehicle registered");
                break;
            case RegisterReply.RESULT_CLT_NOT_FOUND:
                Log.w(TAG, "Registration failed - client not found");
                break;
            case RegisterReply.RESULT_CLT_REGISTERED:
                Log.w(TAG, "Registration failed - client registered");
                break;
            default:
                Log.e(TAG, "Unknown registration result");
        }
    }

    @Override
    public void authComplete(ServerGenericReply reply) {
        switch (reply.getResult()) {
            case ServerGenericReply.RESULT_OK:
                Log.d(TAG, "Auth SUCCESS!!!");
                //Now you can send other message
                startSendLocation();
                break;
            case ServerGenericReply.RESULT_FAIL:
            case ServerGenericReply.RESULT_UNSUPPORTED:
            case ServerGenericReply.RESULT_BAD_REQUEST:
            case ServerGenericReply.RESULT_CONFIRM:
            default:
                Log.e(TAG, "Auth FAIL!!!");
                break;
        }
    }

    public static String getCurrentTimeStamp(long ts) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMddHHmmss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        if (ts == 0) {
            Date now = new Date();
            return sdfDate.format(now);
        } else
            return sdfDate.format(ts);
    }

    private void sendLocation(){
        if(mJT808Client != null){
            Location location = mLocationListener.getLocation();
            try {
                if (location.hasAccuracy()) {
                    MessageBuilder builder = new LocationMessage.Builder();
                    ((LocationMessage.Builder) builder).setGNSSFixed(location.getTime() != 0);
                    builder.phone(mPhone);

                    mJT808Client.sendMessage(((LocationMessage.Builder) builder)
                            .setLatitude(location.getLatitude())
                            .setLongitude(location.getLongitude())
                            .setAltitude((short) location.getAltitude())
                            .setSpeed((short) location.getSpeed())
                            .setDirection((short) location.getBearing())
                            .setTimestamp(Long.parseLong(getCurrentTimeStamp(location.getTime())))
                            .build());

                    mLocationListener.setLocationTime(0); //For checking if location data is valid.
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void startSendLocation(){
        if(mTimer == null) {
            mTimer = new Timer();
        }
        mTimer.schedule(mUploadTimerTask, 5000, 5000);
    }

    private void stopSendLocation(){
        if(mTimer != null)
            mTimer.cancel();
    }

    private TimerTask mUploadTimerTask = new TimerTask() {
        @Override
        public void run() {
            sendLocation();
        }
    };
}
