package com.adichad.lucense.grouping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

import com.adichad.lucense.expression.BooleanExpressionTree;
import com.adichad.lucense.expression.DoubleExpressionTree;
import com.adichad.lucense.expression.ExpressionComparatorSource;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.ExpressionTree;
import com.adichad.lucense.expression.FloatExpressionTree;
import com.adichad.lucense.expression.IntExpressionTree;
import com.adichad.lucense.expression.StringExpressionTree;
import com.adichad.lucense.expression.parse.ParseException;

public class GroupingResult {
  Map<List<Comparable<?>>, List<Comparable<?>>> results;

  Map<String, GroupingResult> subresults;

  private LinkedHashMap<String, SortField> selectFields;

  private boolean getresults;

  private final Map<List<Comparable<?>>, List<Comparable<?>>> emptyResult;

  private int totalCount;
  private int subCount;

  public GroupingResult(Map<List<Comparable<?>>, List<Comparable<?>>> baseResult, int baseSize,
      LinkedHashMap<String, SortField> selectFields, Map<String, GenericGroupCriteria> subgroupers, boolean getresults) {
    this.results = baseResult;
    this.totalCount = baseSize;
    this.getresults = getresults;
    if (!getresults)
      this.emptyResult = new HashMap<List<Comparable<?>>, List<Comparable<?>>>(0);
    else
      this.emptyResult = null;
    this.selectFields = selectFields;
    try {
      if (subgroupers != null) {
        this.subresults = new HashMap<String, GroupingResult>();
        for (String gname : subgroupers.keySet()) {
          GenericGroupCriteria grouper = subgroupers.get(gname);
          if (grouper != null) {
            Map<List<Comparable<?>>, List<Comparable<?>>> subRes = getSubGroup(grouper, this.results, selectFields);
            this.subresults.put(gname, new GroupingResult(subRes,subCount,
                grouper.select, grouper.subgroupers, grouper.getresults));
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  abstract class GroupKeySource {
    protected GroupKeySource inner;

    public GroupKeySource(GroupKeySource inner) {
      this.inner = inner;
    }

    protected abstract Object getThisKey(List<Comparable<?>> row);

    protected abstract Object getThisKeyFinal();

    public final void getKeys(List<Comparable<?>> row, List<Object> output) {
      if (this.inner != null)
        this.inner.getKeys(row, output);
      output.add(getThisKey(row));
    }

    public void getKeysFinal(List<Object> output) {
      if (this.inner != null)
        this.inner.getKeysFinal(output);
      output.add(getThisKeyFinal());
    }

    public int length() {
      return 1 + (this.inner != null ? this.inner.length() : 0);
    }

  }

  abstract class ExpressionGroupKeySource extends GroupKeySource {

    private Map<String, Integer> selectFieldIndex;

    public ExpressionGroupKeySource(GroupKeySource inner, Map<String, Integer> selectFieldIndex) {
      super(inner);
      this.selectFieldIndex = selectFieldIndex;
    }

    protected void setVars(List<Comparable<?>> row, ExpressionTree expr) {
      for (String var : expr.getIntVariables()) {
        expr.setIntVariableValue(var, (Integer) row.get(this.selectFieldIndex.get(var)));
      }
      for (String var : expr.getFloatVariables()) {
        expr.setFloatVariableValue(var, (Float) row.get(this.selectFieldIndex.get(var)));
      }
      for (String var : expr.getDoubleVariables()) {
        expr.setDoubleVariableValue(var, (Double) row.get(this.selectFieldIndex.get(var)));
      }
      for (String var : expr.getBooleanVariables()) {
        expr.setBooleanVariableValue(var, (Boolean) row.get(this.selectFieldIndex.get(var)));
      }
      for (String var : expr.getStringVariables()) {
        expr.setStringVariableValue(var, (String) row.get(this.selectFieldIndex.get(var)));
      }

    }

  }

  class IntExpressionGroupKeySource extends ExpressionGroupKeySource {
    private IntExpressionTree expr;

    public IntExpressionGroupKeySource(GroupKeySource inner, IntExpressionTree expr,
        Map<String, Integer> selectFieldIndex) {
      super(inner, selectFieldIndex);
      this.expr = expr;
    }

    @Override
    protected Object getThisKey(List<Comparable<?>> row) {
      setVars(row, this.expr);
      return this.expr.evaluate();
    }

    @Override
    protected Object getThisKeyFinal() {
      return this.expr.evaluateFinal();
    }

  }

  class FloatExpressionGroupKeySource extends ExpressionGroupKeySource {
    private FloatExpressionTree expr;

    public FloatExpressionGroupKeySource(GroupKeySource inner, FloatExpressionTree expr,
        Map<String, Integer> selectFieldIndex) {
      super(inner, selectFieldIndex);
      this.expr = expr;
    }

    @Override
    protected Object getThisKey(List<Comparable<?>> row) {
      setVars(row, this.expr);
      return this.expr.evaluate();
    }

    @Override
    protected Object getThisKeyFinal() {
      return this.expr.evaluateFinal();
    }

  }

  class DoubleExpressionGroupKeySource extends ExpressionGroupKeySource {
    private DoubleExpressionTree expr;

    public DoubleExpressionGroupKeySource(GroupKeySource inner, DoubleExpressionTree expr,
        Map<String, Integer> selectFieldIndex) {
      super(inner, selectFieldIndex);
      this.expr = expr;
    }

    @Override
    protected Object getThisKey(List<Comparable<?>> row) {
      setVars(row, this.expr);
      return this.expr.evaluate();
    }

    @Override
    protected Object getThisKeyFinal() {
      return this.expr.evaluateFinal();
    }

  }

  class BooleanExpressionGroupKeySource extends ExpressionGroupKeySource {
    private BooleanExpressionTree expr;

    public BooleanExpressionGroupKeySource(GroupKeySource inner, BooleanExpressionTree expr,
        Map<String, Integer> selectFieldIndex) {
      super(inner, selectFieldIndex);
      this.expr = expr;
    }

    @Override
    protected Object getThisKey(List<Comparable<?>> row) {
      setVars(row, this.expr);
      return this.expr.evaluate();
    }

    @Override
    protected Object getThisKeyFinal() {
      return this.expr.evaluateFinal();
    }

  }

  class StringExpressionGroupKeySource extends ExpressionGroupKeySource {
    private StringExpressionTree expr;

    public StringExpressionGroupKeySource(GroupKeySource inner, StringExpressionTree expr,
        Map<String, Integer> selectFieldIndex) {
      super(inner, selectFieldIndex);
      this.expr = expr;
    }

    @Override
    protected Object getThisKey(List<Comparable<?>> row) {
      setVars(row, this.expr);
      return this.expr.evaluate();
    }

    @Override
    protected Object getThisKeyFinal() {
      return this.expr.evaluateFinal();
    }

  }

  class SelectedKeySource extends GroupKeySource {
    private Map<String, Integer> selectFieldIndex;

    private String fieldName;

    private Comparable<?> val;

    public SelectedKeySource(GroupKeySource inner, Map<String, Integer> selectFieldIndex, String fieldName) {
      super(inner);
      this.selectFieldIndex = selectFieldIndex;
      this.fieldName = fieldName;
    }

    @Override
    protected Object getThisKey(List<Comparable<?>> row) {
      this.val = row.get(this.selectFieldIndex.get(this.fieldName));
      return this.val;
    }

    @Override
    protected Object getThisKeyFinal() {
      return this.val;
    }

  }

  private Map<List<Comparable<?>>, List<Comparable<?>>> getSubGroup(GenericGroupCriteria grouper,
      Map<List<Comparable<?>>, List<Comparable<?>>> baseResult, LinkedHashMap<String, SortField> selectFields)
      throws ParseException {
    Map<String, Integer> selectFieldIndexes = new HashMap<String, Integer>();

    int i = 0;
    for (String field : selectFields.keySet()) {
      selectFieldIndexes.put(field, i++);
    }

    Map<List<Object>, GroupKeySource> keyedSelectors = new LinkedHashMap<List<Object>, GroupKeySource>();

    Map<List<Comparable<?>>, List<Comparable<?>>> results = new LinkedHashMap<List<Comparable<?>>, List<Comparable<?>>>();

    GroupKeySource groupKeySource = null;

    for (String groupby : grouper.groupfields) {

      if (groupby.startsWith("@expr")) {
        groupby = groupby.substring(5);
        ExpressionTree tree = ExpressionFactory.getExpressionTreeFromString(groupby, grouper.cx, grouper.scope);

        if (tree instanceof IntExpressionTree)
          groupKeySource = new IntExpressionGroupKeySource(groupKeySource, (IntExpressionTree) tree, selectFieldIndexes);
        else if (tree instanceof FloatExpressionTree)
          groupKeySource = new FloatExpressionGroupKeySource(groupKeySource, (FloatExpressionTree) tree,
              selectFieldIndexes);
        else if (tree instanceof DoubleExpressionTree)
          groupKeySource = new DoubleExpressionGroupKeySource(groupKeySource, (DoubleExpressionTree) tree,
              selectFieldIndexes);
        else if (tree instanceof BooleanExpressionTree)
          groupKeySource = new BooleanExpressionGroupKeySource(groupKeySource, (BooleanExpressionTree) tree,
              selectFieldIndexes);
        else if (tree instanceof StringExpressionTree)
          groupKeySource = new StringExpressionGroupKeySource(groupKeySource, (StringExpressionTree) tree,
              selectFieldIndexes);
      } else {
        groupKeySource = new SelectedKeySource(groupKeySource, selectFieldIndexes, groupby);
      }
    }

    GroupKeySource filterSource = null;
    List<Object> filterVals = new ArrayList<Object>(1);

    if (grouper.whereExpr != null) {
      filterSource = new BooleanExpressionGroupKeySource(null,
          (BooleanExpressionTree) grouper.whereExpr.getExpressionTree(), selectFieldIndexes);
    }

    for (List<Comparable<?>> row : baseResult.values()) {
      boolean filterVal = true;
      if (filterSource != null) {
        filterVals.clear();
        filterSource.getKeys(row, filterVals);
        filterVal = (Boolean) filterVals.get(0);
      }
      if (filterVal) {
        List<Object> key = new ArrayList<Object>();
        groupKeySource.getKeys(row, key);
        if (!keyedSelectors.containsKey(key)) {
          GroupKeySource selectKeySource = null;

          for (String select : grouper.select.keySet()) {
            SortField selectField = grouper.select.get(select);
            FieldComparatorSource fcs = selectField.getComparatorSource();
            if (fcs instanceof ExpressionComparatorSource) {
              ExpressionTree tree = ExpressionFactory.getExpressionTreeFromString(selectField.getField(), grouper.cx,
                  grouper.scope);
              if (tree instanceof IntExpressionTree)
                selectKeySource = new IntExpressionGroupKeySource(selectKeySource, (IntExpressionTree) tree,
                    selectFieldIndexes);
              else if (tree instanceof FloatExpressionTree)
                selectKeySource = new FloatExpressionGroupKeySource(selectKeySource, (FloatExpressionTree) tree,
                    selectFieldIndexes);
              else if (tree instanceof DoubleExpressionTree)
                selectKeySource = new DoubleExpressionGroupKeySource(selectKeySource, (DoubleExpressionTree) tree,
                    selectFieldIndexes);
              else if (tree instanceof BooleanExpressionTree)
                selectKeySource = new BooleanExpressionGroupKeySource(selectKeySource, (BooleanExpressionTree) tree,
                    selectFieldIndexes);
              else if (tree instanceof StringExpressionTree)
                selectKeySource = new StringExpressionGroupKeySource(selectKeySource, (StringExpressionTree) tree,
                    selectFieldIndexes);

            } else {

              selectKeySource = new SelectedKeySource(selectKeySource, selectFieldIndexes, selectField.getField());
            }
          }
          keyedSelectors.put(key, selectKeySource);
        }
        filterVals.clear();

        keyedSelectors.get(key).getKeys(row, filterVals);

      }
    }
    subCount = keyedSelectors.size();
    for (List<Object> key : keyedSelectors.keySet()) {

      List<Comparable<?>> resKey = new ArrayList<Comparable<?>>(key.size());
      for (Object keyElem : key) {
        resKey.add((Comparable<?>) keyElem);
      }
      List<Object> select = new ArrayList<Object>();
      GroupKeySource selector = keyedSelectors.get(key);
      List<Comparable<?>> resSel = new ArrayList<Comparable<?>>(key.size());
      selector.getKeysFinal(select);

      for (Object selectElem : select) {
        resSel.add((Comparable<?>) selectElem);
      }

      results.put(resKey, resSel);
    }

    return results;
  }

  public LinkedHashSet<String> getFieldNames() {
    if (this.selectFields == null)
      return null;
    LinkedHashSet<String> list = new LinkedHashSet<String>();
    for (String field : this.selectFields.keySet()) {
      list.add(field);
    }
    return list;
  }

  public Map<List<Comparable<?>>, List<Comparable<?>>> getBaseResult() {
    if (this.getresults)
      return this.results;
    return this.emptyResult;
  }

  public int getBaseSize() {
    return totalCount;
  }
  
  public Map<String, GroupingResult> getSubResults() {
    return this.subresults;
  }
}
