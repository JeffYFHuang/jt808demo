package com.liteon.jt808app;

import com.liteon.javacint.common.BufferedReader;
import com.liteon.javacint.common.Bytes;
import com.liteon.javacint.gps.GpsManager;
import com.liteon.javacint.gps.GpsPosition;
import com.liteon.javacint.gps.GpsPositionListener;
import com.liteon.javacint.logging.Logger;
import com.liteon.javacint.time.DateManagement;
import com.liteon.jt808.ClientStateCallback;
import com.liteon.jt808.JT808Client;
import com.liteon.jt808.msg.AuthenticateRequest;
import com.liteon.jt808.msg.LocationMessage;
import com.liteon.jt808.msg.RegisterReply;
import com.liteon.jt808.msg.RegisterRequest;
import com.liteon.jt808.msg.ServerGenericReply;
import com.liteon.jt808.util.BCD8421Operater;
import com.liteon.jt808.util.LogUtils;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.xml.bind.DatatypeConverter;

public class jt808App implements ClientStateCallback, GpsPositionListener{

    private static final String TAG = LogUtils.makeTag(jt808App.class);

    private String            mHost;
    private int               mPort;
    private String            mAuthCode;

    private JT808Client mJT808Client;
    private RegisterRequest.Builder mRegisterReqBuilder;
    private AuthenticateRequest.Builder mAuthReqBuilder;
    private LocationMessage.Builder mLocationMessageBuilder;

    private Timer mTimer = null;
    private TimerTask mUploadTimerTask = null;
    private final GpsManager gps;
    
    private double fuellevel = ClientConstants.CAR_FUEL_FULL;
    private double odb_odometer = 0;

    public jt808App(String host, int port) {

        Logger.log("jt808App");
        mHost = host;
        mPort = port;
        mAuthCode = null;//ClientConstants.PREF_KEY_AUTH_CODE;

        mRegisterReqBuilder = new RegisterRequest.Builder();
       // mRegisterReqBuilder.phone(readImei());
        mLocationMessageBuilder  = new LocationMessage.Builder();
       // mLocationMessageBuilder.phone(readImei());

        mJT808Client = new JT808Client();
        this.gps = new GpsManager(this);
    }

    public void finalize() {
        mJT808Client.close();
        mJT808Client = null;
    }

    public void connectToServer(String host, int port){
        Logger.log("connectToServer");
        gps.start();
        mJT808Client.connect(host, port, this);
    }

