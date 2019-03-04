package com.liteon.jt808.util;

public class AtCommand {  // Save as HelloJNI.java
   static {
      System.loadLibrary("atcmd"); // Load native library hello.dll (Windows) or libhello.so (Unixes)
                                   //  at runtime
                                   // This library contains a native method called sayHello()
   }
 
   // Declare an instance native method sayHello() which receives no parameter and returns void
   private native static int Ql_SendAT(String atCmd, String finalRsp, char[] strResponse, long timeout_ms);
 
   // Test Driver
   public static void main(String[] args) {
      char[] chars = new char[128];
      //Arrays.fill(chars, 0);
      //String resp = new String(chars);
 //     AtCommand atCommand = new AtCommand();
      Ql_SendAT("ATE0", "OK", chars, 1500);
 //     System.out.println(chars);
      Ql_SendAT("AT+GSN", "OK", chars, 2000);
      System.out.println(chars);
      //new HelloJNI().sayHello();  // Create an instance and invoke the native method
   }
}
