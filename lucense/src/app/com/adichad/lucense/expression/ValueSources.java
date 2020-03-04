package com.adichad.lucense.expression;

import java.util.Map;

import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;

public class ValueSources {
  public Map<String, IntValueSource> intValueSources;

  public Map<String, FloatValueSource> floatValueSources;

  public Map<String, DoubleValueSource> doubleValueSources;

  public Map<String, BooleanValueSource> booleanValueSources;

  public Map<String, StringValueSource> stringValueSources;

  public ValueSources(Map<String, IntValueSource> intValueSources, Map<String, FloatValueSource> floatValueSources,
      Map<String, DoubleValueSource> doubleValueSources, Map<String, BooleanValueSource> booleanValueSources,
      Map<String, StringValueSource> stringValueSources) {
    this.intValueSources = intValueSources;
    this.floatValueSources = floatValueSources;
    this.doubleValueSources = doubleValueSources;
    this.booleanValueSources = booleanValueSources;
    this.stringValueSources = stringValueSources;
  }
}