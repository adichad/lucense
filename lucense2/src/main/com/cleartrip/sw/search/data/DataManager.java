package com.cleartrip.sw.search.data;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NRTManager;
import org.apache.lucene.search.NRTManager.TrackingIndexWriter;
import org.apache.lucene.search.NRTManagerReopenThread;
import org.apache.lucene.search.SearcherLifetimeManager;
import org.apache.lucene.search.SearcherLifetimeManager.PruneByAge;
import org.apache.lucene.store.LockObtainFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleartrip.sw.analytics.AnalyticsProcessor;
import com.cleartrip.sw.params.AnalyticsParams;
import com.cleartrip.sw.result.AnalyzeResult;
import com.cleartrip.sw.search.context.CustomSearcherFactory;
import com.cleartrip.sw.search.context.TaskStatus;
import com.cleartrip.sw.search.query.processors.QueryProcessor;
import com.cleartrip.sw.search.schema.Schema;
import com.cleartrip.sw.search.searchj.SearchParameters;
import com.cleartrip.sw.search.searchj.SearchResult;
import com.cleartrip.sw.suggest.SuggestResult;
import com.cleartrip.sw.suggest.SuggestionProcessor;

public class DataManager {

  private final Schema                           schema;
  private final DataMapper                       dataMapper;
  private final NRTManager                       nrtManager;
  private NRTManagerReopenThread                 nrtThread;
  private final SearcherLifetimeManager          searcherLifetimeManager;
  private final SearcherLifetimePurgeThread      searcherLifetimePruneThread;
  private final long                             refreshWaitTime;
  private final TimeUnit                         refreshWaitTimeUnit;
  private final Map<String, QueryProcessor>      queryProcessors;
  private final Map<String, SuggestionProcessor> suggestionProcessors;
  private final IndexWriter                      writer;
  private final TrackingIndexWriter              trackingWriter;
  private final Map<String, AnalyticsProcessor>  analyticsProcessors;
  private final CustomSearcherFactory            searcherFactory;

  private static final Logger                    log = LoggerFactory
                                                         .getLogger(DataManager.class);

  static class SearcherLifetimePurgeThread extends Thread implements Closeable {
    private SearcherLifetimeManager slManager;
    private volatile boolean        canRun;
    private PruneByAge              pruner;
    private int                     pruneIntervalMS;

    SearcherLifetimePurgeThread(SearcherLifetimeManager slManager,
        int pruneAgeSec, int pruneIntervalMS) {
      this.slManager = slManager;
      this.canRun = true;
      this.pruner = new SearcherLifetimeManager.PruneByAge(pruneAgeSec);
      this.pruneIntervalMS = pruneIntervalMS;
    }

    @Override
    public void close() throws IOException {
      canRun = false;
      try {
        join();
      } catch (InterruptedException e) {
      }
    }

    @Override
    public void run() {
      while (canRun) {
        try {
          slManager.prune(pruner);
          sleep(pruneIntervalMS);
        } catch (IOException | InterruptedException e) {

        }
      }

    }

  }

  static abstract class SearcherReleaser {
    abstract void release(IndexSearcher searcher) throws IOException;
  }

  final SearcherReleaser NRTReleaser = new SearcherReleaser() {

                                       @Override
                                       void release(IndexSearcher searcher)
                                           throws IOException {
                                         nrtManager.release(searcher);
                                       }

                                     };
  final SearcherReleaser SLMReleaser = new SearcherReleaser() {

                                       @Override
                                       void release(IndexSearcher searcher)
                                           throws IOException {
                                         searcherLifetimeManager
                                             .release(searcher);
                                       }

                                     };
  private final int      maxStaleSec;
  private final int      minStaleSec;

  public DataManager(IndexWriter writer, Schema schema, DataMapper dataMapper,
      Map<String, ?> params, Map<String, QueryProcessor> queryProcessors,
      Map<String, AnalyticsProcessor> analyticsProcessors,
      Map<String, SuggestionProcessor> suggestionProcessors,
      CustomSearcherFactory searcherFactory) throws CorruptIndexException,
      LockObtainFailedException, IOException {
    this.schema = schema;
    this.dataMapper = dataMapper;
    this.refreshWaitTime = (Integer) params.get("refreshWaitTime");
    this.refreshWaitTimeUnit = TimeUnit.valueOf((String) params
        .get("refreshWaitTimeUnit"));
    this.queryProcessors = queryProcessors;
    this.suggestionProcessors = suggestionProcessors;
    this.analyticsProcessors = analyticsProcessors;

    this.writer = writer;

    this.trackingWriter = new TrackingIndexWriter(writer);
    this.searcherFactory = searcherFactory;

    this.nrtManager = new NRTManager(trackingWriter, this.searcherFactory);
    this.searcherLifetimeManager = new SearcherLifetimeManager();
    this.searcherLifetimePruneThread = new SearcherLifetimePurgeThread(
        searcherLifetimeManager, (Integer) params.get("sessionTimeoutSec"),
        (Integer) params.get("sessionPurgeIntervalMS"));
    this.searcherLifetimePruneThread.start();
    this.maxStaleSec = (Integer) params.get("maxStaleSec");
    this.minStaleSec = (Integer) params.get("minStaleSec");
    this.nrtThread = new NRTManagerReopenThread(nrtManager, maxStaleSec,
        minStaleSec);
    this.nrtThread.start();

  }

