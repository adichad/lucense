/*
 * @(#)org.apache.lucene.search.DefaultTermDocsFactoryBuilder.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package org.apache.lucene.search;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.lucene.index.TermDocs;

public class DefaultTermDocsFactoryBuilder extends TermDocsFactoryBuilder {
  private static final TermDocsFactory DEFAULT = new DefaultTermDocsFactory();

  @Override
  public TermDocsFactory decode(DataInputStream dis) throws IOException {
    return DEFAULT;
  }

  public static class DefaultTermDocsFactory extends TermDocsFactory {
    @Override
    public TermDocs wrapTermDocs(TermDocs termDocs) {
      return termDocs;
    }

  }
}
