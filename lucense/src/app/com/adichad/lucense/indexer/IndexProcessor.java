/**
 * 
 */
package com.adichad.lucense.indexer;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.mozilla.javascript.Context;

import com.adichad.lucense.indexer.source.DocumentSource;
import com.adichad.lucense.indexer.target.IndexingTarget;

/**
 * @author adichad
 */
public class IndexProcessor implements Runnable {
  DocumentSource source;

  IndexWriter writer;

  private String targetNames;

  private Set<IndexingTarget> targets;

  private static Logger statusLogger = Logger.getLogger("StatusLogger");

  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  private Thread t;

  public IndexProcessor(DocumentSource documentSource, Set<IndexingTarget> targets) {
    this.source = documentSource;
    this.targets = targets;
    this.targetNames = new String();
    for (IndexingTarget target : targets) {
      this.targetNames = this.targetNames + " " + target.getName();
    }
  }

  public void start() {
    this.t = new Thread(this);
    this.t.start();
  }

  public void join() throws InterruptedException {
    this.t.join();
  }

  @Override
  public void run() {
    try {
      Context cx = Context.enter();
      statusLogger.log(Level.INFO, "Starting indexing for index(es): " + this.targetNames);
      for (IndexingTarget target : this.targets) {
        target.addContext(cx);
        source.initFieldFactory(target);
      }

      statusLogger.log(Level.INFO, "Initialized fields");
      while (this.source.next()) {
        for (IndexingTarget target : this.targets) {
          this.source.getDocument(target, cx);
          target.addDocument(cx);
        }
      }
      statusLogger.log(Level.DEBUG, "Total time (ms) creating docs: " + this.source.getTotalTimeDoc());
      statusLogger.log(Level.DEBUG, "Total time (ms) reading source: " + this.source.getTotalTimeSource());

      for (IndexingTarget target : this.targets) {
        statusLogger.log(Level.DEBUG,
            "Total time (ms) @target [" + target.getName() + "] adding docs: " + target.getTotalTime());
        statusLogger.log(Level.INFO,
            target.getAddedCount() + " document(s) added/updated to index: " + target.getName());
        statusLogger.log(Level.INFO, target.getDeletedCount() + " document(s) deleted from index: " + target.getName());
        target.optimize();
      }
      this.source.executePostQuery();
      statusLogger.log(Level.INFO, "Source [" + this.source.toString() + "] indexed successfully");
    } catch (CorruptIndexException e) {
      errorLogger.log(Level.ERROR, e);
    } catch (IOException e) {
      errorLogger.log(Level.ERROR, e);
    } catch (Exception e) {
      errorLogger.log(Level.ERROR, e);
      e.printStackTrace();
    } finally {
      try {
        for (IndexingTarget target : this.targets) {
          target.close();
        }
      } catch (Exception e) {
        errorLogger.log(Level.ERROR, e);
        e.printStackTrace();
      }
      Context.exit();
    }
  }
}
