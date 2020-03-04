package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class FloatQueryLengthSource extends QueryLengthFieldSource implements FloatValueSource {

  public FloatQueryLengthSource(String field, Object2IntOpenHashMap<String> map) {
    super(field, map);
  }

  @Override
  public float getValue(Document doc) {
    return 0f; // new FloatExpressionTree(new FloatLiteral(null), null);
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public float getValue(int doc) {
    return this.val;
  }

}
