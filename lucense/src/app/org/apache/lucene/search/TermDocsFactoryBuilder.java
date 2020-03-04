/*
 * @(#)org.apache.lucene.search.TermDocsFactoryBuilder.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package org.apache.lucene.search;

import java.io.DataInputStream;
import java.io.IOException;

public abstract class TermDocsFactoryBuilder {
  public abstract TermDocsFactory decode(DataInputStream dis) throws IOException;
}
