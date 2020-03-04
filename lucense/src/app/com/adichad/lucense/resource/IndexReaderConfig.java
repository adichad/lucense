/*
 * @(#)com.adichad.lucense.resource.IndexReaderConfig.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

public class IndexReaderConfig {

  private static Logger statusLogger = Logger.getLogger("StatusLogger");
  private static Logger errorLogger = Logger.getLogger("ErrorLogger");
  
  private boolean storeOnRAM;
  private boolean forceCreate;
  private String indexPath;
  private Version defaultLuceneVersion;
  private Analyzer schemaAnalyzer;
  private String indexName;

  public IndexReaderConfig(boolean storeOnRAM, boolean forceCreate, String indexPath, Version defaultLuceneVersion, 
     Analyzer schemaAnalyzer, String indexName) {
    this.storeOnRAM = storeOnRAM;
    this.forceCreate = forceCreate;
    this.indexPath = indexPath;
    this.defaultLuceneVersion  = defaultLuceneVersion;
    this.schemaAnalyzer = schemaAnalyzer;
    this.indexName = indexName;
  }
  
  public IndexReader getReader() throws IOException {
    IndexReader reader;

    try {
      if (this.storeOnRAM) {
        reader = IndexReader.open(new RAMDirectory(new NIOFSDirectory(new File(this.indexPath))), true);
      } else {
        reader = IndexReader.open(new NIOFSDirectory(new File(this.indexPath)), true);
      }
    } catch (FileNotFoundException e) {
      if (this.forceCreate) {
        IndexWriterConfig conf = new IndexWriterConfig(this.defaultLuceneVersion, schemaAnalyzer);
        conf.setOpenMode(OpenMode.CREATE_OR_APPEND);

        IndexWriter writer = new IndexWriter(new NIOFSDirectory(new File(this.indexPath)), conf);
        writer.close();

        if (this.storeOnRAM) {
          reader = IndexReader.open(new RAMDirectory(new NIOFSDirectory(new File(this.indexPath))), true);
        } else {
          reader = IndexReader.open(new NIOFSDirectory(new File(this.indexPath)), true);
        }
        errorLogger.log(Level.WARN, "Index: " + this.indexName + " not found, created empty index");
      } else {
        throw e;
      }
    }

    statusLogger.log(Level.INFO, "loaded index: " + this.indexName);
    return reader;

  }
}