    private void registerClient(){
        Logger.log("registerClient");
        byte[] mfrid = {0x37,0x30,0x39,0x36,0x30};
        byte[] cltmodel = {0x54,0x37,0x2d,0x54,0x38,0x30,0x38,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
        byte[] cltid = {0x30,0x33,0x32,0x39,0x31,0x37,0x30};
        RegisterRequest request = mRegisterReqBuilder
                                      .provId((short)44) //short
                                      .cityId((short)307) //short
                                      .mfrsId(mfrid) //5 bytes
                                      .cltModel(cltmodel) // 20 bytes
                                      .cltId(cltid)  // 7 bytes
                                      .build();
        mJT808Client.registerClient(request);
    }

    private void authenticate(String authCode) {
        if(authCode == null){
            Logger.log("鉴权码为空");
            return;
        }
        mJT808Client.authenticate(mAuthCode);
    }

    @Override
    public void connectSuccess() {
        Logger.log("连接成功");
        if(mAuthCode == null){
            registerClient();
        }else{
            authenticate(mAuthCode);
        }
    }

    @Override
    public void connectFail() {
        Logger.log("连接失败");
    }

    @Override
    public void connectionClosed() {
        Logger.log("已关闭连接");
    }

    @Override
    public void registerComplete(RegisterReply reply) {
        switch (reply.getResult()) {
            case RegisterReply.RESULT_OK:
                mAuthCode = reply.getAuthCode();
                Logger.log("authCode=" + mAuthCode);
                //begin auth
                authenticate(mAuthCode);

                Logger.log("Client registered SUCCESS !!!");
                break;
            case RegisterReply.RESULT_VEH_NOT_FOUND:
                Logger.log("Registration failed - vehicle not found");
                break;
            case RegisterReply.RESULT_VEH_REGISTERED:
                Logger.log("Registration failed - vehicle registered");
                break;
            case RegisterReply.RESULT_CLT_NOT_FOUND:
                Logger.log("Registration failed - client not found");
                break;
            case RegisterReply.RESULT_CLT_REGISTERED:
                Logger.log("Registration failed - client registered");
                break;
            default:
                Logger.log("Unknown registration result");
        }
    }

    @Override
    public void authComplete(ServerGenericReply reply) {
        switch (reply.getResult()) {
            case ServerGenericReply.RESULT_OK:
                Logger.log("Auth SUCCESS!!!");
                //Now you can send other message
                stopSendLocation();
                startSendLocation();
                break;
            case ServerGenericReply.RESULT_FAIL:
            case ServerGenericReply.RESULT_UNSUPPORTED:
            case ServerGenericReply.RESULT_BAD_REQUEST:
            case ServerGenericReply.RESULT_CONFIRM:
            default:
                Logger.log("Auth FAIL!!!");
                break;
        }
    }

    public static String getCurrentTimeStamp() {
       SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMddHHmmss");
       sdfDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
       Date now = new Date();
       String strDate = sdfDate.format(now);
       return strDate;
    }

    private void sendLocation(){
        if(mJT808Client != null){
            try {
                /*float ln = (float)121.57123 + (float)Math.random()/(float)1000.0;
                float lt = (float)25.07800 + (float)Math.random()/(float)1000.0;
                System.out.printf("lat, long: %f, %f\n", lt, ln);
                mJT808Client.sendMessage(new LocationMessage.Builder()
                        .setLatitude(lt)
                        .setLongitude(ln)
                        .setTimestamp(getCurrentTimeStamp())
                        .build());*/
                if (mLocationMessageBuilder.isFix()) {
                    mJT808Client.sendMessage(mLocationMessageBuilder.build());
                    mLocationMessageBuilder.reset();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void startSendLocation(){
        Logger.log("startSendLocation");
        if(mTimer == null) {
            mTimer = new Timer();
        }

        if (mUploadTimerTask == null) {
            mUploadTimerTask = new TimerTask() {
               @Override
               public void run() {
                  sendLocation();
               }
            };
        }

        mTimer.schedule(mUploadTimerTask, 5000, 5000);
    }

    private void stopSendLocation(){
        Logger.log("stopSendLocation");
        if(mTimer != null) {
            Logger.log("cancel timer");
            mTimer.cancel();
            mTimer = null;
            mUploadTimerTask = null;
        }
    }

    /*private TimerTask mUploadTimerTask = new TimerTask() {
        @Override
        public void run() {
            sendLocation();
        }
    };*/

    public boolean isClosed() {
        return mJT808Client.isClosed();
    }

    public void loadDrvData() {
        String fileName = ClientConstants.DRIVING_DATA;
        File file = new File(fileName);

        try (Stream<String> linesStream = Files.lines(file.toPath())) {
            linesStream.forEach((line) -> {
                int idx = line.indexOf("odb_odometer");
                if (idx >= 0)
                {
                    idx = line.indexOf(":");
                    odb_odometer = (double) Integer.parseInt(line.substring(idx + 1));
                    
                    System.out.println("odb_odometer:" + odb_odometer);
                }
                idx = line.indexOf("fuellevel");
                if (idx >= 0)
                {
                    idx = line.indexOf(":");
                    fuellevel = (double) Integer.parseInt(line.substring(idx + 1));
                    
                    System.out.println(fuellevel);
                    System.out.println("fuellevel:" + fuellevel);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    
    public void saveDrvData (String data) {
        try {
            File file = new File(ClientConstants.DRIVING_DATA);
	    FileWriter fileWriter = new FileWriter(file);
                       fileWriter.write(data+"\n");
                       fileWriter.flush();
                       fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void car_simulation (GpsPosition gps, boolean bspeed, long tdiff) {
        Random ran = new Random();
	gps.odb_speed = bspeed ? gps.speed : ClientConstants.CAR_AVG_SPEED + ran.nextInt(10) - ran.nextInt(10); //gps.speed;
        //gps.odb_speed = gps.speed;
        double distance = (double) (gps.odb_speed * 1.85200 * tdiff) / 3600.0; //tdiff secs distance
	odb_odometer += distance;
        gps.odb_odometer = odb_odometer;
	fuellevel -= distance * 0.1;
	if (fuellevel <= 0)
		fuellevel = ClientConstants.CAR_FUEL_FULL;
        gps.fuellevel = fuellevel;
        
        saveDrvData ("odb_odometer:" + (int) odb_odometer + "\nfuellevel:" + (int) fuellevel);
	System.out.println("tdiff: " + tdiff + ", odometer: " + gps.odb_odometer + ", level: " + gps.fuellevel + ", constant fuel comsumption: " + distance * 0.1 + ", speed: " + gps.odb_speed);
    }

    private Date date_prev = null;
    @Override
    public void positionReceived(GpsPosition pos) {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMddHHmmss");
        sdfDate.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        System.out.println("pos.date: " + pos.date + "system time: " + getCurrentTimeStamp());
        String datetime = "".equals(pos.date) ? getCurrentTimeStamp(): sdfDate.format(DateManagement.stringDateToTimestamp(pos.date));
        Logger.log("[GPS LOG]: " + pos.hasSignal() + ", isfix: " + pos.hasLocation());
        mLocationMessageBuilder.setGNSSFixed(pos.hasLocation());
        mLocationMessageBuilder.setGNSSAntennaLoss(!pos.hasSignal());
        Date datenow;
        try {
            datenow = sdfDate.parse(datetime);
            if (date_prev == null) {
               car_simulation (pos, pos.speed > 0, 0);
            } else
            {
               long tdiff = datenow.getTime() - date_prev.getTime();
               car_simulation (pos, pos.speed > 0, tdiff);
            }
            date_prev = datenow;
        } catch (ParseException ex) {
            java.util.logging.Logger.getLogger(jt808App.class.getName()).log(Level.SEVERE, null, ex);
        }

        mLocationMessageBuilder.setLatitude(pos.lat)
                               .setLongitude(pos.lon)
                               .setSpeed(new Double(pos.speed).shortValue())
                               .setDirection(new Double(pos.angle).shortValue())
                               .setAltitude(new Double(pos.altitude).shortValue())
                               .setTimestamp(datetime)
                               .setOdbMeters((int) pos.odb_odometer)
                               .setFuelLevel((short) pos.fuellevel)
                               .setOdbSpeed((short) pos.odb_speed);
      //  Logger.log("[GPS Time] " + pos.date);
      //  Logger.log("[GPS POS] " + pos);
        Logger.log("[GPS LocationMessage] " + mLocationMessageBuilder);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void positionAdditionnalReceived(String type, String value) {
        Logger.log("[GPS LOG] " + type + " : " + value);
    }
}
