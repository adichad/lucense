package com.cleartrip.sw.search.experiments;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.suggest.fst.FSTLookup;
import org.apache.lucene.store.NIOFSDirectory;

public class AutoSuggestTest {

  public static void main(String[] args) throws CorruptIndexException, IOException {
    FSTLookup lookup = new FSTLookup();
    TermEnum termEnum = IndexReader.open(new NIOFSDirectory(new File(""))).terms();
    lookup.build(new TermEnumTermFreqIterator(termEnum));
    termEnum.close();
    //lookup.store(storeDir)
  }
}
