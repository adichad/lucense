package com.adichad.lucense.expression;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.expression.fieldSource.ValueSourceFactory;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class LucenseExpression {

  private ArrayList<IntValueSource> myIntValSources;

  private ArrayList<FloatValueSource> myFloatValSources;

  private ArrayList<DoubleValueSource> myDoubleValSources;

  private ArrayList<BooleanValueSource> myBooleanValSources;

  private ArrayList<StringValueSource> myStringValSources;

  public LucenseExpression(LucenseExpression expr) {
    this.myIntValSources = expr.myIntValSources;
    this.myFloatValSources = expr.myFloatValSources;
    this.myDoubleValSources = expr.myDoubleValSources;
    this.myBooleanValSources = expr.myBooleanValSources;
    this.myStringValSources = expr.myStringValSources;

  }

  public LucenseExpression(Set<String> intVars, Set<String> floatVars, Set<String> doubleVars, Set<String> booleanVars,
      Set<String> stringVars, Map<String, Object2IntOpenHashMap<String>> externalValSources,
      Map<String, LucenseExpression> namedExprs, Map<String, IntValueSource> intValueSources,
      Map<String, FloatValueSource> floatValueSources, Map<String, DoubleValueSource> doubleValueSources,
      Map<String, StringValueSource> stringValueSources, Map<String, BooleanValueSource> booleanValueSources,
      SearchResourceManager srm) throws Exception {

    this.myIntValSources = new ArrayList<IntValueSource>(intVars.size());
    this.myFloatValSources = new ArrayList<FloatValueSource>(floatVars.size());
    this.myDoubleValSources = new ArrayList<DoubleValueSource>(doubleVars.size());
    this.myBooleanValSources = new ArrayList<BooleanValueSource>(booleanVars.size());
    this.myStringValSources = new ArrayList<StringValueSource>(stringVars.size());

    for (String field : intVars) {
      if (!intValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          intValueSources.put(field, (IntValueSource) ValueSourceFactory.fieldSource(field, namedExprs.get(field)));
        else
          intValueSources.put(field, ValueSourceFactory.intValueSource(field, externalValSources));
      }
      this.myIntValSources.add(intValueSources.get(field));
    }
    for (String field : floatVars) {
      if (!floatValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          floatValueSources.put(field, (FloatValueSource) ValueSourceFactory.fieldSource(field, namedExprs.get(field)));
        else
          floatValueSources.put(field, ValueSourceFactory.floatValueSource(field, externalValSources));
      }
      this.myFloatValSources.add(floatValueSources.get(field));
    }
    for (String field : doubleVars) {
      if (!doubleValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          doubleValueSources.put(field,
              (DoubleValueSource) ValueSourceFactory.fieldSource(field, namedExprs.get(field)));
        else
          doubleValueSources.put(field, ValueSourceFactory.doubleValueSource(field, externalValSources));
      }
      this.myDoubleValSources.add(doubleValueSources.get(field));
    }
    for (String field : booleanVars) {
      if (!booleanValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          booleanValueSources.put(field,
              (BooleanValueSource) ValueSourceFactory.fieldSource(field, namedExprs.get(field)));
        else
          booleanValueSources.put(field, ValueSourceFactory.booleanValueSource(field, externalValSources, srm));
      }
      this.myBooleanValSources.add(booleanValueSources.get(field));
    }
    for (String field : stringVars) {
      if (!stringValueSources.containsKey(field)) {
        if (namedExprs.containsKey(field))
          stringValueSources.put(field,
              (StringValueSource) ValueSourceFactory.fieldSource(field, namedExprs.get(field)));
        else
          stringValueSources.put(field, ValueSourceFactory.stringValueSource(field, externalValSources));
      }
      this.myStringValSources.add(stringValueSources.get(field));
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

  protected void setVals(int doc, ExpressionTree expr) throws IOException {
    for (IntValueSource source : this.myIntValSources) {
      expr.setIntVariableValue(source.getName(), source.getValue(doc));
    }
    for (FloatValueSource source : this.myFloatValSources) {
      expr.setFloatVariableValue(source.getName(), source.getValue(doc));
    }
    for (DoubleValueSource source : this.myDoubleValSources) {
      expr.setDoubleVariableValue(source.getName(), source.getValue(doc));
    }
    for (BooleanValueSource source : this.myBooleanValSources) {
      expr.setBooleanVariableValue(source.getName(), source.getValue(doc));
    }
    for (StringValueSource source : this.myStringValSources) {
      expr.setStringVariableValue(source.getName(), source.getValue(doc));
    }
  }

  protected void setVals(Document doc, ExpressionTree expr) {
    for (IntValueSource source : this.myIntValSources) {
      expr.setIntVariableValue(source.getName(), source.getValue(doc));
    }
    for (FloatValueSource source : this.myFloatValSources) {
      expr.setFloatVariableValue(source.getName(), source.getValue(doc));
    }
    for (DoubleValueSource source : this.myDoubleValSources) {
      expr.setDoubleVariableValue(source.getName(), source.getValue(doc));
    }
    for (BooleanValueSource source : this.myBooleanValSources) {
      expr.setBooleanVariableValue(source.getName(), source.getValue(doc));
    }
    for (StringValueSource source : this.myStringValSources) {
      expr.setStringVariableValue(source.getName(), source.getValue(doc));
    }
  }
}
