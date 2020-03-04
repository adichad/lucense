/*
 * @(#)org.apache.lucene.search.TermDocsFactory.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package org.apache.lucene.search;

import java.util.Map;

import org.apache.lucene.index.TermDocs;

public abstract class TermDocsFactory {
  public TermDocsFactory(Map<String, ?> params) {

  }

  public abstract TermDocs wrapTermDocs(TermDocs termDocs);
}
