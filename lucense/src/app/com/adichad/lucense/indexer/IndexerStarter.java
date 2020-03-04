/*******************************************************************************
 * SearchServerStarter.java
 * Threadpooled, socketized, distributed search daemon for search request 
 * processing.
 *
 * ****************************************************************************/

package com.adichad.lucense.indexer;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.mozilla.javascript.Context;

import com.adichad.lucense.resource.IndexerResourceManager;
import com.adichad.lucense.resource.ResourceManagerFactory;
import com.adichad.lucense.signal.IndexerSigHandler;

public class IndexerStarter {

  protected enum ArgumentType {
    PARAM_CONF, PARAM_INDEX, DEFAULT
  }

  protected static boolean runOnConsole;

  protected static String configPath;

  @SuppressWarnings("unused")
  private static IndexerSigHandler sigHandler;

  private static IndexerResourceManager indexerResourceManager;

  private static Set<String> indexes;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  protected static boolean parseCommandLine(String[] args) {
    ArgumentType nextArgType = ArgumentType.DEFAULT;

    runOnConsole = false;

    for (String param : args) {
      if (param.equals("--config")) {
        nextArgType = ArgumentType.PARAM_CONF;
        continue;
      } else if (param.equals("--console")) {
        runOnConsole = true;
        continue;
      } else if (param.equals("--index")) {
        nextArgType = ArgumentType.PARAM_INDEX;
        continue;
      }

      switch (nextArgType) {
      case PARAM_CONF:
        configPath = param;
        break;
      case PARAM_INDEX:
        indexes.add(param);
        break;
      default:
        break;
      }
    }
    if ((configPath == null) || (indexes.size() == 0))
      return false;

    return true;
  }

  public static void main(String[] args) {
    try {
      Context cx = Context.enter();
      StringWriter prehistoricLog = configurePrehistoricLog();

      System.out.println("________________________________\n" + " LuceNSE indexer ver. 0.1\n" + " Adichad Search Team\n"
          + " Rights Unknown. Lefts?\n" + "________________________________");

      indexes = new HashSet<String>();
      if (!parseCommandLine(args)) {
        System.out.println("Usage: indexer --config <config-file path> [--console] --index <index>[ index]*");
        System.exit(0);
      } else {
        indexerResourceManager = ResourceManagerFactory.createIndexerResourceManager(configPath, prehistoricLog, cx);
        if (!runOnConsole) {
          statusLogger.removeAppender(statusLogger.getAppender("stdout"));
          statusLogger.removeAppender(statusLogger.getAppender("stderr"));

          errorLogger.removeAppender(errorLogger.getAppender("stdout"));
          errorLogger.removeAppender(errorLogger.getAppender("stderr"));
        }

        sigHandler = new IndexerSigHandler(indexerResourceManager);

        Set<IndexProcessor> procs = indexerResourceManager.getIndexProcessors(indexes);
        if ((procs != null) && (procs.size() > 0)) {
          for (IndexProcessor proc : procs)
            proc.start();
          for (IndexProcessor proc : procs)
            proc.join();
        } else
          statusLogger.log(Level.INFO, "Nothing to do");
        statusLogger.log(Level.INFO, "Indexing process ended");
      }
    } catch (Exception e) {
      e.printStackTrace();
      errorLogger.log(Level.FATAL, e);
      System.exit(1);
    } finally {
      Context.exit();
    }
  }

  private static StringWriter configurePrehistoricLog() {
    StringWriter prehistoricLog;
    prehistoricLog = new StringWriter();
    Logger logger = Logger.getLogger("org.apache.commons.configuration.ConfigurationUtils");
    logger.removeAllAppenders();
    logger.setLevel(Level.OFF);

    logger = Logger.getLogger("StatusLogger");
    logger.removeAllAppenders();
    logger
        .addAppender(new WriterAppender(new PatternLayout("[%d{yyyy MMM dd EEE HH:mm:ss.SSS}] %m%n"), prehistoricLog));

    logger = Logger.getLogger("ErrorLogger");
    logger.removeAllAppenders();
    logger.addAppender(new WriterAppender(
        new PatternLayout("[%d{yyyy MMM dd EEE HH:mm:ss.SSS}] [%5p] [%C{3}:%L] %m%n"), prehistoricLog));
    return prehistoricLog;
  }

}
/*******************************************************************************
 * IndexerStarter.java ends *
 ***************************************************************************/

