package com.adichad.lucense.bitmap;

import java.io.IOException;

import org.apache.lucene.search.Collector;

import com.sleepycat.db.DatabaseException;

public class AuxIndexFilteringCollectorFactory {
  private final AuxIndexManager im;

  private final byte[] rowid;

  private final boolean exclude;

  public AuxIndexFilteringCollectorFactory(AuxIndexManager im, byte[] rowid, boolean exclude) {
    this.im = im;
    this.rowid = rowid;
    this.exclude = exclude;
  }

  public AuxIndexFilteringCollector wrap(Collector c) throws IOException, DatabaseException {
    return new AuxIndexFilteringCollector(c, im, rowid, exclude);
  }
}
