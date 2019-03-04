package com.liteon.jt808app;

/**
 * Static constants for this module.
 *
 */
public class ClientConstants {

  public static final String PREF_FILE_NAME = "client_preferences";

  // Preference keys
  public static final String PREF_KEY_HOST      = "host";
  public static final String PREF_KEY_PORT      = "port";
  public static final String PREF_KEY_AUTH_CODE = "auth_code";

  // Preference defaults
//  public static final String PREF_DEFAULT_HOST      = "10.1.5.21";
//  public static final int    PREF_DEFAULT_PORT      = 29930;

  public static final String PREF_DEFAULT_HOST      = "localhost";
  public static final int    PREF_DEFAULT_PORT      = 5555;
  
  //for car simulation
  public static final int    CAR_AVG_SPEED          = 30;               // knots/hr
  public static final int    RESET_ODOMETER         = 2000;             // 2000 kms
  public static final double CAR_FUEL_FULL          = 55000.0;           // ml
  public static final double CAR_AVG_FUEL_CONSUMPTION = 10000.0; // meters/l
}
