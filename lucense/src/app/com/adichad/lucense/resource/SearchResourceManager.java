/**
 * 
 */
package com.adichad.lucense.resource;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.HighPriorityTimeLimitingCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermDocsFactoryBuilder;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.NamedThreadFactory;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.browseengine.bobo.api.BoboBrowser;
import com.browseengine.bobo.api.BoboIndexReader;
import com.browseengine.bobo.api.Browsable;

import com.adichad.lucense.analysis.spelling.SpellingCorrector;
import com.adichad.lucense.bitmap.AuxIndexManager;
import com.adichad.lucense.connection.ServerConnectionManager;
import com.adichad.lucense.exception.NoValidIndexesFoundException;
import com.adichad.lucense.exception.TooManyRequestsException;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.grouping.Grouper;
import com.adichad.lucense.grouping.GrouperFactory;
import com.adichad.lucense.indexer.target.IndexingTarget;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.request.Request.ScorerType;
import com.adichad.lucense.request.RequestFactory.RequestType;
import com.adichad.lucense.searchd.SearchServer;
import com.adichad.lucense.searchd.ServerStatus;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author adichad
 */
public class SearchResourceManager {
  private String threadPrefix;

  private Map<String, Set<IndexReader>> namedReaders;

  private HashMap<String, Map<String, GrouperFactory>> schemaGrouperFactoryMap;

  private Map<String, Float> readerBoosts;

  private ServerConnectionManager serverConnectionManager;

  private int maxConcurrancy;

  // private int maxQueueSize;
  private long totalSearchRequests;

  // private String masterHost;
  // private int masterPort;
  private final int serverPort;

  private Map<RequestType, Set<String>> authorizationMap;

  private int currConcurrancy;

  private HashMap<String, String> indexnameSchemaMap;

  private Map<String, Map<String, Analyzer>> schemaAnalyzerMap;

  private final Map<Set<IndexReader>, IndexSearcher> searcherMap;

  private Date startTime;

  // private Map<Searcher, IndexReader> readerMap;
  private long maxSearchTimeout;

  private Map<String, Scriptable> scopeMap;

  private HashMap<String, HashSet<String>> schemaExpressionFieldMap;

  private Map<String, Set<String>> schemaIndexNameMap;

  private HashMap<String, SpellingCorrector> spellingCorrectors;

  private final Map<Set<IndexReader>, Browsable> browserMap;

  private Map<String, String> schemaFacetConfigMap;

  private Map<String, String> indexWriteConfigMap;

  private Map<String, IndexerResourceManager> schemaIRMMap;

  private int counter;

  private boolean closeNotInProgress = true;

  private boolean writable = true;

  private Map<String, Map<String, TermDocsFactoryBuilder>> analyzerTDFBMap;

  private final ExecutorService threadPool;

  private HashMap<String, AuxIndexManagerConfig> auxIndexManagerConfigMap;

  private SearchServer searchServer;

  private String configPath;

  private Map<String, Set<IndexReaderConfig>> readerConfigMap;

  private int masterPort;

  private String masterHost;

  private int maxQueueSize;

  private Map<String, AuxIndexManager> auxIndexManagerMap;

  private SearchResourceManager oldSRM;

  private IndexReaderGrimReaper readerKiller;

  

  private static final Logger errorLogger = Logger.getLogger("ErrorLogger");
  private static final Logger statusLogger = Logger.getLogger("StatusLogger");
  
