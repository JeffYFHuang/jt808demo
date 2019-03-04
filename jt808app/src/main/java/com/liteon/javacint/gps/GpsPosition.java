package com.liteon.javacint.gps;

import com.liteon.javacint.common.Bytes;
import com.liteon.javacint.common.Math2;
import com.liteon.javacint.time.DateManagement;

/**
 * GPS Position.
 */
public class GpsPosition {

    public static final byte STATUS_OK = 0;
    public static final byte STATUS_NO_LOC = -1;
    public static final byte STATUS_NO_SIGNAL = -2;
    /**
     * Date
     */
    public String date;
    /**
     * Latitude
     */
    public double lat;
    /**
     * Longitude
     */
    public double lon;
    /**
     * Speed
     */
    public double speed;
    /**
     * Altitude
     */
    public double altitude;
    /**
     * Angle
     */
    public float angle;
    /**
     * Dillution of precision
     */
    public double dop;
    /**
     * Number of satellites.
     * <ul>
     * <li>&gt;=0 : 0 or more satellites</li>
     * <li>-1 : We don't have the information but we received some data</li>
     * <li>-2 : We don't have the information and we didn't receive anything
     * yet</li>
     * </ul>
     */
    public int nbSatellites = -2;
    /**
     * Bogus time.
     * It is hour*3600 + minute*60 + second of the present day.
     */
    public int btime;
    /**
     * Status
     */
    public byte status;

    public double odb_speed;
    public double odb_odometer;
    public double fuellevel;
    /**
     * Creation of an empty position
     */
    public GpsPosition() {
        lat = 0;
        lon = 0;
        speed = 0;
        altitude = 0;
        date = "";
        dop = 0;
        odb_speed = 0;
        odb_odometer = 0;
        fuellevel = 0;
    }

    /**
     * Copy constructor
     *
     * @param source Source GpsPosition to copy from
     */
    public GpsPosition(GpsPosition source) {
        status = source.status;
        lat = source.lat;
        lon = source.lon;
        date = source.date;
        speed = source.speed;
        angle = source.angle;
        altitude = source.altitude;
        dop = source.dop;
        btime = source.btime;
        nbSatellites = source.nbSatellites;
        odb_speed = source.odb_speed;
        odb_odometer = source.odb_odometer;
        fuellevel = source.fuellevel;
    }

    /**
     * Get the compact view of a GPS Position
     *
     * @return Compact view of GPS Position :
     * YYMMDDhhmmss,lat,lon,speed,altitude
     */
    public String toString() {
        return "Loc{sta:" + this.status + ",date:" + this.date + ",lat:" + this.lat + ",lon:" + this.lon + ",spd:" + this.speed + ",ang:" + this.angle + ",alt:" + this.altitude + ",sat:" + this.nbSatellites + ",dop:" + this.dop + "}";
    }

    /**
     * Convert the position to a byte array.
     *
     * These are the possible sizes (in bytes):
     * <ul>
     * <li>1 - No reception, we only have the number of satellites (and no time
     * yet):
     * [1B: nb satellites]</li>
     * <li>5 - No reception, we have the time and the number of satellites:
     * [4B: timestamp], [1B: nb satellites]</li>
     * <li>12 - Reception, but not moving:
     * [4B: timestamp], [4B: latitude (float)], [4B: longitude (float)]</li>
     * <li>14 - Reception and moving:
     * [4B: timestamp], [4B: latitude (float)], [4B: longitude (float)],
     * [2B: speed]</li>
     * </ul>
     *
     * @return Bytes conversion of the current location.
     */
    public byte[] toBytes() {
        if (status == STATUS_OK) {
            byte[] bytes = new byte[speed != 0 ? 14 : 12];

            long time = DateManagement.stringDateToTimestamp(date) / 1000;


            Bytes.longToUInt32Bytes(time, bytes, 0);  // date [0...3]
            Bytes.floatToBytes((float) lat, bytes, 4); // lat [4...7]
            Bytes.floatToBytes((float) lon, bytes, 8); // lon [8...11]
            if (bytes.length == 14) {
                Bytes.intTo2Bytes((int) speed, bytes, 12); // speed [12...13]
            }
            return bytes;
        } else {
            // Maybe we could get the time from the GPS chip
            long time = DateManagement.stringDateToTimestamp(date) / 1000;

            // If we couldn't, maybe we have it some other way
            if (time < DateManagement.THE_PAST) {
                time = DateManagement.time();
            }

            // If we don't have it, it's best to do not report it
            if (time < DateManagement.THE_PAST) {
                byte[] bytes = new byte[1];
                bytes[0] = (byte) nbSatellites;
                return bytes;
            } else {
                byte[] bytes = new byte[5];
                Bytes.longToUInt32Bytes(time, bytes, 0);  // date [0...3]
                bytes[4] = (byte) nbSatellites;
                return bytes;
            }
        }
    }

    /**
     * Distance between two positions
     *
     * @param pos Position to calculate the distance to
     * @return Distance between two positions (in meters)
     */
    public double distanceTo(GpsPosition pos) {
        if ((pos.lat == lat && pos.lon == lon)
                || (lat == 0 && lon == 0)
                || (pos.lat == 0 && pos.lon == 0)) {
            return 0;
        }
        double latA = this.lat * Math.PI / 180,
                lonA = this.lon * Math.PI / 180,
                latB = pos.lat * Math.PI / 180,
                lonB = pos.lon * Math.PI / 180;

        double radius = 6378;

        return 1000 * radius * (Math.PI / 2 - Math2.asin(Math.sin(latB) * Math.sin(latA) + Math.cos(lonB - lonA) * Math.cos(latB) * Math.cos(latA)));
    }

    /**
     * Create a clone of this object
     *
     * @return Clone of this object
     */
    public synchronized GpsPosition clone() {
        return new GpsPosition(this);
    }

    public boolean hasLocation() {
        return status == STATUS_OK;
    }

    public boolean hasSignal() {
        return status != STATUS_NO_SIGNAL;
    }
}
