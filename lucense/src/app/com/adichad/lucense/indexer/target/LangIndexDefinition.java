package com.adichad.lucense.indexer.target;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.LockObtainFailedException;
import org.mozilla.javascript.Context;

import com.adichad.lucense.analysis.spelling.TermSequenceGraph;
import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.indexer.FieldFactory;
import com.adichad.lucense.indexer.FieldTransformer;
import com.adichad.lucense.resource.IndexerConfigParser.FieldModifier;

public class LangIndexDefinition implements SettableIndexingTarget {
  private String name;

  private String path;

  private String analyzername;

  private Analyzer analyzer;

  private String dataSource;

  private boolean append;

  private int addedCount;

  private HashSet<String> modifierNames;

  private HashMap<String, FieldModifier> fieldModifiers;

  private FieldFactory fieldFactory;

  private TermSequenceGraph graph;

  private int deletedCount;

  private Document theDoc;

  public LangIndexDefinition() {
    this.modifierNames = new HashSet<String>();
    this.fieldModifiers = new HashMap<String, FieldModifier>();
    this.modifierNames.add("content");
    this.append = false;
    this.graph = new TermSequenceGraph();
    this.addedCount = this.deletedCount = 0;
  }

  @Override
  public String getDataSource() {
    return this.dataSource;
  }

  @Override
  public void setAnalyzer(HashMap<String, Analyzer> namedAnalyzers) {
    this.analyzer = namedAnalyzers.get(this.analyzername);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void addDocument(Context cx) throws CorruptIndexException, IOException {
    StringReader sr = new StringReader(theDoc.get("content"));
    TokenStream ts = this.analyzer.tokenStream(null, sr);
    CharTermAttribute termAtt = ts.addAttribute(CharTermAttribute.class);
    this.graph.reset();
    ts.reset();
    while (ts.incrementToken()) {
      // posAtt.getPositionIncrement()
      this.graph.add(termAtt.toString());
    }
    ts.end();
    ts.close();
    sr.close();

    ++this.addedCount;
  }

  @Override
  public void optimize() throws CorruptIndexException, IOException {
    this.graph.getDictionary().normalize();
  }

  @Override
  public void close() throws CorruptIndexException, IOException {
    OutputStream out = new FileOutputStream(this.path + "/" + this.name);
    this.graph.writeTo(out);
    out.close();
    this.graph = null;
  }

  @Override
  public int getAddedCount() {
    return this.addedCount;
  }

  @Override
  public int getDeletedCount() {
    return this.deletedCount;
  }

  @Override
  public void addContext(Context cx) {
    // TODO Auto-generated method stub

  }

  @Override
  public FieldFactory getFieldFactory() {
    return this.fieldFactory;
  }

  @Override
  public void setName(String value) {
    this.name = value;
  }

  @Override
  public void setPath(String value) {
    this.path = value;
  }

  @Override
  public void setAnalyzerName(String value) {
    this.analyzername = value;
  }

  @Override
  public void setDataSource(String value) {
    this.dataSource = value;

  }

  @Override
  public void setAppend(boolean b) {
    this.append = b;
  }

  @Override
  public void setFieldModifiers(HashMap<String, FieldModifier> fieldModifiers, LinkedHashMap<Pattern, FieldModifier> regexModifiers) {
    this.fieldModifiers = new HashMap<String, FieldModifier>();
    for (String name : this.modifierNames) {
      this.fieldModifiers.put(fieldModifiers.get(name).getFieldName(), fieldModifiers.get(name));
    }
    this.fieldFactory = new FieldFactory(fieldModifiers, regexModifiers);
  }

  @Override
  public void addModifierName(String value) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setFilterString(String value) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setMaxBufferedDocs(int i) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setMaxFieldLength(int i) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setOptimize(boolean b) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setUseCompoundFile(boolean b) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setIdField(String idfield) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setDuplicateUpdate(boolean b) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getTotalTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getTotalFilterTime() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void setMergeFactor(int mergeFactor) {
    // TODO Auto-generated method stub

  }

  @Override
  public void init() throws CorruptIndexException, LockObtainFailedException, IOException {
    // TODO Auto-generated method stub

  }

  

  @Override
  public StringLucenseExpression getFilter(Context cx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void commit() throws Exception {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isOpen() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void deleteDocuments(Query q) throws CorruptIndexException, IOException {

  }

  @Override
  public void initField(String name) {
    // TODO Auto-generated method stub

  }

  @Override
  public Field setField(int i, String value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void transform(Context cx) {
    // TODO Auto-generated method stub

  }

  @Override
  public Document getDocument() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void addDocument(Document doc, StringLucenseExpression filter,
      Map<String, StringLucenseExpression> transformExpressions, List<FieldTransformer> customTransformers, Context cx)
      throws Exception {
    // TODO Auto-generated method stub
    
  }

  

}
