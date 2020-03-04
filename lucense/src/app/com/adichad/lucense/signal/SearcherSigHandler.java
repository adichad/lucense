/**
 * 
 */
package com.adichad.lucense.signal;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.adichad.lucense.resource.SearchResourceManager;

/**
 * @author adichad
 * 
 */
public class SearcherSigHandler implements SignalHandler {
  SignalHandler oldHandler;

  SearchResourceManager searchResourceManager;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  public SearcherSigHandler(SearchResourceManager searchResourceManager) {
    this.searchResourceManager = searchResourceManager;

    this.oldHandler = Signal.handle(new Signal("HUP"), this);
    this.oldHandler = Signal.handle(new Signal("INT"), this);
    this.oldHandler = Signal.handle(new Signal("TERM"), this);
  }

  /*
   * (non-Javadoc)
   * @see sun.misc.SignalHandler#handle(sun.misc.Signal)
   */
  /*
   * 1) SIGHUP 2) SIGINT 3) SIGQUIT 4) SIGILL 5) SIGTRAP 6) SIGABRT 7) SIGBUS 8)
   * SIGFPE 9) SIGKILL 10) SIGUSR1 11) SIGSEGV 12) SIGUSR2 13) SIGPIPE 14)
   * SIGALRM 15) SIGTERM 16) SIGSTKFLT
   */
  @Override
  public void handle(Signal sig) {
    switch (sig.getNumber()) {
    case 1:
      statusLogger.log(Level.INFO, "Caught " + sig.getName());
      break;
    case 2:
      statusLogger.log(Level.INFO, "Caught " + sig.getName());
      this.oldHandler.handle(sig);
      break;
    case 15:
      statusLogger.log(Level.INFO, "Caught " + sig.getName());
      this.oldHandler.handle(sig);
      break;
    default:
      this.oldHandler.handle(sig);
      break;
    }
  }

}
