package com.cleartrip.sw.suggest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.suggest.FileDictionary;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.fst.FSTCompletionLookup;
import org.apache.lucene.search.suggest.fst.FSTLookup;
import org.apache.lucene.search.suggest.fst.WFSTCompletionLookup;

public class WFSTCompletionLookupFactory extends LookupFactory {

  private final String dir;	
  	
  public WFSTCompletionLookupFactory(String dir) {
	this.dir = dir;	
  }

  @Override
  public Lookup getLookup(IndexReader reader, String field) throws IOException {
    Lookup l = new WFSTCompletionLookup();
    String fieldFileName = dir + File.separator + field + ".csv";
    File f = new File(fieldFileName);
    
    if (f.exists()){
    	l.build(new FileDictionary(new FileInputStream(f)));
    }
    else{
    	System.out.println("File " + f.getAbsolutePath() + " doesn't exists.");
    	System.out.println("Building WFST dictionary from suggester index.");
    	l.build(new LuceneDictionary(reader, field));
    }

    return l;
  }

}