  public synchronized void toggleNRT(boolean enable) {
    if (enable && nrtThread == null) {
      this.nrtThread = new NRTManagerReopenThread(nrtManager, maxStaleSec,
          minStaleSec);
      this.nrtThread.start();
    } else if (!enable) {
      nrtThread.close();
      nrtThread = null;
    }
  }

  public SuggestResult suggest(Map<String, String[]> params, Reader reader,
      TaskStatus log) throws Exception {
    SuggestResult result = null;
    SuggestionProcessor sProc = null;
    long nrtGenerationId = 0;
    long sessionId = -1;

    Map<String, String[]> qParams = new HashMap<>(params.size());

    for (String name : params.keySet()) {
      String[] val = params.get(name);
      switch (name) {
      case "rtgen": {
        nrtGenerationId = val == null ? 0 : Long.parseLong(val[0]);
        break;
      }
      case "sid": {
        sessionId = val == null ? -1 : Long.parseLong(val[0]);
        break;
      }
      case "sp": {
        sProc = val == null ? suggestionProcessors.get("default")
            : suggestionProcessors.get(val[0]);
        break;
      }
      default: {
        qParams.put(name, val);
      }
      }
    }
    if (sProc == null)
      sProc = suggestionProcessors.get("default");

    IndexSearcher searcher = null;
    SearcherReleaser searcherReleaser = null;

    try {
      if (sessionId > 0) {
        searcher = searcherLifetimeManager.acquire(sessionId);
      }
      if (searcher == null) {
        if (nrtGenerationId > 0) {
          nrtManager.waitForGeneration(nrtGenerationId, refreshWaitTime,
              refreshWaitTimeUnit);
        }
        searcher = nrtManager.acquire();
        searcherReleaser = NRTReleaser;
        sessionId = searcherLifetimeManager.record(searcher);

      } else {
        searcherReleaser = SLMReleaser;
      }
      result = sProc.process(qParams, searcher, log);

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (searcher != null) {
        searcherReleaser.release(searcher);
        searcher = null;
      }
    }

    return result;
  }

  public SearchResult search(Map<String, String[]> params, Reader reader,
      TaskStatus log) throws Exception {
    SearchResult result = null;
    QueryProcessor qProc = null;
    long nrtGenerationId = 0;
    long sessionId = -1;

    Map<String, String[]> qParams = new HashMap<>(params.size());

    for (String name : params.keySet()) {
      String[] val = params.get(name);
      switch (name) {
      case "rtgen": {
        nrtGenerationId = val == null ? 0 : Long.parseLong(val[0]);
        break;
      }
      case "sid": {
        sessionId = val == null ? -1 : Long.parseLong(val[0]);
        break;
      }
      case "qp": {
        qProc = val == null ? queryProcessors.get("default") : queryProcessors
            .get(val[0]);
        break;
      }
      default: {
        qParams.put(name, val);
      }
      }
    }

    IndexSearcher searcher = null;
    SearcherReleaser searcherReleaser = null;

    try {
      if (sessionId > 0) {
        searcher = searcherLifetimeManager.acquire(sessionId);
      }
      if (searcher == null) {
        if (nrtGenerationId > 0) {
          nrtManager.waitForGeneration(nrtGenerationId, refreshWaitTime,
              refreshWaitTimeUnit);
        }
        searcher = nrtManager.acquire();
        searcherReleaser = NRTReleaser;
        sessionId = searcherLifetimeManager.record(searcher);

      } else {
        searcherReleaser = SLMReleaser;
      }
      SearchParameters searchParams = qProc.createSearchParams(qParams, reader);

      result = qProc.process(searchParams, searcher, log);

    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    } finally {
      if (searcher != null) {
        searcherReleaser.release(searcher);
        searcher = null;
      }
    }

    return result;
  }

  public long delete(String[] queryStrings, TaskStatus log, boolean commit)
      throws Exception {
    /*
     * CustomQueryParser parser = this.queryProcessors.get("delete")
     * .getQueryParser(); List<Query> queries = new
     * ArrayList<Query>(queryStrings.length); for (String query : queryStrings)
     * { queries.add(parser.parse(query)); } long genId =
     * trackingWriter.deleteDocuments(queries.toArray(new Query[queries
     * .size()])); String action = ""; if (commit) { writer.commit(); action =
     * "(c)"; }
     * log.info.append("docs deleted").append(action).append(" where [")
     * .append(queries).append("]");
     */
    return 0;// genId;
  }

