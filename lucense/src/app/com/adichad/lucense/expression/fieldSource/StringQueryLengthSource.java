package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class StringQueryLengthSource extends QueryLengthFieldSource implements StringValueSource {

  public StringQueryLengthSource(String field, Object2IntOpenHashMap<String> map) {
    super(field, map);
  }

  @Override
  public String getValue(Document doc) {
    return null;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public String getValue(int doc) {
    return Integer.valueOf(this.val).toString();
  }

}
