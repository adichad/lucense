package com.adichad.lucense.expression;

import java.util.HashMap;
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

  public ValueSources() {
    this.intValueSources = new HashMap<>();
    this.floatValueSources = new HashMap<>();
    this.doubleValueSources = new HashMap<>();
    this.booleanValueSources = new HashMap<>();
    this.stringValueSources = new HashMap<>();
  }
}