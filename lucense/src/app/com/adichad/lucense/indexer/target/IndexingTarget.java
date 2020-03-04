package com.adichad.lucense.indexer.target;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.LockObtainFailedException;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.indexer.FieldFactory;
import com.adichad.lucense.indexer.FieldTransformer;

public interface IndexingTarget {
  public abstract String getDataSource();

  public abstract FieldFactory getFieldFactory();

  public abstract String getName();

  public abstract void addContext(Context cx) throws Exception;

  public abstract void addDocument(Context cx) throws CorruptIndexException, IOException;

  public abstract void optimize() throws CorruptIndexException, IOException;

  public abstract void close() throws CorruptIndexException, IOException;

  public abstract int getAddedCount();

  public abstract int getDeletedCount();

  public abstract long getTotalTime();

  public abstract long getTotalFilterTime();

  public abstract void init() throws CorruptIndexException, LockObtainFailedException, IOException;

  public abstract void addDocument(Document doc, StringLucenseExpression filter, Map<String, StringLucenseExpression> transformExpressions,
      List<FieldTransformer> customTransformers, Context cx) throws Exception;

  StringLucenseExpression getFilter(Context cx);

  void commit() throws Exception;

  boolean isOpen();

  public abstract void deleteDocuments(Query q) throws CorruptIndexException, IOException;

  public abstract void initField(String name);

  public abstract Field setField(int i, String value);

  public abstract void transform(Context cx);

  public abstract Document getDocument();

  
}
