package com.liteon.jt808.util;

import com.liteon.javacint.common.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by DeW on 2017/3/31.
 */

public class DataUtils {

    private static final String imeifile = "/data/imei";

    /**
     * transfer manufacturer id into byte[5];
     * @param id
     * @return
     */
    public static byte[] getPhoneBytes(long id){
        if(id > 1099511627775L){
            throw new IllegalArgumentException("id is bigger than 1099511627775L");
        }
        byte[] idBytes = new byte[5];
        idBytes[0] = (byte) ((id>>32) & 0xFF);
        idBytes[1] = (byte) ((id>>24) & 0xFF);
        idBytes[2] = (byte) ((id>>16) & 0xFF);
        idBytes[3] = (byte) ((id>>8) & 0xFF);
        idBytes[4] = (byte) (id & 0xFF);
        return idBytes;
    }

    public static byte[] readImei()
    {
       String sCurrentLine = "";
       final File initialFile = new File(imeifile);
       byte[] buf;
       byte[] imei = new byte[6];
               
       try {
            final InputStream targetStream = 
            new DataInputStream(new FileInputStream(initialFile));
            BufferedReader reader;
            reader = new BufferedReader(targetStream);
	    while ((sCurrentLine = reader.readLine()) != null) {
                if (sCurrentLine.length()>=12)
                {
                   buf = BCD8421Operater.string2Bcd(sCurrentLine.substring(3, 15));
                   System.arraycopy(buf, 0, imei, 0, 6);
                   for(byte b:imei) {
                      System.out.printf("%02x", b);
                   }
                   System.out.println("");
                   break;
                }
	    }

	} catch (IOException e) {
	     e.printStackTrace();
	}
        return imei;
    }

    public static byte[] readImeiAt()
    {
        byte[] buf;
        byte[] imei = new byte[6];
        char[] chars;

        AtCommand atCommand = new AtCommand();
        chars = atCommand.sendAT("AT+GSN", "OK", 1000);
        if (chars.length > 12) {
           String str = new String(chars);
           str = str.replaceAll("[^0-9.]", "");
           System.out.println(str);
           buf = BCD8421Operater.string2Bcd(str.substring(3, 15));
           System.arraycopy(buf, 0, imei, 0, 6);
           for (byte b:imei) {
              System.out.printf("%02x", b);
           }
           System.out.println("");
        }

        return imei;
    }
}
