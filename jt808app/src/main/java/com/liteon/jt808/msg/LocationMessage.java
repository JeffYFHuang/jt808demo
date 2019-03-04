package com.liteon.jt808.msg;

import com.liteon.jt808.util.LogUtils;
import com.liteon.jt808.util.ArrayUtils;
import com.liteon.jt808.util.IntegerUtils;

/**
 * Created by DeW on 2017/3/31.
 */

public class LocationMessage extends Message {

    private static final String TAG = LogUtils.makeTag(LocationMessage.class);

    public static final short ID = 0x0200;

    private int alarm;
    private int state;
    private double longitude;
    private double latitude;
    private short altitude;
    private short speed;
    private short direction;
    private String datetime;

    private boolean emergency          = false;
    private boolean overspeed          = false;
    private boolean fatigued           = false;
    private boolean danger             = false;
    private boolean GNSS_model_broken  = false;
    private boolean GNSS_antenna_loss  = false;
    private boolean GNSS_antenna_short = false;
    private boolean low_power          = false;
    private boolean power_off          = false;

    //      additional information
    private int odb_odometer = 0;
    private short fuellevel  = 0;
    private short odb_speed  = 0;
//    private final byte[] alarmBytes;
//    private final byte[] stateBytes;
//    private final byte[] longitudeBytes;
//    private final byte[] latitudeBytes;
//    private final byte[] altitudeBytes;
//    private final byte[] speedBytes;
//    private final byte[] directionBytes;
//    private final byte[] datetimeBytes;

    private static final int MASK_EMERGENCY_ALARM          = 0x00000001;
    private static final int MASK_OVERSPEED_ALARM          = 0x00000002;
    private static final int MASK_FATIGUED_ALARM           = 0x00000004;
    private static final int MASK_DANGER_ALARM             = 0x00000008;
    private static final int MASK_GNSS_MODEL_BROKEN_ALARM  = 0x00000010;
    private static final int MASK_GNSS_ANTENNA_LOSS_ALARM  = 0x00000020;
    private static final int MASK_GNSS_ANTENNA_SHORT_ALARM = 0x00000040;
    private static final int MASK_LOW_POWER_ALARM          = 0x00000080;
    private static final int MASK_POWER_OFF_ALARM          = 0x00000100;
    //
    //

    private LocationMessage(Builder builder) {
        super(ID, builder.cipher, builder.phone, builder.body);
//        this.alarmBytes = builder.alarmBytes;
//        this.stateBytes = builder.stateBytes;
//        this.longitudeBytes = builder.longitudeBytes;
//        this.latitudeBytes = builder.latitudeBytes;
//        this.altitudeBytes = builder.altitudeBytes;
//        this.speedBytes = builder.speedBytes;
//        this.directionBytes = builder.directionBytes;
//        this.datetimeBytes = builder.datetimeBytes;

        this.alarm              = builder.alarm;
        this.state              = builder.state;
        this.longitude          = builder.longitude;
        this.latitude           = builder.latitude;
        this.altitude           = builder.altitude;
        this.speed              = builder.speed;
        this.direction          = builder.direction;
        this.datetime           = builder.datetime;
        this.emergency          = builder.emergency;
        this.overspeed          = builder.overspeed;
        this.fatigued           = builder.fatigued;
        this.danger             = builder.danger;
        this.GNSS_model_broken  = builder.GNSS_model_broken;
        this.GNSS_antenna_loss  = builder.GNSS_antenna_loss;
        this.GNSS_antenna_short = builder.GNSS_antenna_short;
        this.low_power          = builder.low_power;
        this.power_off          = builder.power_off;
        //      additional information
        this.odb_odometer       = builder.odb_odometer;
	this.fuellevel          = builder.fuellevel;
	this.odb_speed          = builder.odb_speed;
    }

    public static class Builder extends MessageBuilder {

        private int alarm        = 0;
        private int state        = 0;
        private double longitude = 0;
        private double latitude  = 0;
        private short altitude   = 0;
        private short speed      = 0;
        private short direction  = 0;
        private String datetime   = "";

        private boolean emergency          = false;
        private boolean overspeed          = false;
        private boolean fatigued           = false;
        private boolean danger             = false;
        private boolean GNSS_model_broken  = false;
        private boolean GNSS_antenna_loss  = false;
        private boolean GNSS_antenna_short = false;
        private boolean low_power          = false;
        private boolean power_off          = false;

        private boolean GNSSfix            = false;
//        private byte[] alarmBytes;
//        private byte[] stateBytes;
//        private byte[] longitudeBytes;
//        private byte[] latitudeBytes;
//        private byte[] altitudeBytes;
//        private byte[] speedBytes;
//        private byte[] directionBytes;
//        private byte[] datetimeBytes = new byte[6];
        
//      additional information
        private int odb_odometer = 0;
	private short fuellevel  = 0;
	private short odb_speed  = 0;

        public boolean isFix() {
           return longitude != 0 && latitude != 0;
        }
        
        public void reset() {
           this.alarm        = 0;
           this.state        = 0;
           this.longitude = 0;
           this.latitude  = 0;
           this.altitude   = 0;
           this.speed      = 0;
           this.direction  = 0;
           this.datetime   = "";
           this.odb_odometer = 0;
	   this.fuellevel  = 0;
	   this.odb_speed  = 0;
        }

