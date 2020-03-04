package com.adichad.lucense.bitmap;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;

import com.sleepycat.db.DatabaseException;

public class AuxIndexFilteringCollector extends Collector {

  private final Collector c;

  private boolean exclude;

  private String luField;

  private CellResolver cellResolver;

  private Row currRow;

  private CellDictionaryHandler cellDictionary;

  private interface CollectDelegate {
    void collect(int doc) throws Exception;
  }

  private class NullRowCollectDelegate implements CollectDelegate {
    public void collect(int doc) throws Exception {
      if (exclude) {
        c.collect(doc);
      }
    }
  }

  private class ValidRowCollectDelegate implements CollectDelegate {
    public void collect(int doc) throws Exception {
      if ((currRow.search(cellResolver, doc) == BitMapOperationStatus.SUCCESS) ^ exclude) {
        c.collect(doc);
      }

    }
  }

  private final CollectDelegate collectDelegate;

  public AuxIndexFilteringCollector(Collector c, AuxIndexManager im, byte[] rowid, boolean exclude) throws IOException,
      DatabaseException {
    this.c = c;
    this.luField = im.getLucenseFieldName();
    this.currRow = im.getRowHandler().loadRow(rowid);
    this.cellDictionary = im.getCellDictionary();
    this.exclude = exclude;
    this.collectDelegate = ((currRow == null) ? new NullRowCollectDelegate() : new ValidRowCollectDelegate());
  }

  @Override
  public boolean acceptsDocsOutOfOrder() {
    return this.c.acceptsDocsOutOfOrder();
  }

  @Override
  public void collect(int doc) throws IOException {
    try {
      this.collectDelegate.collect(doc);
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.c.setNextReader(reader, docBase);

    this.cellResolver = this.cellDictionary.readArrayFrom(reader, luField);// readArrayFrom(reader,
                                                                           // docBase);
  }

  @Override
  public void setScorer(Scorer scorer) throws IOException {
    this.c.setScorer(scorer);
  }

}
