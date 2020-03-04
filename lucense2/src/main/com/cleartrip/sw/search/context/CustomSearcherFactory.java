package com.cleartrip.sw.search.context;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.IndexSearcherWrapper;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.search.Similarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleartrip.sw.suggest.LookupFactory;

public class CustomSearcherFactory extends SearcherFactory {

  private final Similarity                 similarity;
  private final ExecutorService            es;
  private final SearcherWarmingStrategy    warmer;
  private final Map<String, LookupFactory> lookupFactories;

  private static final Logger              log = LoggerFactory
                                                   .getLogger(CustomSearcherFactory.class);

  public CustomSearcherFactory(Similarity similarity, ExecutorService es,
      SearcherWarmingStrategy warmer, Map<String, LookupFactory> lookupFactories) {
    this.similarity = similarity;
    this.es = es;
    this.warmer = warmer;
    this.lookupFactories = lookupFactories;

  }

  @Override
  public IndexSearcher newSearcher(IndexReader reader) throws IOException {
    IndexSearcher searcher = new IndexSearcherWrapper(reader, this.es,
        lookupFactories);
    searcher.setSimilarity(similarity);
    warmer.warm(searcher);
    log.debug("warmed searcher created for index: {}, version: {}", reader
        .getIndexCommit().getDirectory().toString(), reader.getVersion());
    return searcher;
  }

}
