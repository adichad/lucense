package com.adichad.lucense.indexer.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.indexer.FieldFactory;
import com.adichad.lucense.indexer.target.IndexingTarget;

public class FileDocumentSource implements DocumentSource {
  private Reader sourceReader;

  private ArrayList<File> arrFiles;

  private Iterator<File> iterator;

  private File current;

  private StringBuilder s;

  public FileDocumentSource(String dspath, boolean subdirs, String dsfiletype) throws FileNotFoundException {
    File input = new File(dspath);
    this.arrFiles = new ArrayList<File>();
    if (input.isDirectory()) {
      if (subdirs == false) {
        File[] files = input.listFiles();
        for (File file : files) {
          if (!file.isDirectory())
            this.arrFiles.add(file);
        }
      } else {
        traverseAll(input);
      }
    } else {
      this.arrFiles.add(input);
    }

    this.iterator = this.arrFiles.iterator();
  }

  private void traverseAll(File input) {
    File[] files = input.listFiles();
    for (File file : files) {
      if (!file.isDirectory())
        this.arrFiles.add(file);
      else
        traverseAll(file);
    }
  }

  @Override
  public void close() throws Exception {
    if (this.sourceReader != null)
      this.sourceReader.close();
  }

  @Override
  public void executePostQuery() throws Exception {

  }

  @Override
  public Document getDocument(IndexingTarget target, Context cx) throws Exception {
    Document doc = new Document();
    FieldFactory ff = target.getFieldFactory();
    doc.add(ff.initField("content"));

    ff.getField(0, this.s.toString());

    return doc;
  }

  @Override
  public boolean next() throws Exception {
    boolean updated = false;

    while (this.iterator.hasNext()) {
      if (!(this.current = this.iterator.next()).isDirectory()) {
        updated = true;
        break;
      }
    }
    if (updated) {
      if (this.sourceReader != null) {
        this.sourceReader.close();
      }
      this.sourceReader = new BufferedReader(new FileReader(this.current));
      this.s = new StringBuilder();
      int c;
      while ((c = this.sourceReader.read()) >= 0) {
        this.s.append((char) c);
      }
      return true;
    }
    return false;
  }

  @Override
  public long getTotalTimeDoc() {
    return 0;
  }

  @Override
  public long getTotalTimeSource() {
    return 0;
  }

  @Override
  public void initFieldFactory(IndexingTarget target) throws Exception {
    // TODO Auto-generated method stub

  }
}
