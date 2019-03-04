package com.liteon.jt808.msg;



import com.liteon.javacint.logging.Logger;
import com.liteon.jt808.util.LogUtils;

public abstract class MessageBuilder {

  private static final String TAG = LogUtils.makeTag(MessageBuilder.class);

  // Optional parameters - initialized to default values
  protected byte   cipher = Message.CIPHER_NONE;
//  protected byte[] phone  = Message.EMPTY_PHONE;
  protected byte[] phone = new byte[]{0x08, (byte) 0x91,0x23,0x45,0x67, (byte) 0x89};
  protected byte[] body   = Message.EMPTY_BODY;

  public MessageBuilder cipher(byte cipher) {
    switch (cipher) {
      case Message.CIPHER_NONE:
      case Message.CIPHER_RSA:
        this.cipher = cipher;
        break;
      default:
        Logger.log(TAG + "  cipher: Unknown cipher mode, use default.");
    }

    return this;
  }

  public MessageBuilder phone(byte[] imei) {
    if (phone != null && imei.length == 6) {
      Logger.log("phone set imei");
      System.arraycopy(this.phone, 0, imei, 0, 6);
      //this.phone = imei;
    } else {
      Logger.log(TAG + "  phone: Illegal phone number, use default.");
    }

    return this;
  }

  public abstract Message build();

}