  public SearchResourceManager(Map<String, Set<IndexReaderConfig>> readerConfigMap, Map<String, Float> readerBoosts,
      HashMap<String, String> indexnameSchemaMap, Map<String, Set<String>> schemaIndexNameMap,
      Map<String, String> schemaFacetConfigMap, Map<String, Map<String, Analyzer>> schemaAnalyzerMap,
      Map<String, Map<String, TermDocsFactoryBuilder>> analyzerTDFBMap,
      HashMap<String, Map<String, GrouperFactory>> schemaGrouperFactoryMap,
      HashMap<String, HashSet<String>> schemaExpressionFieldMap, Map<String, IndexerResourceManager> schemaIRMMap,
      Map<String, String> indexWriteConfigMap, HashMap<String, SpellingCorrector> spellingCorrectors,
      ServerConnectionManager serverConnectionManager, Map<RequestType, Set<String>> authorizationMap2,
      Map<String, Scriptable> scopeMap, int maxConcurrancy, int maxQueueSize, long maxSearchTimeout, String masterHost,
      int masterPort, int serverPort, HashMap<String, AuxIndexManagerConfig> auxIndexManagerConfigMap, String configPath)
      throws Exception {
    this.startTime = new Date();
    this.namedReaders = new HashMap<String, Set<IndexReader>>();
    this.readerConfigMap = readerConfigMap;
    this.indexnameSchemaMap = indexnameSchemaMap;
    
    for(String indexName: readerConfigMap.keySet()) {
      Set<IndexReaderConfig> confs = readerConfigMap.get(indexName);
      Set<IndexReader> readers = new HashSet<IndexReader>(confs.size());
      for(IndexReaderConfig conf: confs) {
        readers.add(conf.getReader());
      }
      namedReaders.put(indexName, readers);
      readerBoosts.put(readers.iterator().next().directory().toString(), readerBoosts.get(indexName));
      readerBoosts.remove(indexName);
      
      if(!this.namedReaders.containsKey(this.indexnameSchemaMap.get(indexName))) {
        namedReaders.put(this.indexnameSchemaMap.get(indexName), new HashSet<IndexReader>());
      }
      namedReaders.get(this.indexnameSchemaMap.get(indexName)).addAll(readers);
    }
    
    this.readerBoosts = readerBoosts;
    this.authorizationMap = authorizationMap2;
    this.scopeMap = scopeMap;
    this.schemaExpressionFieldMap = schemaExpressionFieldMap;
    this.spellingCorrectors = spellingCorrectors;

    this.schemaGrouperFactoryMap = schemaGrouperFactoryMap;

    if (this.namedReaders.isEmpty()) {
      throw new NoValidIndexesFoundException();
    }
    this.searcherMap = new HashMap<Set<IndexReader>, IndexSearcher>();
    this.browserMap = new HashMap<Set<IndexReader>, Browsable>();
    // this.readerMap = new HashMap<Searcher, IndexReader>();
    
    this.schemaIndexNameMap = schemaIndexNameMap;
    this.schemaFacetConfigMap = schemaFacetConfigMap;
    this.schemaAnalyzerMap = schemaAnalyzerMap;
    this.serverConnectionManager = serverConnectionManager;
    if (this.serverConnectionManager == null) {
      throw new IOException("connection settings not defined, cannot daemonize.");
    }

    this.maxConcurrancy = maxConcurrancy;
    this.maxQueueSize = maxQueueSize;
    this.maxSearchTimeout = maxSearchTimeout;
    this.totalSearchRequests = 0;
    this.masterHost = masterHost;
    this.masterPort = masterPort;
    this.serverPort = serverPort;
    this.threadPrefix = "lucense" + this.serverPort;
    
    this.schemaIRMMap = schemaIRMMap;
    this.indexWriteConfigMap = indexWriteConfigMap;
    this.analyzerTDFBMap = analyzerTDFBMap;
    this.threadPool = Executors.newCachedThreadPool(new NamedThreadFactory(threadPrefix));
    HighPriorityTimeLimitingCollector.setResolution(1000);
    this.auxIndexManagerMap = new HashMap<String, AuxIndexManager>(auxIndexManagerConfigMap.size());
    this.auxIndexManagerConfigMap = auxIndexManagerConfigMap;
    for(String indexName: auxIndexManagerConfigMap.keySet()) {
      auxIndexManagerMap.put(indexName, auxIndexManagerConfigMap.get(indexName).getAuxIndexManager());
    }
    this.configPath = configPath;
  }
  
  public SearchResourceManager(Map<String, Set<IndexReaderConfig>> readerConfigMap, Map<String, Float> readerBoosts,
      HashMap<String, String> indexnameSchemaMap, Map<String, Set<String>> schemaIndexNameMap,
      Map<String, String> schemaFacetConfigMap, Map<String, Map<String, Analyzer>> schemaAnalyzerMap,
      Map<String, Map<String, TermDocsFactoryBuilder>> analyzerTDFBMap,
      HashMap<String, Map<String, GrouperFactory>> schemaGrouperFactoryMap,
      HashMap<String, HashSet<String>> schemaExpressionFieldMap, Map<String, IndexerResourceManager> schemaIRMMap,
      Map<String, String> indexWriteConfigMap, HashMap<String, SpellingCorrector> spellingCorrectors,
      ServerConnectionManager serverConnectionManager, Map<RequestType, Set<String>> authorizationMap2,
      Map<String, Scriptable> scopeMap, int maxConcurrancy, int maxQueueSize, long maxSearchTimeout, String masterHost,
      int masterPort, int serverPort, HashMap<String, AuxIndexManagerConfig> auxIndexManagerConfigMap, String configPath, SearchResourceManager old)
  //auxIndexManagerMap, scopeMap, schemaIRMMap
      throws Exception {
    this(old);
    this.merge(readerConfigMap, readerBoosts,
        indexnameSchemaMap, schemaIndexNameMap, schemaFacetConfigMap, schemaAnalyzerMap,
        analyzerTDFBMap, schemaGrouperFactoryMap, schemaExpressionFieldMap, schemaIRMMap,
        indexWriteConfigMap, spellingCorrectors, serverConnectionManager, authorizationMap2,
        scopeMap, maxConcurrancy, maxQueueSize, maxSearchTimeout, masterHost, masterPort,
        serverPort, auxIndexManagerConfigMap);
  }
  
