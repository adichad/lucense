/**
 * 
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.RegisterWorkerResult;

/**
 * @author adichad
 */
public class RegisterWorkerRequest extends Request implements Runnable {
  private SearchResourceManager context;

  private boolean closeSock;

  private int workerPort;

  public RegisterWorkerRequest(Socket sock, int version, int id) {
    super(sock, version, id);
  }

  @Override
  protected void readFrom(InputStream in) throws IOException {
    DataInputStream dis = new DataInputStream(in);
    this.workerPort = dis.readInt();
  }

  @Override
  protected void sendTo(OutputStream out) {

  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Request#process(com.adichad.lucense.request
   * .ServerContext, java.util.concurrent.ExecutorService)
   */
  @Override
  public void process(SearchResourceManager context, ExecutorService executor, boolean closeSock) {
    try {
      this.context = context;
      this.closeSock = closeSock;
      executor.submit(this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    try {
      if (this.sock == null) { // this is my request to be registered with
                               // master
        this.sock = new Socket(this.context.getMasterHost(), this.context.getMasterPort());
        sendTo(this.sock.getOutputStream());
        DataInputStream dis = new DataInputStream(this.sock.getInputStream());
        RegisterWorkerResult result = new RegisterWorkerResult(this.id);
        result.readFrom(dis);
      } else { // received request from outside

        /*
         * Context cx = Context.enter(); Scriptable scope =
         * cx.initStandardObjects(); String s =
         * "if (1 == 2) { \"hello!\";} else {\"shello!\";}"; Object val =
         * cx.evaluateString(scope, s, "<cmd>", 1, null); String response =
         * Context.toString(val);
         */
        readFrom(this.sock.getInputStream());
        String host = this.sock.getInetAddress().getHostAddress();
        RegisterWorkerResult result = new RegisterWorkerResult(this.id);
        result.setResponse(this.context.addWorker(host, this.workerPort));
        result.writeTo(this.sock.getOutputStream());
        
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      // Context.exit();
    }
  }
}
