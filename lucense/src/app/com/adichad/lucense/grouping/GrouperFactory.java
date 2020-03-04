package com.adichad.lucense.grouping;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.request.Request.ScorerType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public abstract class GrouperFactory {

  public abstract Grouper decoder(DataInputStream dis, Context cx, Scriptable scope,
      HashMap<String, Object2IntOpenHashMap<String>> externalValSource, Map<String, LucenseExpression> namedExprs,
      ValueSources valueSources, SearchResourceManager searchResourceManager, Map<String, Float> readerBoosts,
      HashMap<String, FieldType> fieldTypes, HashSet<String> expressionFields, ScorerType scorerType)
      throws IOException;

  public GrouperFactory() {}

}