  private void merge(Map<String, Set<IndexReaderConfig>> readerConfigMap2, Map<String, Float> readerBoosts2,
      HashMap<String, String> indexnameSchemaMap2, Map<String, Set<String>> schemaIndexNameMap2,
      Map<String, String> schemaFacetConfigMap2, Map<String, Map<String, Analyzer>> schemaAnalyzerMap2,
      Map<String, Map<String, TermDocsFactoryBuilder>> analyzerTDFBMap2,
      HashMap<String, Map<String, GrouperFactory>> schemaGrouperFactoryMap2,
      HashMap<String, HashSet<String>> schemaExpressionFieldMap2, Map<String, IndexerResourceManager> schemaIRMMap2,
      Map<String, String> indexWriteConfigMap2, HashMap<String, SpellingCorrector> spellingCorrectors2,
      ServerConnectionManager serverConnectionManager2, Map<RequestType, Set<String>> authorizationMap2,
      Map<String, Scriptable> scopeMap2, int maxConcurrancy2, int maxQueueSize2, long maxSearchTimeout2,
      String masterHost2, int masterPort2, int serverPort2, HashMap<String, AuxIndexManagerConfig> auxIndexManagerConfigMap2) throws Exception {
    
    List<IndexReader> readersToClose = new LinkedList<IndexReader>();
    this.readerConfigMap = readerConfigMap2;
    for(String indexName: readerConfigMap2.keySet()) {
      if(this.namedReaders.containsKey(indexName)) {
        this.namedReaders.remove(this.indexnameSchemaMap.get(indexName));
        if(!this.readerConfigMap.get(indexName).equals(readerConfigMap2.get(indexName))) {
          Set<IndexReader> currReaders = this.namedReaders.get(indexName);
          readersToClose.addAll(currReaders);
          currReaders.clear();
          currReaders.add(readerConfigMap2.get(indexName).iterator().next().getReader());
        }
      } else {
        Set<IndexReader> readers = new HashSet<IndexReader>();
        readers.add(readerConfigMap2.get(indexName).iterator().next().getReader());
        this.namedReaders.put(indexName, readers);
      }
      readerBoosts2.put(namedReaders.get(indexName).iterator().next().directory().toString(), readerBoosts2.get(indexName));
      readerBoosts2.remove(indexName);
    }
    Map<String, Set<IndexReader>> closeReaderMap = new HashMap<String, Set<IndexReader>>();
    for(String oldName: this.namedReaders.keySet()) {
      if(!readerConfigMap2.containsKey(oldName)) {
        closeReaderMap.put(oldName, namedReaders.get(oldName));
      }
    }
    this.namedReaders.keySet().removeAll(closeReaderMap.keySet());
    for(Set<IndexReader> rs: closeReaderMap.values()) {
      readersToClose.addAll(rs);
    }
    
    for(String indexName: indexnameSchemaMap2.keySet()) {
      String schemaName = indexnameSchemaMap2.get(indexName);
      if(!namedReaders.containsKey(schemaName)) {
        namedReaders.put(schemaName, new HashSet<IndexReader>());
      }
      namedReaders.get(schemaName).addAll(namedReaders.get(indexName));
    }
    //TODO: schedule all readers in readersToClose to be closed.
    this.readerKiller = new IndexReaderGrimReaper(closeReaderMap, oldSRM.searcherMap, oldSRM.browserMap, oldSRM.maxSearchTimeout);
    this.oldSRM = null;
    
    
    //LinkedList<AuxIndexManager> auxIndexesToClose = new LinkedList<AuxIndexManager>();
    for(String indexName: auxIndexManagerConfigMap2.keySet()) {
      if(this.auxIndexManagerMap.containsKey(indexName)) {
        if(!this.auxIndexManagerConfigMap.get(indexName).equals(auxIndexManagerConfigMap2.get(indexName))) {
          
          this.auxIndexManagerMap.put(indexName, auxIndexManagerConfigMap2.get(indexName).getAuxIndexManager());
        }
      } else {
        this.auxIndexManagerMap.put(indexName, auxIndexManagerConfigMap2.get(indexName).getAuxIndexManager());
      }
    }
    
    
    
    for(String schemaName: schemaIRMMap2.keySet()) {
      if(this.schemaIRMMap.containsKey(schemaName)) {
        schemaIRMMap2.get(schemaName).setOld(this.schemaIRMMap.get(schemaName));
      }
    }
    
    for(String schemaName: this.schemaIRMMap.keySet()) {
      if(!schemaIRMMap2.containsKey(schemaName)) {
        schemaIRMMap2.put(schemaName, this.schemaIRMMap.get(schemaName));
        Set<String> indexNames = this.schemaIndexNameMap.get(schemaName);
        for(String indexName: indexNames) {
          indexnameSchemaMap2.put(indexName, schemaName);
        }
      }
    }
    
    this.schemaIRMMap = schemaIRMMap2;
    this.auxIndexManagerConfigMap = auxIndexManagerConfigMap2;
    this.schemaFacetConfigMap = schemaFacetConfigMap2;
    this.schemaExpressionFieldMap = schemaExpressionFieldMap2;
    this.schemaAnalyzerMap = schemaAnalyzerMap2;
    this.maxSearchTimeout = maxSearchTimeout2;
    this.maxConcurrancy = maxConcurrancy2;
    this.indexWriteConfigMap = indexWriteConfigMap2;
    this.analyzerTDFBMap = analyzerTDFBMap2;
    this.readerBoosts = readerBoosts2;
    
    this.scopeMap = scopeMap2;
    this.serverConnectionManager = serverConnectionManager2;
    this.authorizationMap = authorizationMap2;
    this.indexnameSchemaMap = indexnameSchemaMap2;
    this.schemaIndexNameMap = schemaIndexNameMap2;
    this.schemaGrouperFactoryMap = schemaGrouperFactoryMap2;
    this.threadPrefix = "lucense" + this.serverPort;
    this.spellingCorrectors = spellingCorrectors2;//TODO:do we need to safecopy this?
  }