        public void setGNSSFixed(boolean isFix) {
            this.GNSSfix = isFix;
            if(GNSSfix) state = state | 0x00000002;
        }

        public void setEmergency(boolean isEmergency) {
            this.emergency = isEmergency;
            if(emergency) alarm = alarm | MASK_EMERGENCY_ALARM;
        }

        public void setOverspeed(boolean isOverspeed) {
            this.overspeed = isOverspeed;
            if(overspeed) alarm = alarm | MASK_OVERSPEED_ALARM;
        }

        public void setFatigued(boolean isFatigued) {
            this.fatigued = isFatigued;
            if(fatigued) alarm = alarm | MASK_FATIGUED_ALARM;
        }

        public void setDanger(boolean isDanger) {
            this.danger = isDanger;
            if(danger) alarm = alarm | MASK_DANGER_ALARM;
        }

        public void setGNSSModelBroken(boolean isGNSSModelBroken) {
            this.GNSS_model_broken = isGNSSModelBroken;
            if(GNSS_model_broken) alarm = alarm | MASK_GNSS_MODEL_BROKEN_ALARM;
        }

        public void setGNSSAntennaLoss(boolean isGNSSAntennaLoss) {
            this.GNSS_antenna_loss = isGNSSAntennaLoss;
            if(GNSS_antenna_loss) alarm = alarm | MASK_GNSS_ANTENNA_LOSS_ALARM;
        }

        public void setGNSSAntennaShort(boolean isGNSSAntennaShort) {
            this.GNSS_antenna_short = isGNSSAntennaShort;
            if(GNSS_antenna_short) alarm = alarm | MASK_GNSS_ANTENNA_SHORT_ALARM;
        }

        public void setLowPower(boolean isLowPower) {
            this.low_power = isLowPower;
            if(low_power) alarm = alarm | MASK_LOW_POWER_ALARM;
        }

        public void setPowerOff(boolean isPowerOff) {
            this.power_off = isPowerOff;
            if(power_off) alarm = alarm | MASK_POWER_OFF_ALARM;
        }

        public Builder setSpeed(short speed) {
            this.speed = speed;
            return this;
        }

        public Builder setDirection(short direction) {
            this.direction = direction;
            return this;
        }

        public Builder setTimestamp(String datetime) {
            this.datetime = datetime;
            return this;
        }

        public Builder setLongitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder setLatitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder setAltitude(short altitude) {
            this.altitude = altitude;
            return this;
        }

        //additional message
        public Builder setOdbMeters(int odb_meters) {
            this.odb_odometer = odb_meters;
            return this;
        }
        
        public Builder setOdbSpeed(short odb_speed) {
            this.odb_speed = odb_speed;
            return this;
        }
                
        public Builder setFuelLevel(short fuellevel) {
            this.fuellevel = fuellevel;
            return this;
        }

        @Override
        public LocationMessage build() {
//            this.alarmBytes =  IntegerUtils.asBytes(alarm);
//            this.stateBytes =  IntegerUtils.asBytes(state);
//            this.longitudeBytes =  IntegerUtils.asBytes(longitude);
//            this.latitudeBytes =  IntegerUtils.asBytes(latitude);
//            this.altitudeBytes =  IntegerUtils.asBytes(altitude);
//            this.stateBytes =  IntegerUtils.asBytes(speed);
//            this.directionBytes =  IntegerUtils.asBytes(direction);
//            this.datetimeBytes =  IntegerUtils.toBcd(datetime);

            //Should have problem!
            
            this.body = ArrayUtils.concatenate(
                    IntegerUtils.asBytes(alarm),
                    IntegerUtils.asBytes(state),
                    IntegerUtils.asBytes(new Double(latitude*1E6).intValue()),
                    IntegerUtils.asBytes(new Double(longitude*1E6).intValue()),
                    IntegerUtils.asBytes(altitude),
                    IntegerUtils.asBytes(speed),
                    IntegerUtils.asBytes(direction),
                    IntegerUtils.toBcd(datetime),
                    IntegerUtils.asBytes((byte) 0x01),
                    IntegerUtils.asBytes((byte) 0x04),
                    IntegerUtils.asBytes((int) odb_odometer),
                    IntegerUtils.asBytes((byte) 0x02),
                    IntegerUtils.asBytes((byte) 0x02),
                    IntegerUtils.asBytes((short) fuellevel),
                    IntegerUtils.asBytes((byte) 0x03),
                    IntegerUtils.asBytes((byte) 0x02),
                    IntegerUtils.asBytes((short) odb_speed));
            byte[] ln = IntegerUtils.toBcd(datetime);
            for (byte lbyte : ln)
                 System.out.printf("%02X", lbyte);
            System.out.println("");
            for (byte theByte : this.body)
            {
                 System.out.printf("%02X", theByte);
            }
            System.out.println("");

            return new LocationMessage(this);
        }
        
        @Override
        public String toString() {
           return "Loc{sta:" + this.state + ",datetime:" + this.datetime + ",lat:" + this.latitude + ",lon:" + this.longitude + ",spd:" + this.speed + ",ang:" + this.direction + ",alt:" + this.altitude + "}";
        }
    }
}
