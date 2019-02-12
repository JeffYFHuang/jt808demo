package com.cva.jt808.auth;

import com.cva.jt808.conn.MessageCollector;
import com.cva.jt808.msg.Message;
import com.cva.jt808.msg.ServerGenericReply;
import com.cva.jt808.conn.Connection;
import com.cva.jt808.filter.MessageIdFilter;
import com.cva.jt808.msg.AuthenticateRequest;

public class BasicAuthentication {

  private Connection mConnection;

  public BasicAuthentication(Connection conn) {
    mConnection = conn;
  }

  public boolean authenticate(String auth) {
    MessageCollector collector =
        mConnection.createMessageCollector(new MessageIdFilter(ServerGenericReply.ID));
    AuthenticateRequest request = new AuthenticateRequest.Builder(auth).build();
    // Send the message
    mConnection.sendMessage(request);
    // Wait up to a certain number of seconds for a reply from the server
    Message replyMsg = collector.nextResult(5000L);
    ServerGenericReply reply = new ServerGenericReply.Builder(replyMsg).build();
    if (reply == null) {
      throw new NullPointerException("No reply from the server.");
    }
    // Otherwise, no error so continue processing
    collector.cancel();

    switch (reply.getResult()) {
      case ServerGenericReply.RESULT_OK:
        return true;
      case ServerGenericReply.RESULT_FAIL:
      case ServerGenericReply.RESULT_BAD_REQUEST:
      case ServerGenericReply.RESULT_CONFIRM:
      case ServerGenericReply.RESULT_UNSUPPORTED:
      default:
        return false;
    }
  }

}
