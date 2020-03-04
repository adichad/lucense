package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FieldType;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.expression.fieldSource.ValueSourceFactory;

public abstract class LucenseExpression {

  private ArrayList<IntValueSource>     myIntValSources;

  private ArrayList<FloatValueSource>   myFloatValSources;

  private ArrayList<DoubleValueSource>  myDoubleValSources;

  private ArrayList<BooleanValueSource> myBooleanValSources;

  private ArrayList<StringValueSource>  myStringValSources;

  public LucenseExpression(LucenseExpression expr) {
    this.myIntValSources = expr.myIntValSources;
    this.myFloatValSources = expr.myFloatValSources;
    this.myDoubleValSources = expr.myDoubleValSources;
    this.myBooleanValSources = expr.myBooleanValSources;
    this.myStringValSources = expr.myStringValSources;
  }

  public LucenseExpression(Set<String> intVars, Set<String> floatVars,
      Set<String> doubleVars, Set<String> booleanVars, Set<String> stringVars,
      Map<String, Object2IntOpenHashMap<String>> externalValSources,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources, Set<String> scoreFields,
      Context cx) throws Exception {

    this.myIntValSources = new ArrayList<IntValueSource>(intVars.size());
    this.myFloatValSources = new ArrayList<FloatValueSource>(floatVars.size());
    this.myDoubleValSources = new ArrayList<DoubleValueSource>(
        doubleVars.size());
    this.myBooleanValSources = new ArrayList<BooleanValueSource>(
        booleanVars.size());
    this.myStringValSources = new ArrayList<StringValueSource>(
        stringVars.size());

    for (String field : intVars) {
      if (!valueSources.intValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          valueSources.intValueSources.put(
              field,
              (IntValueSource) ValueSourceFactory.fieldSource(field,
                  namedExprs.get(field), cx));
        else
          valueSources.intValueSources.put(field,
              ValueSourceFactory.intValueSource(field, externalValSources, scoreFields));
      }
      this.myIntValSources.add(valueSources.intValueSources.get(field));
    }
    for (String field : floatVars) {
      if (!valueSources.floatValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          valueSources.floatValueSources.put(field,
              (FloatValueSource) ValueSourceFactory.fieldSource(field,
                  namedExprs.get(field), cx));
        else
          valueSources.floatValueSources.put(field,
              ValueSourceFactory.floatValueSource(field, externalValSources, scoreFields));
      }
      this.myFloatValSources.add(valueSources.floatValueSources.get(field));
    }
    for (String field : doubleVars) {
      if (!valueSources.doubleValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          valueSources.doubleValueSources.put(field,
              (DoubleValueSource) ValueSourceFactory.fieldSource(field,
                  namedExprs.get(field), cx));
        else
          valueSources.doubleValueSources.put(field,
              ValueSourceFactory.doubleValueSource(field, externalValSources));
      }
      this.myDoubleValSources.add(valueSources.doubleValueSources.get(field));
    }
    for (String field : booleanVars) {
      if (!valueSources.booleanValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          valueSources.booleanValueSources.put(field,
              (BooleanValueSource) ValueSourceFactory.fieldSource(field,
                  namedExprs.get(field), cx));
        else 
          valueSources.booleanValueSources.put(field,
              ValueSourceFactory.booleanValueSource(field, externalValSources, scoreFields));
      }
      this.myBooleanValSources.add(valueSources.booleanValueSources.get(field));
    }
    for (String field : stringVars) {
      if (!valueSources.stringValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          valueSources.stringValueSources.put(field,
              (StringValueSource) ValueSourceFactory.fieldSource(field,
                  namedExprs.get(field), cx));
        else
          valueSources.stringValueSources.put(field,
              ValueSourceFactory.stringValueSource(field, externalValSources, scoreFields));
      }
      this.myStringValSources.add(valueSources.stringValueSources.get(field));
    }
  }

  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    for (IntValueSource source : this.myIntValSources) {
      source.setNextReader(reader, docBase);
    }
    for (FloatValueSource source : this.myFloatValSources) {
      source.setNextReader(reader, docBase);
    }
    for (DoubleValueSource source : this.myDoubleValSources) {
      source.setNextReader(reader, docBase);
    }
    for (BooleanValueSource source : this.myBooleanValSources) {
      source.setNextReader(reader, docBase);
    }
    for (StringValueSource source : this.myStringValSources) {
      source.setNextReader(reader, docBase);
    }
  }

  public void setScorer(Scorer scorer) {
    for (IntValueSource source : this.myIntValSources) {
      source.setScorer(scorer);
    }
    for (FloatValueSource source : this.myFloatValSources) {
      source.setScorer(scorer);
    }
    for (DoubleValueSource source : this.myDoubleValSources) {
      source.setScorer(scorer);
    }
    for (BooleanValueSource source : this.myBooleanValSources) {
      source.setScorer(scorer);
    }
    for (StringValueSource source : this.myStringValSources) {
      source.setScorer(scorer);
    }
  }

  @Override
  public abstract LucenseExpression clone();

  public abstract FieldType getType();

  public abstract ExpressionTree getExpressionTree();

  protected void setVals(int doc, Slates slates) throws IOException {
    for (IntValueSource source : this.myIntValSources) {
      slates.setIntVariableValue(source.getName(), source.getValue(doc));
    }
    for (FloatValueSource source : this.myFloatValSources) {
      slates.setFloatVariableValue(source.getName(), source.getValue(doc));
    }
    for (DoubleValueSource source : this.myDoubleValSources) {
      slates.setDoubleVariableValue(source.getName(), source.getValue(doc));
    }
    for (BooleanValueSource source : this.myBooleanValSources) {
      slates.setBooleanVariableValue(source.getName(), source.getValue(doc));
    }
    for (StringValueSource source : this.myStringValSources) {
      slates.setStringVariableValue(source.getName(), source.getValue(doc));
    }
  }

  protected void setVals(Document doc, Slates slates) {
    for (IntValueSource source : this.myIntValSources) {
      slates.setIntVariableValue(source.getName(), source.getValue(doc));
    }
    for (FloatValueSource source : this.myFloatValSources) {
      slates.setFloatVariableValue(source.getName(), source.getValue(doc));
    }
    for (DoubleValueSource source : this.myDoubleValSources) {
      slates.setDoubleVariableValue(source.getName(), source.getValue(doc));
    }
    for (BooleanValueSource source : this.myBooleanValSources) {
      slates.setBooleanVariableValue(source.getName(), source.getValue(doc));
    }
    for (StringValueSource source : this.myStringValSources) {
      slates.setStringVariableValue(source.getName(), source.getValue(doc));
    }
  }
}
