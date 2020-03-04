/**
 * 
 */
package com.adichad.lucense.analysis.spelling.neo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.neo4j.graphdb.Transaction;

import com.adichad.lucense.analysis.AnalyzerFactory;
import com.adichad.lucense.analysis.component.filter.LowerCaseFilterSource;
import com.adichad.lucense.analysis.component.filter.TokenFilterSource;
import com.adichad.lucense.analysis.component.tokenizer.PatternTokenizerSource;

public class FixedSizeContextCorpus {

  private ContextManager    contextManager;

  private GraphStoreManager graphStoreManager;

  private int proximity;

  public FixedSizeContextCorpus(String graphPath, int proximity) throws IOException {

    this.graphStoreManager = new GraphStoreManager(graphPath, proximity);
    this.contextManager = new ContextManager(proximity, this.graphStoreManager);
    this.proximity = proximity;
    DataOutputStream dos = new DataOutputStream(new FileOutputStream (graphPath+"/"+"proximity.conf", false));
    dos.writeInt(proximity);
    dos.close();
  }
  
  public FixedSizeContextCorpus(String graphPath) throws IOException {
    DataInputStream dis = new DataInputStream(new FileInputStream(graphPath+"/"+"proximity.conf"));
    this.proximity = dis.readInt();
    dis.close();
    this.graphStoreManager = new GraphStoreManager(graphPath, proximity);
    this.contextManager = new ContextManager(proximity, this.graphStoreManager);
  }

  public int getProximity() {
    return proximity;
  }
  
  public Transaction beginTx() {
    return this.graphStoreManager.beginTx();
  }

  public boolean add(String label, int posIncr) {
    contextManager.addAndPersist(label, posIncr);
    return true;
  }

  public boolean contextFilter(String label, int posIncr,
      Map<Correction, Correction> corrs) {
    contextManager.contextFilter(label, posIncr, corrs);
    return true;
  }

  public void resetContext() {
    this.contextManager.reset();
  }

  public void purge() {
    this.graphStoreManager.purge();
  }

  public void normalize() {
    this.graphStoreManager.normalize();
  }

  public void close() {
    this.graphStoreManager.close();
  }

  public static void main(String[] args) throws Throwable {
    // params
    String graphPath = "/home/adichad/workspace/neo4j";
    int proximity = 3;
    
    // test data
    List<String> queries = new LinkedList<String>();
    for (int i = 0; i < 1; i++) {
      queries.add("The glass of water");
      queries.add("The glass of waiter");
      queries.add("The glass of water on the table");
      queries.add("A glass of water");
      queries.add("A waiter on the glass table");
      queries.add("The water on the glass table");
      queries.add("apache lucene engineer");
      queries.add("lucent engineer");
    }

    // analyzer definition
    List<TokenFilterSource> filterSources = new ArrayList<TokenFilterSource>();
    filterSources.add(new LowerCaseFilterSource(Version.LUCENE_33));
    Analyzer an = AnalyzerFactory.createAnalyzer(new PatternTokenizerSource(
        "[\\w#][\\w#+]*"), filterSources);

    // purge graph
    
    
    FixedSizeContextCorpus corpus = new FixedSizeContextCorpus(
        graphPath, proximity);
    long start = System.currentTimeMillis();
    Transaction tx = corpus.beginTx();
    try {
      corpus.purge();
      tx.success();
    } catch (Throwable e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }

    System.out.println("Purge time: " + (System.currentTimeMillis() - start)
        + " msecs");
    corpus.close();

    // build graph
    corpus = new FixedSizeContextCorpus(graphPath, proximity);
    start = System.currentTimeMillis();
    tx = corpus.beginTx();
    try {
      int i=0;
      int batchSize = 10000;
      long istart = System.currentTimeMillis();
      long time =0;
      for(String query: queries) {
      //System.out.println("print!");
      //for (int c =0; c < 1000; c++) {
        //String query = "a glass of waiter";
        StringReader reader = new StringReader(query);
        TokenStream ts = an.reusableTokenStream("", reader);

        CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
        PositionIncrementAttribute posAttr = ts
            .addAttribute(PositionIncrementAttribute.class);

        corpus.resetContext();
        while (ts.incrementToken()) {
          istart = System.currentTimeMillis();
          corpus.add(termAtt.toString(), posAttr.getPositionIncrement());
          time +=  (System.currentTimeMillis() - istart);
        }
        
        ts.close();
        
        
        if(i%batchSize==0) {
          System.out.println("batch add time ("+i+"): " + time
              + " msecs");
          time = 0;
          tx.success();
          tx.finish();
          tx = corpus.beginTx();
          
          //istart = System.currentTimeMillis();
        }
          
        i++;
      }
      tx.success();
    } catch (Throwable e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
    System.out.println("Add time: " + (System.currentTimeMillis() - start)
        + " msecs");

    start = System.currentTimeMillis();
    tx = corpus.beginTx();
    try {
      corpus.normalize();
      tx.success();
    } catch (Throwable e) {
      tx.failure();
      throw e;
    } finally {
      tx.finish();
    }
    System.out.println("Normalize time: "
        + (System.currentTimeMillis() - start) + " msecs");

    start = System.currentTimeMillis();
    String query = "A glass of mineral waiter";

    TokenStream ts = an.tokenStream("", new StringReader(query));
    CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
    PositionIncrementAttribute posAttr = ts
        .addAttribute(PositionIncrementAttribute.class);
    corpus.resetContext();
    List<CorrectedContextTerm> results = new LinkedList<CorrectedContextTerm>();
    Term t = new Term("label", "");
    while (ts.incrementToken()) {
      String text = termAtt.toString();
      Map<Correction, Correction> corrs = new HashMap<Correction, Correction>();
      corpus.queryFilter(new FuzzyQuery(t.createTerm(text), 0.0f, 2,
          Integer.MAX_VALUE), corrs);
      corpus.editDistanceFilter(text, corrs);

      corpus.contextFilter(text, posAttr.getPositionIncrement(), corrs);
      CorrectedContextTerm cct = new CorrectedContextTerm();
      cct.posIncr = posAttr.getPositionIncrement();
      cct.term = termAtt.toString();
      cct.corrs = corrs;
      results.add(cct);
    }
    while (corpus.advanceNoInput())
      ;
    System.out.println(results);
    System.out.println("Correction time: "
        + (System.currentTimeMillis() - start) + " msecs");

    start = System.currentTimeMillis();
    corpus.close();
    System.out.println("Close time: " + (System.currentTimeMillis() - start)
        + " msecs");

    System.out.println("done.");
  }

  public void editDistanceFilter(String text, Map<Correction, Correction> corrs) {
    for (Correction corr : corrs.values()) {
      corr.score /= (((Integer) (StringUtils.getLevenshteinDistance(text,
          corr.label) + 1)).floatValue());
      //System.out.println("from editfilter: "+corr);
    }
    
  }

  public void queryFilter(Query query, Map<Correction, Correction> corrs) {
    this.graphStoreManager.queryFilter(query, corrs);
  }

  public boolean advanceNoInput() {
    return this.contextManager.advanceNoInput();
  }

}