  private SearchResourceManager(SearchResourceManager old) {//shallow copy
    this.serverPort = old.serverPort;
    this.threadPool = old.threadPool;
    this.totalSearchRequests = old.totalSearchRequests;//?
    this.startTime = old.startTime;
    
    this.searcherMap = new HashMap<Set<IndexReader>, IndexSearcher>();
    this.browserMap = new HashMap<Set<IndexReader>, Browsable>();
    this.namedReaders = new HashMap<String, Set<IndexReader>>(old.namedReaders.size());
    this.namedReaders.putAll(old.namedReaders);
    this.auxIndexManagerMap = new HashMap<String, AuxIndexManager>(old.auxIndexManagerMap.size());
    this.auxIndexManagerMap.putAll(old.auxIndexManagerMap);
    this.schemaIRMMap = new HashMap<String, IndexerResourceManager>(old.schemaIRMMap.size());
    this.schemaIRMMap.putAll(old.schemaIRMMap);
    this.indexnameSchemaMap = old.indexnameSchemaMap;
    this.configPath = old.configPath;
    
    this.oldSRM = old;
  }
  
  public void openWriter(String indexName) throws CorruptIndexException, LockObtainFailedException, IOException {
    String idxconf = this.indexWriteConfigMap.get(indexName);
    String schema = indexnameSchemaMap.get(indexName);
    IndexerResourceManager irm = schemaIRMMap.get(schema);
    IndexingTarget it = irm.getIndexingTarget(idxconf);
    if (it == null) {
      throw new IOException("Index " + indexName + " not configured for writing");
    }
    synchronized (it) {
      if (writable) {
        if (!it.isOpen()) {
          it.init();
          closeNotInProgress = true;
        } else {
          throw new IOException("Writer for " + indexName + " already open");
        }
      } else {
        throw new IOException("Writer for " + indexName + " not in writable state");
      }
    }
  }

