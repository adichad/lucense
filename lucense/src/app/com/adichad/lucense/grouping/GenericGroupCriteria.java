/**
 * 
 */
package com.adichad.lucense.grouping;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.BooleanLucenseExpression;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.expression.fieldSource.BooleanValueSource;
import com.adichad.lucense.expression.fieldSource.DoubleValueSource;
import com.adichad.lucense.expression.fieldSource.FloatValueSource;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.StringValueSource;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * @author adichad
 */
public class GenericGroupCriteria extends GroupingCriteria {

  public SortField[] sortfields;

  public int groupoffset;

  public int grouplimit;

  public BooleanLucenseExpression whereExpr;

  public String havingExpr;

  public SearchResourceManager context;

  public Context cx;

  public Map<String, FieldType> fieldTypes;

  public Scriptable scope;

  public Set<String> indexFields;

  public LinkedHashMap<String, SortField> select;

  public Map<String, GenericGroupCriteria> subgroupers;

  public boolean getresults;

  public Map<String, Object2IntOpenHashMap<String>> externalValSource;

  public Map<String, LucenseExpression> namedExprs;

  public GenericGroupCriteria(String[] groupFields, FieldType[] gftypes, Sort sort, int groupoffset, int grouplimit,
      String where, String having, LinkedHashMap<String, SortField> select, SearchResourceManager context,
      HashMap<String, FieldType> fieldTypes, Context cx, Scriptable scope, HashSet<String> expressionFields,
      Map<String, GenericGroupCriteria> subgroupers, boolean getresults,
      Map<String, Object2IntOpenHashMap<String>> externalValSource, Map<String, LucenseExpression> namedExprs,
      Map<String, IntValueSource> intValueSources, Map<String, FloatValueSource> floatValueSources,
      Map<String, DoubleValueSource> doubleValueSources, Map<String, BooleanValueSource> booleanValueSources,
      Map<String, StringValueSource> stringValueSources) throws IOException, ParseException {
    super(groupFields, gftypes, intValueSources, floatValueSources, doubleValueSources, booleanValueSources,
        stringValueSources);
    this.sortfields = sort.getSort();
    this.groupoffset = groupoffset;
    this.grouplimit = grouplimit;
    this.whereExpr = ((where == null) || where.trim().toLowerCase().equals("true")) ? null
        : (BooleanLucenseExpression) ExpressionFactory.getExpressionFromString(where, FieldType.TYPE_BOOLEAN, cx,
            scope, externalValSource, namedExprs, new ValueSources(intValueSources, floatValueSources,
                doubleValueSources, booleanValueSources, stringValueSources), context);
    this.havingExpr = having;
    this.select = select;
    this.context = context;
    this.cx = cx;
    this.scope = scope;
    this.indexFields = expressionFields;
    this.fieldTypes = fieldTypes;
    this.subgroupers = subgroupers;

    this.getresults = getresults;
    this.externalValSource = externalValSource;
    this.namedExprs = namedExprs;

  }

  @Override
  public String toString() {
    String s = "[[";
    for (String gf : this.groupfields)
      s += gf + ", ";
    s += "]";
    return s + "--" + this.select + "\n" + this.subgroupers + "]";
  }

}
