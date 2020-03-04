package com.adichad.lucense.indexer.target;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;
import org.mozilla.javascript.Context;

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
import com.adichad.lucense.indexer.FieldFactory;
import com.adichad.lucense.indexer.FieldTransformer;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.IndexerConfigParser.FieldModifier;

public class LuceneIndexDefinition implements SettableIndexingTarget {
  private static final Version DEFAULT_LUCENE_VERSION = Version.LUCENE_33;

  protected String name;

  protected String path;

  protected String analyzername;

  protected Analyzer analyzer;

  protected String filterString;

  protected StringLucenseExpression filter;

  protected HashSet<String> modifierNames;

  protected HashMap<String, FieldModifier> fieldModifiers;

  protected boolean append;

  protected boolean useCompoundFile;

  protected boolean optimize;

  protected String dataSource;

  private IndexWriter writer;

  protected int maxBufferedDocs;

  protected int maxFieldLength;

  protected FieldFactory fieldFactory;

  protected HashMap<Context, StringLucenseExpression> filterMap;

  protected int addedCount;

  protected HashMap<String, FieldType> fieldTypes;

  protected static Logger statusLogger = Logger.getLogger("StatusLogger");

  protected static Logger errorLogger = Logger.getLogger("ErrorLogger");

  protected Term idterm;

  protected String idfield;

  protected boolean onDuplicateUpdate;

  protected int deletedCount;

  protected long totalTime = 0;

  protected long totalFilterTime;

  protected int mergeFactor;

  protected final Document theDoc = new Document();

  private Version luceneVersion;

  public LuceneIndexDefinition() {
    this.modifierNames = new HashSet<String>();
    this.optimize = false;
    this.append = true;
    this.useCompoundFile = false;
    this.mergeFactor = 10;
    this.maxBufferedDocs = 1000;
    this.onDuplicateUpdate = true;
    this.filterMap = new HashMap<Context, StringLucenseExpression>();
    this.fieldTypes = new HashMap<String, FieldType>();
    this.addedCount = 0;
    this.deletedCount = 0;
    this.idfield = null;
    this.idterm = null;
    this.luceneVersion = DEFAULT_LUCENE_VERSION;
  }

  public Version getMatchVersion() {
    return this.luceneVersion;
  }

  public void setMatchVersion(Version v) {
    this.luceneVersion = v;
  }

  @Override
  public String getDataSource() {
    return this.dataSource;
  }

  @Override
  public FieldFactory getFieldFactory() {
    return this.fieldFactory;
  }

  @Override
  public void initField(String name) {
    theDoc.add(this.fieldFactory.initField(name));
  }

  @Override
  public Field setField(int i, String value) {
    return this.fieldFactory.getField(i, value);
  }

  @Override
  public void transform(Context cx) {
    this.fieldFactory.transform(theDoc, cx);
  }

  
    
  
  
  @Override
  public Document getDocument() {
    return theDoc;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public StringLucenseExpression getFilter(Context cx) {
    return (StringLucenseExpression) ExpressionFactory.getExpressionFromString(this.filterString,
        FieldType.TYPE_STRING, cx, null, null, new HashMap<String, LucenseExpression>(), new ValueSources(
            new HashMap<String, IntValueSource>(), new HashMap<String, FloatValueSource>(),
            new HashMap<String, DoubleValueSource>(), new HashMap<String, BooleanValueSource>(),
            new HashMap<String, StringValueSource>()), null);
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

      IndexWriterConfig conf = new IndexWriterConfig(this.luceneVersion, this.analyzer);
      conf.setOpenMode(this.append ? OpenMode.CREATE_OR_APPEND : OpenMode.CREATE);
      conf.setMaxBufferedDocs(this.maxBufferedDocs);
      LogMergePolicy mp = new LogDocMergePolicy();
      mp.setUseCompoundFile(useCompoundFile);
      mp.setMergeFactor(mergeFactor);
      conf.setMergePolicy(mp);
      this.writer = new IndexWriter(new NIOFSDirectory(new File(this.path + "/" + this.name)), conf);
      if (this.append && this.onDuplicateUpdate && (this.idterm == null)) {
        errorLogger.log(Level.WARN, "id-field undefined for index: " + this.name + ". No de-duplication will occur");
      }
    }
  }

  @Override
  public boolean isOpen() {
    return writer != null;
  }

  @Override
  public void setFieldModifiers(HashMap<String, FieldModifier> fieldModifiers2, LinkedHashMap<Pattern, FieldModifier> regexModifiers) {
    this.fieldModifiers = new HashMap<String, FieldModifier>();
    for (String name : this.modifierNames) {
      this.fieldModifiers.put(fieldModifiers2.get(name).getFieldName(), fieldModifiers2.get(name));
      this.fieldTypes.put(fieldModifiers2.get(name).getFieldName(), fieldModifiers2.get(name).getFieldType());
    }
    for (String name : this.fieldModifiers.keySet()) {
      this.fieldModifiers.get(name).setFieldTypes(this.fieldTypes);
    }
    this.fieldFactory = new FieldFactory(this.fieldModifiers, regexModifiers);
  }