  public void closeWriter(String indexName, boolean optimize) throws Throwable {
    String idxconf = this.indexWriteConfigMap.get(indexName);
    String schema = indexnameSchemaMap.get(indexName);
    IndexerResourceManager irm = schemaIRMMap.get(schema);
    IndexingTarget it = irm.getIndexingTarget(idxconf);
    if (it == null) {
      throw new IOException("Index " + indexName + " not configured for writing");
    }
    synchronized (it) {
      if (it.isOpen()) {
        if (closeNotInProgress) {
          writable = false;
          closeNotInProgress = false;
        } else {
          throw new IOException("Writer for index " + indexName + " close in progress");
        }
      } else {
        throw new IOException("Writer for index " + indexName + " already closed");
      }
    }
    while (counter > 0) {
      try {
        it.wait();
      } catch (InterruptedException e) {
      }
    }
    try {
      if (optimize)
        it.optimize();
      it.close();
      irm.closeIndexingTarget(indexName);
    } finally {
      synchronized (it) {
        writable = true;
        closeNotInProgress = true;
      }
    }
  }

  public void safeIndexWriteAction(String indexName, IndexWriteAction action, boolean commit) throws Throwable {
    String idxconf = this.indexWriteConfigMap.get(indexName);
    String schema = indexnameSchemaMap.get(indexName);
    IndexerResourceManager irm = schemaIRMMap.get(schema);
    IndexingTarget it = irm.getIndexingTarget(idxconf);
    if (it == null) {
      throw new IOException("Index " + indexName + " not configured for writing");
    }

    synchronized (it) {
      if (!writable) {
        throw new IOException("Writer for index " + indexName + " not writable");
      } else if (!it.isOpen()) {
        if (closeNotInProgress) {
          it.init();
        } else {
          throw new IOException("Writer for index " + indexName + " not open and close in progress");
        }
      }
      ++counter;
    }
    try {
      action.execute(it);
    } finally {
      synchronized (it) {
        --counter;
        it.notifyAll();
      }
    }
    if (commit)
      it.commit();
  }

  public void deleteDocumentsInIndex(String indexName, Query query, boolean commit) throws Throwable {
    IndexWriteAction action = new IndexWriteAction.DeleteDocumentsAction(query);
    this.safeIndexWriteAction(indexName, action, commit);
  }

  public void replaceDocumentsInIndex(String indexName, List<Map<String, String>> docs, boolean commit, Context cx)
      throws Throwable {
    IndexWriteAction action = new IndexWriteAction.ReplaceDocumentsAction(cx, docs);
    this.safeIndexWriteAction(indexName, action, commit);
  }

  public boolean addWorker(String host, int port) {
    // TODO Auto-generated method stub
    return true;
  }

  public int getMasterPort() {
    // TODO Auto-generated method stub
    return 0;
  }

  public String getMasterHost() {
    // TODO Auto-generated method stub
    return null;
  }

  public ServerConnectionManager getConnectionManager() {
    return this.serverConnectionManager;
  }

  public synchronized boolean checkIncrementConcurrancy() {
    if (this.currConcurrancy < this.maxConcurrancy) {
      ++this.currConcurrancy;
      ++this.totalSearchRequests;
      return true;
    }
    return false;
  }

  public IndexSearcher getSearcher(List<String> indexes) throws IOException, TooManyRequestsException,
      NoValidIndexesFoundException {
    if (checkIncrementConcurrancy()) {
      HashSet<IndexReader> readers = new HashSet<IndexReader>(indexes.size());
      for (String index : indexes) {
        if (this.namedReaders.containsKey(index))
          readers.addAll(this.namedReaders.get(index));
        else
          errorLogger.log(Level.WARN, "Unknown Index: " + index);
      }
      if (readers.isEmpty())
        throw new NoValidIndexesFoundException();

      if (!this.searcherMap.containsKey(readers)) {
        synchronized (this.searcherMap) {
          if (!this.searcherMap.containsKey(readers)) {
            IndexSearcher searcher;
            if (readers.size() == 1) {
              searcher = new IndexSearcher(readers.iterator().next());
            } else {
              // Searcher[] searchers = new Searcher[readers.size()];
              MultiReader r = new MultiReader(readers.toArray(new IndexReader[readers.size()]));
              searcher = new IndexSearcher(r, threadPool);
            }
            this.searcherMap.put(readers, searcher);
          }
        }
      }
      return this.searcherMap.get(readers);
    } else
      throw new TooManyRequestsException();
  }

