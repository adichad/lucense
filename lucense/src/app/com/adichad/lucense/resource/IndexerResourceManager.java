/**
 * 
 */
package com.adichad.lucense.resource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.index.CorruptIndexException;

import com.adichad.lucense.indexer.IndexProcessor;
import com.adichad.lucense.indexer.source.DataSource;
import com.adichad.lucense.indexer.target.IndexingTarget;
import com.adichad.lucense.indexer.target.SettableIndexingTarget;

/**
 * @author adichad
 * 
 */
public class IndexerResourceManager {

  private Map<String, DataSource> namedDataSources;

  private HashMap<String, SettableIndexingTarget> namedIndexes;

  private List<IndexingTarget> indexesToClose;

//  private IndexerResourceManager old;
  private HashMap<String, SettableIndexingTarget> newNamedIndexes;

  // private static Logger statusLogger = Logger
  // .getLogger("StatusLogger");
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");

  public IndexerResourceManager(Map<String, DataSource> namedDataSources,
      HashMap<String, SettableIndexingTarget> namedIndexes) {
    this.namedDataSources = namedDataSources;
    this.namedIndexes = namedIndexes;
    this.newNamedIndexes = new HashMap<String, SettableIndexingTarget>();
    this.indexesToClose = new LinkedList<IndexingTarget>();
  }

  public IndexingTarget getIndexingTarget(String indexName) {
    return this.namedIndexes.get(indexName);
  }

  public void closeIndexingTarget(String indexName) {
    if(newNamedIndexes.containsKey(indexName)) {
      this.namedIndexes.put(indexName, newNamedIndexes.get(indexName)); 
      newNamedIndexes.remove(indexName);
    } 
  }
  
  public void closeAll() throws CorruptIndexException, IOException {
    for (IndexingTarget it : indexesToClose) {
      it.close();
    }
    indexesToClose.clear();
  }

  public void setOld(IndexerResourceManager old) {
    for (String indexName : this.namedIndexes.keySet()) {
      if (old.namedIndexes.containsKey(indexName)) {
        newNamedIndexes.put(indexName, this.namedIndexes.get(indexName));
        this.namedIndexes.put(indexName, old.namedIndexes.get(indexName));
      }
    }
      
    
    for (String indexName : old.namedIndexes.keySet()) {
      if (!this.namedIndexes.containsKey(indexName)) {
        this.indexesToClose.add(old.namedIndexes.get(indexName));
        
      } 
    }
    
    
  }

  public Set<IndexProcessor> getIndexProcessors(Set<String> indexNames) {
    HashMap<DataSource, Set<IndexingTarget>> procDefinition = new HashMap<DataSource, Set<IndexingTarget>>();

    Set<IndexProcessor> procs = new HashSet<IndexProcessor>();

    for (String indexName : indexNames) {
      IndexingTarget idxdef = this.namedIndexes.get(indexName);
      if (!this.namedDataSources.containsKey(idxdef.getDataSource())) {
        continue;
      }
      DataSource ds = this.namedDataSources.get(idxdef.getDataSource());
      if (!procDefinition.containsKey(ds)) {
        procDefinition.put(ds, new HashSet<IndexingTarget>());
      }
      ((SettableIndexingTarget) idxdef).setIdField(ds.getIdField());
      procDefinition.get(ds).add(idxdef);
    }
    for (DataSource ds : procDefinition.keySet()) {
      try {
        procs.add(new IndexProcessor(ds.getDocumentSource(), procDefinition.get(ds)));
      } catch (SQLException e) {
        errorLogger.log(Level.ERROR, e);
        e.printStackTrace();
      } catch (FileNotFoundException e) {
        errorLogger.log(Level.ERROR, e);
      } catch (Exception e) {
        errorLogger.log(Level.ERROR, e);
        e.printStackTrace();
      }
    }
    return procs;
  }

}
