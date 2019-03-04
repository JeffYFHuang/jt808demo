/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.liteon.jt808.util;

/**
 *
 * @author iasuser
 */
public class AtCommand {  // Save as HelloJNI.java
   static {
      //System.loadLibrary("atcmd"); // Load native library hello.dll (Windows) or libhello.so (Unixes)
                                   //  at runtime
                                   // This library contains a native method called sayHello()
   }
 
   // Declare an instance native method sayHello() which receives no parameter and returns void
   private native int Ql_SendAT(String atCmd, String finalRsp, char[] strResponse, long timeout_ms);
 
   public char[] sendAT(String atCmd, String finalRsp, long timeout_ms){
       char[] strResponse = new char[128];
       AtCommand atCommand = new AtCommand();
       int ret = atCommand.Ql_SendAT(atCmd, finalRsp, strResponse, timeout_ms);
       if(ret == 0)
           return strResponse;
       
       return strResponse;
   }
   // Test Driver
   public static void main(String[] args) {
      char[] chars = new char[128];
      //Arrays.fill(chars, 0);
      //String resp = new String(chars);
      AtCommand atCommand = new AtCommand();
      atCommand.Ql_SendAT("ATE0", "OK", chars, 1500);
      System.out.println(chars);
      atCommand.Ql_SendAT("AT+GSN", "OK", chars, 2000);
      System.out.println(chars);
      //new HelloJNI().sayHello();  // Create an instance and invoke the native method
   }
}