  public Browsable getBrowser(List<String> indexes) throws IOException, NoValidIndexesFoundException {
    HashSet<IndexReader> readers = new HashSet<IndexReader>();

    for (String index : indexes) {
      if (this.namedReaders.containsKey(index)) {
        readers.addAll(this.namedReaders.get(index));
      } else {
        errorLogger.log(Level.WARN, "Unknown Index: " + index);
      }
    }
    if (readers.isEmpty()) {
      throw new NoValidIndexesFoundException();
    }
    if (!this.browserMap.containsKey(readers)) {
      synchronized (this.browserMap) {
        if (!this.browserMap.containsKey(readers)) {
          IndexReader reader;
          if (readers.size() == 1)
            reader = readers.iterator().next();
          else
            reader = new MultiReader(readers.toArray(new IndexReader[0]));
          this.browserMap.put(readers, new BoboBrowser(BoboIndexReader.getInstance(reader)));
        }
      }
    }
    return this.browserMap.get(readers);
  }

  public SpellingCorrector getSpellingCorrector(String name) {
    return this.spellingCorrectors.get(name);
  }

  public Set<Analyzer> getAnalyzers(List<String> indexes) throws NoValidIndexesFoundException {
    return getAnalyzers(indexes, "default");
  }

  public Set<Analyzer> getAnalyzers(List<String> indexes, String analyzerName) throws NoValidIndexesFoundException {
    HashSet<Analyzer> analyzers = new HashSet<Analyzer>();

    for (String index : indexes) {
      if (this.indexnameSchemaMap.containsKey(index)) {
        String schemaName = this.indexnameSchemaMap.get(index);
        if (this.schemaAnalyzerMap.containsKey(schemaName)) {
          Map<String, Analyzer> namedAnalyzers = this.schemaAnalyzerMap.get(schemaName);
          if (!namedAnalyzers.containsKey(analyzerName))
            throw new IllegalArgumentException("no such analyzer found in schema: " + schemaName);
          analyzers.add(namedAnalyzers.get(analyzerName));
        }
      }
    }
    if (analyzers.isEmpty())
      throw new NoValidIndexesFoundException();
    if (analyzers.size() > 1)
      errorLogger.log(Level.WARN, "Multiple analyzers detected, will use only first");
    return analyzers;
  }

  public synchronized void removeRequest(IndexSearcher searcher) {
    --this.currConcurrancy;
  }

  public int getServerPort() {
    return this.serverPort;
  }

  public ServerStatus getStatus() throws CorruptIndexException, IOException {
    ServerStatus status = new ServerStatus();
    status.setCurrentConcurrantSearchRequests(this.currConcurrancy);
    status.setUpTime((new Date()).getTime() - this.startTime.getTime());
    status.setTotalSearchRequests(this.totalSearchRequests);
    Set<String> baseReaders = new HashSet<String>();
    for (String name : this.namedReaders.keySet()) {
      Set<IndexReader> readerset = this.namedReaders.get(name);
      if (readerset != null) {
        for (IndexReader reader : readerset) {
          if ((reader != null) && (name != null)) {
            baseReaders.add(name +" ("+reader.numDocs()+" docs)[@"+reader.getVersion()+((!reader.isCurrent())?"<"+IndexReader.getCurrentVersion(reader.directory()):"")+"]["+(reader.isOptimized()?"optimized":"unoptimized")+"]: " + reader.directory().toString());
          }
        }
      }
    }
    Set<String> auxReaders = new HashSet<String>(auxIndexManagerMap.size());
    for (String name: this.auxIndexManagerMap.keySet()) {
      AuxIndexManager aim = auxIndexManagerMap.get(name);
      auxReaders.add(name+aim.toString());
    }
    status.setLoadedIndexes(baseReaders.toArray(new String[0]));
    status.setLoadedAuxIndexes(auxReaders.toArray(new String[0]));
    return status;
  }

  /*
   * public IndexReader getReader(Searcher searcher) { return
   * this.readerMap.get(searcher); }
   */

