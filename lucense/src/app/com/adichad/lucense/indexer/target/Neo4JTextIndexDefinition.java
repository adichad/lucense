/*
 * @(#)com.adichad.lucense.indexer.target.Neo4JTextIndexDefinition.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.indexer.target;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.CorruptIndexException;
import org.mozilla.javascript.Context;
import org.neo4j.graphdb.Transaction;

import com.adichad.lucense.analysis.spelling.neo.FixedSizeContextCorpus;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.request.Request.FieldType;

public class Neo4JTextIndexDefinition extends LuceneIndexDefinition {
  FixedSizeContextCorpus writer = null;
  private int proximity;
  private Transaction tx;
  
  public Neo4JTextIndexDefinition() {
    super();
    this.proximity = 3;
  }
  
  @Override
  public void addContext(Context cx) throws CorruptIndexException, IOException, ParseException {
    this.filter = (StringLucenseExpression) ExpressionFactory.getExpressionFromString(this.filterString,
        FieldType.TYPE_STRING, cx, null, null, new HashMap<String, LucenseExpression>(), new ValueSources(
            new HashMap<String, IntValueSource>(), new HashMap<String, FloatValueSource>(),
            new HashMap<String, DoubleValueSource>(), new HashMap<String, BooleanValueSource>(),
            new HashMap<String, StringValueSource>()), null);
    if (this.filter != null) {
      // filter.initValueSources(); : TODO!!
      this.filterMap.put(cx, this.filter);
    }
    this.fieldFactory.addContext(cx);
    if (this.writer == null) {
      this.writer = new FixedSizeContextCorpus(this.path + "/" + this.name, this.proximity);
      
      if(!this.append) {
        this.tx = this.writer.beginTx();
        writer.purge();
        tx.success();
        tx.finish();
        writer.close();
        
        this.writer = new FixedSizeContextCorpus(this.path + "/" + this.name, this.proximity);
      }
      this.tx = this.writer.beginTx();
    }
  }

  @Override
  public void addDocument(Context cx) throws IOException {
    long start = System.currentTimeMillis();
    String action = null;

    if (this.filter != null) {
      long fstart = System.currentTimeMillis();
      action = this.filter.evaluate(theDoc);
      this.totalFilterTime += System.currentTimeMillis() - fstart;
    } else
      action = "insert";
    if (action.equals("insert")) {
      transform(cx);
      StringReader reader = new StringReader(theDoc.get("content"));
      TokenStream ts = this.analyzer.reusableTokenStream("", reader);

      CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
      PositionIncrementAttribute posAttr = ts
          .addAttribute(PositionIncrementAttribute.class);

      writer.resetContext();
      while (ts.incrementToken()) {
        writer.add(termAtt.toString(), posAttr.getPositionIncrement());
      }
      
      ts.close();
      
      if(++this.addedCount%maxBufferedDocs==0) {
        tx.success();
        tx.finish();
        tx = writer.beginTx();
      }      
    }
    this.totalTime += System.currentTimeMillis() - start;
  }
  
  @Override 
  public void setMergeFactor(int mf) {
    this.proximity = mf;
  }
  @Override
  public void optimize() throws CorruptIndexException, IOException {
    if (this.optimize)
      if (this.writer != null) {
        statusLogger.log(Level.INFO, "Optimizing index: " + getName());
        this.writer.normalize();
        tx.success();
        tx.finish();
        tx = writer.beginTx();
        statusLogger.log(Level.INFO, "Optimized index: " + getName());
      }
  }
  
  @Override
  public void close() throws CorruptIndexException, IOException {
    if (this.writer != null) {
      tx.success();
      tx.finish();
      this.writer.close();
      this.writer = null;
    }
  }
}