  public long upsert(Reader reader, TaskStatus log, Map<String, ?> params)
      throws Exception {
    DataMapperState state = dataMapper.init(reader, schema);
    Term idTerm = schema.getIdTerm();
    Document doc;
    int i = 0;
    long genId = 0;
    List<String> skipped = new LinkedList<String>();
    long start, writerTime = 0;

    while ((doc = dataMapper.map(skipped, state)) != null) {
      idTerm = idTerm.createTerm(doc.get(idTerm.field()));
      try {
        if (idTerm.text() == null)
          throw new NullPointerException();
        start = System.currentTimeMillis();
        genId = trackingWriter.updateDocument(idTerm, doc);
        writerTime += System.currentTimeMillis() - start;
        i++;
      } catch (NullPointerException npe) {
        // npe.printStackTrace();
        skipped.add(idTerm.text());
      }

    }

    String action = "";
    if ((Boolean) params.get("commit")) {
      start = System.currentTimeMillis();
      writer.commit();
      writerTime += System.currentTimeMillis() - start;
      action = "(c)";
    }
    if ((Boolean) params.get("purgeDeletes")) {
      writer.forceMergeDeletes();
      action += "(p)";
    }
    log.info.append("[lucene: ").append(writerTime)
        .append(" ms] docs upserted").append(action).append("[").append(i)
        .append("] skipped[").append(skipped.size()).append("[")
        .append(skipped).append("]").append("]");
    dataMapper.destroy(state);
    return genId;
  }

  public long insert(Reader reader, TaskStatus log, boolean commit)
      throws Exception {
    DataMapperState state = dataMapper.init(reader, schema);
    Document doc;
    int i = 0;
    long genId = 0;
    List<String> skipped = new LinkedList<String>();
    while ((doc = dataMapper.map(skipped, state)) != null) {
      genId = trackingWriter.addDocument(doc);
      i++;
    }
    dataMapper.destroy(state);
    String action = "";
    if (commit) {
      writer.commit();
      action = "(c)";
    }
    log.info.append("docs inserted").append(action).append("[").append(i)
        .append("] skipped[").append(skipped.size()).append("]");
    return genId;
  }

  public void close() throws CorruptIndexException, IOException {
    if (nrtThread != null)
      nrtThread.close();
    nrtManager.close();

    searcherLifetimePruneThread.close();
    searcherLifetimeManager.close();
    writer.close();

  }

  public long upsert(List<Map<String, ?>> records, boolean commit,
      DataMapper dataMapper) throws Exception {
    DataMapperState state = dataMapper.init(records, schema);
    Term idTerm = schema.getIdTerm();
    Document doc;
    int i = 0;
    long genId = 0;
    List<String> skipped = new LinkedList<String>();
    while ((doc = dataMapper.map(skipped, state)) != null) {
      idTerm = idTerm.createTerm(doc.get(idTerm.field()));
      try {
        if (idTerm.text() == null)
          throw new NullPointerException();
        genId = trackingWriter.updateDocument(idTerm, doc);
        i++;
      } catch (NullPointerException npe) {
        npe.printStackTrace();
        skipped.add(idTerm.text());
      }

    }
    dataMapper.destroy(state);
    String action = "";
    if (commit) {
      writer.commit();
      action = "(c)";
    }
    log.info("docs upserted" + action + "[" + i + "] skipped[" + skipped.size()
        + "[" + skipped + "]" + "]");
    return genId;

  }

  public AnalyzeResult analyze(Map<String, String[]> params, Reader reader,
      TaskStatus log) throws Exception {
    AnalyzeResult result;
    AnalyticsProcessor aProc = null;
    long nrtGenerationId = 0;
    long sessionId = -1;

    Map<String, String[]> qParams = new HashMap<>(params.size());

    for (String name : params.keySet()) {
      String[] val = params.get(name);
      switch (name) {
      case "rtgen": {
        nrtGenerationId = val == null ? 0 : Long.parseLong(val[0]);
        break;
      }
      case "sid": {
        sessionId = val == null ? -1 : Long.parseLong(val[0]);
        break;
      }
      case "proc": {
        aProc = val == null ? analyticsProcessors.get("default")
            : analyticsProcessors.get(val[0]);
        break;
      }
      default: {
        qParams.put(name, val);
      }
      }
    }

    IndexSearcher searcher = null;

    try {
      if (nrtGenerationId > 0 || sessionId <= 0) {
        nrtManager.waitForGeneration(nrtGenerationId, refreshWaitTime,
            refreshWaitTimeUnit);
        searcher = nrtManager.acquire();
        sessionId = searcherLifetimeManager.record(searcher);
      } else {
        searcher = searcherLifetimeManager.acquire(sessionId);
        if (searcher == null) {
          nrtManager.waitForGeneration(nrtGenerationId, refreshWaitTime,
              refreshWaitTimeUnit);
          searcher = nrtManager.acquire();
          sessionId = searcherLifetimeManager.record(searcher);
        }
      }
      AnalyticsParams analyticsParams = aProc.createParams(qParams, reader);

      result = aProc.process(analyticsParams, searcher, log);

    } catch (Exception e) {
      throw e;
    } finally {
      if (searcher != null) {
        searcherLifetimeManager.release(searcher);
      }
    }

    return result;
  }

}
