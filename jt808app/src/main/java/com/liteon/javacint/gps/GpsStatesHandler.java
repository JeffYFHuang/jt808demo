package com.liteon.javacint.gps;

/**
 * Handle the GPS chip state change.
 */
public interface GpsStatesHandler {
        /**
         * On startup.
         * This method is called before startup.
         */
	void gpsStart();
	
        /**
         * On shutdown.
         * This method is called before shutdown.
         */
	void gpsStop();
}
