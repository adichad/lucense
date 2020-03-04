package com.adichad.lucense.grouping;

import java.util.Map;

import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.request.Request.FieldType;

public abstract class GroupingCriteria {
  public String[] groupfields;

  public FieldType[] gftypes;

  public Map<String, IntValueSource> intValueSources;

  public Map<String, FloatValueSource> floatValueSources;

  public Map<String, DoubleValueSource> doubleValueSources;

  public Map<String, BooleanValueSource> booleanValueSources;

  public Map<String, StringValueSource> stringValueSources;

  public GroupingCriteria(String[] groupfields, FieldType[] gftypes, Map<String, IntValueSource> intValueSources,
      Map<String, FloatValueSource> floatValueSources, Map<String, DoubleValueSource> doubleValueSources,
      Map<String, BooleanValueSource> booleanValueSources, Map<String, StringValueSource> stringValueSources) {
    this.groupfields = groupfields;
    this.gftypes = gftypes;
    this.intValueSources = intValueSources;
    this.floatValueSources = floatValueSources;
    this.doubleValueSources = doubleValueSources;
    this.booleanValueSources = booleanValueSources;
    this.stringValueSources = stringValueSources;
  }

  // public abstract void init(DataInputStream dis);

}