/*******************************************************************************
 * SearchServerStarter.java Threadpooled, socketized, distributed search daemon
 * for search request processing.
 ****************************************************************************/

package com.adichad.lucense.searchd;

import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.mozilla.javascript.Context;

import com.adichad.lucense.resource.ResourceManagerFactory;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.signal.SearcherSigHandler;

public class SearchServerStarter {

  protected enum ArgumentType {
    PARAM_CONF, PARAM_PORT, DEFAULT
  }

  protected static boolean runOnConsole;

  protected static int portNumber;

  protected static String configPath;

  @SuppressWarnings("unused")
  private static SearcherSigHandler sigHandler;

  private static SearchResourceManager searchResourceManager;

  protected static boolean parseCommandLine(String[] args) {
    ArgumentType nextArgType = ArgumentType.DEFAULT;

    runOnConsole = false;
    portNumber = 0;

    for (String param : args) {
      if (param.equals("--config")) {
        nextArgType = ArgumentType.PARAM_CONF;
        continue;
      } else if (param.equals("--console")) {
        runOnConsole = true;
        continue;
      } else if (param.equals("--port")) {
        nextArgType = ArgumentType.PARAM_PORT;
        continue;
      }

      switch (nextArgType) {
      case PARAM_CONF:
        configPath = param;
        break;
      case PARAM_PORT:
        portNumber = Integer.parseInt(param);
        break;
      default:
        break;
      }
    }
    if (configPath == null)
      return false;

    if (portNumber == 0)
      portNumber = 6000;

    return true;
  }

  public static void main(String[] args) {
    try {
      StringWriter prehistoricLog = configurePrehistoricLog();

      System.out.println("________________________________\n" + " LuceNSE search daemon ver. 0.1\n"
          + " Adichad Search Team\n" + " Rights Unknown. Lefts?\n" + "________________________________");
      if (!parseCommandLine(args)) {
        System.out.println("Usage: searchd --config <config-file path> [--console] [--port <port-number>]");
        System.exit(0);
      } else {
        Context cx = Context.enter();
        cx.setOptimizationLevel(1);
        searchResourceManager = ResourceManagerFactory.createSearchResourceManager(configPath, portNumber,
            prehistoricLog, cx);

        SearchServer server = new SearchServer(searchResourceManager);
        sigHandler = new SearcherSigHandler(searchResourceManager);
        server.start(runOnConsole);
        System.gc();
      }
    } catch (Throwable e) {
      Logger logger = Logger.getLogger("ErrorLogger");
      logger.log(Level.FATAL, e);
      e.printStackTrace();
      Context.exit();
      System.exit(1);
    }
  }

  private static StringWriter configurePrehistoricLog() {
    StringWriter prehistoricLog;
    prehistoricLog = new StringWriter();
    Logger logger = Logger.getRootLogger();
    logger.removeAllAppenders();
    logger
        .addAppender(new WriterAppender(new PatternLayout("[%d{yyyy MMM dd EEE HH:mm:ss.SSS}] %m%n"), prehistoricLog));
    logger.setLevel(Level.ALL);

    logger = Logger.getLogger("StatusLogger");
    logger.removeAllAppenders();
    logger
        .addAppender(new WriterAppender(new PatternLayout("[%d{yyyy MMM dd EEE HH:mm:ss.SSS}] %m%n"), prehistoricLog));
    logger.setLevel(Level.ALL);

    logger = Logger.getLogger("ErrorLogger");
    logger.removeAllAppenders();
    logger.addAppender(new WriterAppender(
        new PatternLayout("[%d{yyyy MMM dd EEE HH:mm:ss.SSS}] [%5p] [%C{3}:%L] %m%n"), prehistoricLog));
    logger.setLevel(Level.ALL);
    return prehistoricLog;
  }
}
/*******************************************************************************
 * SearchServerStart.java ends
 ****************************************************************************/