  @Override
  public void setAnalyzer(HashMap<String, Analyzer> namedAnalyzers) {
    this.analyzer = namedAnalyzers.get(this.analyzername);
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
      action = "merge";
    if (action.equals("insert")) {
      transform(cx);
      this.writer.addDocument(theDoc);
      ++this.addedCount;
    } else if (action.equals("merge") || action.equals("update")) {
      transform(cx);
      if (this.append && this.onDuplicateUpdate && (this.idterm != null)) {
        this.idterm = this.idterm.createTerm(theDoc.get(this.idfield));
        this.writer.updateDocument(this.idterm, theDoc);
      } else {
        this.writer.addDocument(theDoc);
      }
      ++this.addedCount;
    } else if (action.equals("delete")) {
      this.idterm = this.idterm.createTerm(theDoc.get(this.idfield));
      this.writer.deleteDocuments(this.idterm);
      ++this.deletedCount;
    }
    this.totalTime += System.currentTimeMillis() - start;
  }
  
  @Override
  public void addDocument(Document doc, StringLucenseExpression filter, Map<String, StringLucenseExpression> transformExpressions,
      List<FieldTransformer> customTransformers, Context cx) throws Exception {
    String action = null;
    if (filter != null) {
      action = filter.evaluate(doc);
    } else
      action = "merge";
    if (action.equals("insert")) {
      this.fieldFactory.transformNew(doc, transformExpressions, customTransformers, cx);
      this.writer.addDocument(doc);
      ++this.addedCount;
    } else if (action.equals("merge") || action.equals("update")) {
      this.fieldFactory.transformNew(doc, transformExpressions, customTransformers, cx);
      if (this.append && this.onDuplicateUpdate && (this.idterm != null)) {
        Term idt = this.idterm.createTerm(doc.get(this.idfield));
        this.writer.updateDocument(idt, doc);
      } else {
        this.writer.addDocument(doc);
      }
      ++this.addedCount;
    } else if (action.equals("delete")) {
      Term idt = this.idterm.createTerm(doc.get(this.idfield));
      this.writer.deleteDocuments(idt);
      ++this.deletedCount;
    }

  }



  @Override
  public void optimize() throws CorruptIndexException, IOException {
    if (this.optimize)
      if (this.writer != null) {
        statusLogger.log(Level.INFO, "Optimizing index: " + getName());
        this.writer.optimize();
        statusLogger.log(Level.INFO, "Optimized index: " + getName());
      }
  }

  @Override
  public void close() throws CorruptIndexException, IOException {
    if (this.writer != null) {
      this.writer.close();
      this.writer = null;
    }
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
  public void setFilterString(String value) {
    this.filterString = value;

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
  public void setMaxFieldLength(int i) {
    this.maxFieldLength = i;
  }

  @Override
  public void setMaxBufferedDocs(int i) {
    this.maxBufferedDocs = i;

  }

  @Override
  public void setUseCompoundFile(boolean b) {
    this.useCompoundFile = b;

  }

  @Override
  public void setMergeFactor(int mergeFactor) {
    this.mergeFactor = mergeFactor;
  }

  @Override
  public void setOptimize(boolean b) {
    this.optimize = b;
  }

  @Override
  public void addModifierName(String value) {
    this.modifierNames.add(value);
  }

  @Override
  public void setIdField(String idfield) {
    this.idfield = idfield;
    if (idfield != null)
      this.idterm = new Term(idfield, "");
  }

  @Override
  public void setDuplicateUpdate(boolean b) {
    this.onDuplicateUpdate = b;
  }

  @Override
  public long getTotalTime() {
    // TODO Auto-generated method stub
    return this.totalTime;
  }

  @Override
  public long getTotalFilterTime() {
    return this.totalFilterTime;
  }

  @Override
  public void init() throws CorruptIndexException, LockObtainFailedException, IOException {
    if (this.writer == null) {

      IndexWriterConfig conf = new IndexWriterConfig(this.luceneVersion, this.analyzer);
      conf.setOpenMode(this.append ? OpenMode.CREATE_OR_APPEND : OpenMode.CREATE);
      conf.setMaxBufferedDocs(this.maxBufferedDocs);
      LogMergePolicy mp = new LogDocMergePolicy();
      mp.setUseCompoundFile(useCompoundFile);
      mp.setMergeFactor(mergeFactor);
      conf.setMergePolicy(mp);
      this.writer = new IndexWriter(new NIOFSDirectory(new File(this.path + "/" + this.name)), conf);

      if (this.append && this.onDuplicateUpdate && (this.idterm == null)) {
        errorLogger.log(Level.WARN, "id-field undefined for index: " + this.name + ". No de-duplication will occur");
      }
    }

  }

  @Override
  public void commit() throws Exception {
    try {
      this.writer.commit();
    } catch (OutOfMemoryError e) {
      this.writer.rollback();
      throw e;
    }
  }

  @Override
  public void deleteDocuments(Query query) throws CorruptIndexException, IOException {
    try {
      this.writer.deleteDocuments(query);
    } catch (OutOfMemoryError e) {
      this.writer.rollback();
      throw e;
    }
  }
}
