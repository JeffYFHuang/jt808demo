package com.liteon.jt808.conn;



import com.liteon.javacint.logging.Logger;
import com.liteon.jt808.conn.Connection.ListenerWrapper;
import com.liteon.jt808.msg.Message;
import com.liteon.jt808.msg.Packet;
import com.liteon.jt808.util.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listens for packet traffic from the JT/T808 server and parse it into message objects.
 * <p>
 * The message reader also invokes all message listeners and collectors.
 *
 */
class MessageReader {

  private static final String TAG = LogUtils.makeTag(MessageReader.class);

  private Connection      mConnection;
  private InputStream     mInput;
  private Thread          mReadThread;
  private ExecutorService mExecutor;

  private boolean mDone;

  /**
   * Creates a new message reader with the specified connection.
   *
   * @param conn the connection
   */
  MessageReader(Connection conn) {
    mConnection = conn;
    init();
  }

  /**
   * Initializes the reader in order to be used. The reader is initialized during the first
   * connection and when reconnecting due to an abruptly disconnection.
   */
  void init() {
    mDone = false;
    mInput = mConnection.getInput();

    mReadThread = new ReadThread();
    // TODO: 10/24/2016 add connection count to the name
    mReadThread.setName("Pigeon Message Reader ( )");
    mReadThread.setDaemon(true);

    // Create an executor to deliver incoming messages to listeners. We'll use a single thread with
    // an unbounded queue
    mExecutor = Executors.newSingleThreadExecutor();
  }

  /** Starts the packet read thread. */
  public synchronized void startup() {
    mReadThread.start();
  }

  /** Shuts the message reader down. */
  public void shutdown() {
    Logger.log(TAG + "  shutdown reader");
    mDone = true;
  }

  /** Parses packets in order to process them further. */
  private void readPackets() {
    try {
      byte[] buf = new byte[128];
      int len;
      while (!mDone && (len = mInput.read(buf)) != -1) {
        if (len > 0) {
          /*for (byte lbyte : buf) {
              System.out.printf("%02X", lbyte);
          }
          Logger.log("");*/
          byte[] raw = new byte[len];
          System.arraycopy(buf, 0, raw, 0, len);
          Packet packet = null;
          try {
            packet = new Packet(raw);
          } catch (IllegalArgumentException e) {
            Logger.log("Packet read exception! " + e);
            continue;
          }
          Logger.log(TAG + "  readPackets: " + packet);
          Message msg = new Message.Builder(packet).build();
          processMessage(msg);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();

      //TODO   this solution is not good
      mConnection.shutDown();
    } finally {

    }
  }

  /**
   * Processes a message after it's been fully parsed by looping through the installed message
   * collectors and listeners and letting them examine the message to see if they are a match with
   * the filter.
   *
   * @param msg the message to process
   */
  private void processMessage(Message msg) {
    if (msg == null) {
      return;
    }

    // Loop through all collectors and notify the appropriate ones.
    for (MessageCollector collector : mConnection.getCollectors()) {
      collector.processMessage(msg);
    }

    // Deliver the incoming message to listeners
    mExecutor.submit(new ListenerNotification(msg));
  }

  /** A thread to read packets from the connection. */
  private class ReadThread extends Thread {

    @Override
    public void run() {
      super.run();
      readPackets();
    }

  }

  /** A runnable to notify all listeners of a message. */
  private class ListenerNotification implements Runnable {

    private Message message;

    public ListenerNotification(Message msg) {
      this.message = msg;
    }

    @Override
    public void run() {
      for (ListenerWrapper wrapper : mConnection.getRcvListeners().values()) {
        wrapper.notifyListener(this.message);
      }
    }

  }

}
