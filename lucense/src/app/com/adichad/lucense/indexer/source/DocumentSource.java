/**
 * 
 */
package com.adichad.lucense.indexer.source;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.indexer.target.IndexingTarget;

/**
 * @author adichad
 * 
 */
public interface DocumentSource {
  public boolean next() throws Exception;

  public void close() throws Exception;

  public void initFieldFactory(IndexingTarget target) throws Exception;

  public Document getDocument(IndexingTarget target, Context cx) throws Exception;

  public void executePostQuery() throws Exception;

  public long getTotalTimeDoc();

  public long getTotalTimeSource();
}
