/*
 * @(#)com.adichad.lucense.resource.IndexWriteActionTest.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.resource;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;

import com.adichad.lucense.indexer.target.IndexingTarget;
import com.adichad.lucense.indexer.target.LuceneIndexDefinition;

public class IndexWriteActionTest {

  private IndexWriteAction iwa;
  private LinkedList<Map<String, String>> docs;
  private LuceneIndexDefinition it;

  @Before
  public void setUp() throws Exception {
    this.docs = new LinkedList<Map<String, String>>();
    this.it = new LuceneIndexDefinition();
    
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testExecute() {
    this.iwa = new IndexWriteAction.ReplaceDocumentsAction(Context.enter(), docs);
    try {
      iwa.execute(this.it);
    } catch (Throwable e) {
      fail("caught unexpected exception: "+e); // TODO
    }
    fail("Not yet implemented"); // TODO
  }

}
