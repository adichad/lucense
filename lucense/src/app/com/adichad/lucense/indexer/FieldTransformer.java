/*
 * @(#)com.adichad.lucense.indexer.FieldTransformer.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.indexer;

import org.apache.lucene.document.Document;
import org.mozilla.javascript.Context;

public abstract class FieldTransformer {
  public abstract void transform(Document doc, Context cx);
}