  public boolean isAuthorized(Socket sock, RequestType type) {
    String actualHost = sock.getInetAddress().toString();
    if (this.authorizationMap.containsKey(type)) {
      if (this.authorizationMap.get(type).contains(actualHost))
        return true;
      try {
        sock.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    errorLogger.log(Level.INFO, "Hack attempt from: " + actualHost);
    return false;
  }

  public long getMaxSearchTimeout() {
    return this.maxSearchTimeout;
  }

  public boolean loadIndexes(HashMap<String, String> indexLoadRequests, boolean ram) throws CorruptIndexException,
      IOException {
    Map<String, Set<IndexReader>> newNamedReaders = new HashMap<String, Set<IndexReader>>();
    Map<String, Set<IndexReader>> readersToClose = new HashMap<String, Set<IndexReader>>();
    Map<String, Float> newReaderBoosts = new HashMap<String, Float>();
    Set<String> schemasToReload = new HashSet<String>();

    // open new readers and add them to the new namedReaders map
    for (String indexName : indexLoadRequests.keySet()) {
      HashSet<IndexReader> hs = new HashSet<IndexReader>();
      IndexReader reader = null;
      if (ram)
        reader = IndexReader.open(new RAMDirectory(new NIOFSDirectory(new File(indexLoadRequests.get(indexName)))),
            true);
      else
        reader = IndexReader.open(new NIOFSDirectory(new File(indexLoadRequests.get(indexName))), true);
      String facetConfig = this.schemaFacetConfigMap.get(this.indexnameSchemaMap.get(indexName));
      if (facetConfig != null) {
        try {
          FileReader in = new FileReader(new File(facetConfig));
          String indexPath = indexLoadRequests.get(indexName);
          try {
            File outfile = new File(indexPath + File.separator + "bobo.spring");
            if (!outfile.exists()) {
              FileWriter out = new FileWriter(outfile, false);
              int c;
              while ((c = in.read()) != -1)
                out.write(c);
              out.close();
            }
          } catch (IOException e) {
            errorLogger.log(Level.WARN, "Error copying facet configuration: " + e);
          }
          in.close();
        } catch (IOException e) {
          errorLogger.log(Level.WARN, "Error reading facet configuration: " + e);
        }
      }

      hs.add(reader);
      newNamedReaders.put(indexName, hs);
      newReaderBoosts.put(reader.directory().toString(), 0f);
      schemasToReload.add(this.indexnameSchemaMap.get(indexName));
    }

    for (String oIndexName : this.namedReaders.keySet()) {
      if (oIndexName != null) {
        String olddirpath = this.namedReaders.get(oIndexName).iterator().next().directory().toString();
        if (!newNamedReaders.containsKey(oIndexName)) {
          newReaderBoosts.put(olddirpath, this.readerBoosts.get(olddirpath));
          newNamedReaders.put(oIndexName, this.namedReaders.get(oIndexName));
        } else {
          String newdirpath = newNamedReaders.get(oIndexName).iterator().next().directory().toString();
          newReaderBoosts.put(newdirpath, this.readerBoosts.get(olddirpath));
          readersToClose.put(oIndexName, this.namedReaders.get(oIndexName));
        }
      }
    }

    for (String schema : schemasToReload) {
      Set<IndexReader> readers = new HashSet<IndexReader>();
      for (String indexName : this.schemaIndexNameMap.get(schema)) {
        readers.addAll(newNamedReaders.get(indexName));
      }
      newNamedReaders.put(schema, readers);
    }

    IndexReaderGrimReaper readerReaper = new IndexReaderGrimReaper(readersToClose, this.searcherMap, this.browserMap,
        this.maxSearchTimeout);
    readerReaper.runCleanUp();

    synchronized (this.namedReaders) {
      this.namedReaders = newNamedReaders;
      this.readerBoosts = newReaderBoosts;
    }

    return true;
  }

  private class IndexReaderGrimReaper implements Runnable {

    private Map<String, Set<IndexReader>> readerSetsToRemove;

    private Map<Set<IndexReader>, IndexSearcher> searcherMap;

    private long maxSearchTimeout;

    private Map<Set<IndexReader>, Browsable> browserMap;

    public IndexReaderGrimReaper(Map<String, Set<IndexReader>> readersToClean,
        Map<Set<IndexReader>, IndexSearcher> searcherMap, Map<Set<IndexReader>, Browsable> browserMap,
        long maxSearchTimeout) {
      this.readerSetsToRemove = readersToClean;
      this.searcherMap = searcherMap;
      this.browserMap = browserMap;
      this.maxSearchTimeout = maxSearchTimeout;
    }

    protected void runCleanUp() {
      new Thread(this).start();
    }

    @Override
    public void run() {
      try {
        Thread.sleep(this.maxSearchTimeout + this.maxSearchTimeout / 10);
        HashSet<IndexReader> readersToDestroy = new HashSet<IndexReader>();
        for (String indexSet : this.readerSetsToRemove.keySet()) {
          for (IndexReader ir : this.readerSetsToRemove.get(indexSet)) {
            readersToDestroy.add(ir);
          }
        }
        Set<Set<IndexReader>> setToRemove = new HashSet<Set<IndexReader>>();
        for (Set<IndexReader> indexReaderSet : this.searcherMap.keySet()) {
          for (IndexReader ir : indexReaderSet)
            if (readersToDestroy.contains(ir)) {
              setToRemove.add(indexReaderSet);
            }
        }
        for (Set<IndexReader> set : setToRemove) {
          synchronized (this.searcherMap) {
            this.searcherMap.get(set).close();
            this.searcherMap.remove(set);
          }
          synchronized (this.browserMap) {
            if (this.browserMap.containsKey(set)) {
              this.browserMap.get(set).close();
              this.browserMap.remove(set);
            }
          }
        }
        setToRemove = null;
        for (IndexReader ir : readersToDestroy) {
          ir.close();
        }
      } catch (InterruptedException e) {
        errorLogger.log(Level.ERROR, e);
        // e.printStackTrace();
      } catch (IOException e) {
        errorLogger.log(Level.ERROR, e);
        // e.printStackTrace();
      }
    }
  }

  public boolean unloadIndexes(HashMap<String, String> indexUnloadRequests) {
    return true;
  }

  public Scriptable getScopeByName(String scopeName) {
    return this.scopeMap.get(scopeName);
  }

  public HashSet<String> getExpressionFields(String indexName) {
    return this.schemaExpressionFieldMap.get(this.indexnameSchemaMap.get(indexName));
  }

  public Map<String, Float> getReaderBoosts() {
    return this.readerBoosts;
  }

  public String getIndexDir(String indexName) {
    Set<IndexReader> readers = this.namedReaders.get(indexName);
    if (readers != null) {
      IndexReader reader = readers.iterator().next();
      if (reader != null)
        return reader.directory().toString();
    }
    return null;
  }

  public Grouper getGrouper(List<String> indexes, String canonicalName, DataInputStream dis, Context cx,
      Scriptable scope, HashMap<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources,
      SearchResourceManager searchResourceManager, Map<String, Float> readerBoosts,
      HashMap<String, FieldType> fieldTypes, HashSet<String> expressionFields, ScorerType scorerType)
      throws IOException {
    String schema = null;
    for (String index : indexes) {
      if (this.indexnameSchemaMap.containsKey(index)) {
        schema = this.indexnameSchemaMap.get(index);
        break;
      }
    }

    GrouperFactory factory = this.schemaGrouperFactoryMap.get(schema).get(canonicalName);

    return factory.decoder(dis, cx, scope, externalValSource, namedExprs, valueSources, searchResourceManager,
        readerBoosts, fieldTypes, expressionFields, scorerType);
  }

  public AuxIndexManager getAuxIndexer(String indexName) {
    if (auxIndexManagerMap != null)
      return auxIndexManagerMap.get(indexName);
    return null;
  }

  public TermDocsFactoryBuilder termDocsFactoryBuilder(List<String> indexes, String analyzerName) {
    String schema = null;
    for (String index : indexes) {
      if (this.indexnameSchemaMap.containsKey(index)) {
        schema = this.indexnameSchemaMap.get(index);
        break;
      }
    }
    if (!analyzerTDFBMap.containsKey(schema)) {
      throw new IllegalArgumentException("schema: " + schema + " not found");
    }
    Map<String, TermDocsFactoryBuilder> TDFBMap = analyzerTDFBMap.get(schema);
    if (!TDFBMap.containsKey(analyzerName))
      throw new IllegalArgumentException("analyzer: " + analyzerName + " not found in schema: " + schema);
    TermDocsFactoryBuilder builder = TDFBMap.get(analyzerName);
    return builder;
  }

  public IndexReader getReader(ArrayList<String> indexes) throws IOException {
    Set<IndexReader> readers = new HashSet<IndexReader>();
    for (String index : indexes) {
      Set<IndexReader> r = namedReaders.get(index);
      readers.addAll(r);
    }
    MultiReader reader = new MultiReader((IndexReader[]) readers.toArray(new IndexReader[readers.size()]), false);
    return reader;
  }

  public void setSearchServer(SearchServer searchServer) {
    this.searchServer = searchServer;
    
  }

  public SearchServer getSearchServer() {
 
    return this.searchServer;
  }

  public int getPortNumber() {
    
    return this.serverPort;
  }

  public String getConfigPath() {
    return this.configPath;
  }

  public void closeAll() {
    if(this.readerKiller!=null) {
      readerKiller.runCleanUp();
      statusLogger.log(Level.INFO, "closing old index readers");
      this.readerKiller = null;
    }
    
  }

}
