package com.adichad.lucense.expression.fieldSource;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;

import org.apache.lucene.document.Document;

public class IntegerQueryLengthSource extends QueryLengthFieldSource implements IntValueSource {

  public IntegerQueryLengthSource(String field, Object2IntOpenHashMap<String> map) {
    super(field, map);
  }

  @Override
  public int getValue(Document doc) {
    return 0;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(int doc) {
    return this.val;
  }

}
