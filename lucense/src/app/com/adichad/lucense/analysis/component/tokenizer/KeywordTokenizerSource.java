/*
 * @(#)com.adichad.lucense.analysis.component.filter.KeywordTokenizerSource.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.analysis.component.tokenizer;

import java.io.Reader;

import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.TokenStream;


public class KeywordTokenizerSource implements TokenStreamSource {

  @Override
  public TokenStream getTokenStream(Reader reader) throws Exception {
    return new KeywordTokenizer(reader);
  }

}
