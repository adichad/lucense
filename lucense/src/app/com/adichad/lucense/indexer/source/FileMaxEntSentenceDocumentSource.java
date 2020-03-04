package com.adichad.lucense.indexer.source;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;

import opennlp.tools.lang.english.SentenceDetector;
import opennlp.tools.sentdetect.SentenceDetectorME;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

import com.adichad.lucense.indexer.FieldFactory;
import com.adichad.lucense.indexer.target.IndexingTarget;

public class FileMaxEntSentenceDocumentSource implements DocumentSource {
  private ArrayList<File> arrFiles;

  private Iterator<File> iterator;

  private File current;

  private SentenceDetectorME sdetector;

  private String[] sents;

  private int currSentIndex;

  public FileMaxEntSentenceDocumentSource(String dspath, boolean subdirs, String dsfiletype)
      throws FileNotFoundException, IOException {
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
    this.sdetector = new SentenceDetector(dsfiletype);
    this.iterator = this.arrFiles.iterator();
    this.currSentIndex = -1;

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
    this.sents = null;
  }

  @Override
  public void executePostQuery() throws Exception {

  }

  @Override
  public Document getDocument(IndexingTarget target, Context cx) throws Exception {
    Document doc = new Document();
    FieldFactory ff = target.getFieldFactory();
    doc.add(ff.initField("content"));

    ff.getField(0, this.sents[this.currSentIndex]);
    return doc;
  }

  @Override
  public boolean next() throws Exception {
    if (this.sents != null) {
      if (this.currSentIndex < this.sents.length - 1) {
        ++this.currSentIndex;
        return true;
      }
      this.sents = null;
    }

    while (this.iterator.hasNext() && (this.sents == null)) {
      if (!(this.current = this.iterator.next()).isDirectory()) {
        Reader sourceReader = new BufferedReader(new FileReader(this.current));
        StringBuilder s = new StringBuilder();
        int c;
        while ((c = sourceReader.read()) >= 0)
          s.append((char) c);
        sourceReader.close();
        this.sents = this.sdetector.sentDetect(s.toString());
        if (this.sents.length < 1)
          this.sents = null;
        else {
          this.currSentIndex = 0;
          return true;
        }
      }
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
