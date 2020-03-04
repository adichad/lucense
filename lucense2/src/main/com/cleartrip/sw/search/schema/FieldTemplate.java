package com.cleartrip.sw.search.schema;

import java.io.IOException;

import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;

public abstract class FieldTemplate {

  public abstract Fieldable createField(String fieldName);

  public abstract Fieldable[] setValue(String name, Fieldable[] field, Object val);
  
  public abstract void warmFieldCache(IndexReader reader, String name) throws IOException;

}
