package com.adichad.lucense.grouping;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.lucene.search.IndexBoostComparatorSource;
import org.apache.lucene.search.LCSLengthComparatorSource;
import org.apache.lucene.search.NumwordsComparatorSource;
import org.apache.lucene.search.QueryLenComparatorSource;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.ExpressionComparatorSource;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.expression.fieldSource.IntValueSource;
import com.adichad.lucense.expression.fieldSource.ValueSourceFactory;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.request.Request;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.request.Request.ScorerType;
import com.adichad.lucense.resource.SearchResourceManager;
import com.sleepycat.db.DatabaseException;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class DoAllGenericGrouperFactory extends GrouperFactory {

  public DoAllGenericGrouperFactory() {
    super();

  }

  @Override
  public Grouper decoder(DataInputStream dis, Context cx, Scriptable scope,
      HashMap<String, Object2IntOpenHashMap<String>> externalValSource, Map<String, LucenseExpression> namedExprs,
      ValueSources valueSources, SearchResourceManager srm, Map<String, Float> readerBoosts,
      HashMap<String, FieldType> fieldTypes, HashSet<String> expressionFields, ScorerType scorerType)
      throws IOException {
    GenericGrouper grouper = null;
    try {
      Map<String, GenericGroupCriteria> crits = decodeCriteria(dis, cx, scope, externalValSource, namedExprs,
          valueSources, srm, readerBoosts, fieldTypes, expressionFields);
      for (String name : crits.keySet()) {
        grouper = new DoAllGenericGrouper(name, crits.get(name), grouper, srm);
      }

    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    return grouper;

  }

  private Map<String, GenericGroupCriteria> decodeCriteria(DataInputStream dis, Context cx, Scriptable scope,
      HashMap<String, Object2IntOpenHashMap<String>> externalValSource, Map<String, LucenseExpression> namedExprs,
      ValueSources valueSources, SearchResourceManager srm, Map<String, Float> readerBoosts,
      HashMap<String, FieldType> fieldTypes, HashSet<String> expressionFields) throws IOException, ParseException,
      DatabaseException {
    int len = dis.readInt();
    Map<String, GenericGroupCriteria> crits = new HashMap<String, GenericGroupCriteria>(len);
    for (int i = 0; i < len; i++) {
      String gname = Request.readString(dis);

      int mlen = dis.readInt();
      String[] groupFields = new String[mlen];
      FieldType[] gftypes = new FieldType[mlen];
      for (int j = 0; j < mlen; j++) {
        String groupfield = Request.readString(dis);
        FieldType grouperType = FieldType.getFieldType(dis.readByte());
        gftypes[j] = grouperType;
        if (groupfield.equals("@score")) {
          if (!valueSources.floatValueSources.containsKey("_score")) {
            valueSources.floatValueSources.put("_score", ValueSourceFactory.floatValueSource("_score"));
          }
          groupFields[j] = "_score";
        } else if (groupfield.equals("@indexboost")) {
          if (!valueSources.intValueSources.containsKey("_indexboost")) {
            valueSources.intValueSources.put("_indexboost", ValueSourceFactory.intValueSource("_indexboost"));
          }
          groupFields[j] = "_indexboost";
        } else if (groupfield.equals("@docid")) {
          if (!valueSources.intValueSources.containsKey("_docid")) {
            valueSources.intValueSources.put("_docid", ValueSourceFactory.intValueSource("_docid"));
          }
          groupFields[j] = "_docid";
        } else if (groupfield.startsWith("@expr")) {

          LucenseExpression expr = ExpressionFactory.getExpressionFromString(groupfield.substring(5), grouperType, cx,
              scope, externalValSource, namedExprs, valueSources, srm);
          switch (expr.getType()) {
          case TYPE_INT:
            if (!valueSources.intValueSources.containsKey(groupfield))
              valueSources.intValueSources.put(groupfield,
                  (IntValueSource) ValueSourceFactory.fieldSource(groupfield, expr));
          }
          groupFields[j] = groupfield;
        } else if (groupfield.startsWith("@numwords")) {
          // groupfield = groupfield.substring(10, groupfield.length() - 1);
          // groupFields[j] = new SortField(groupfield, new
          // NumwordsComparatorSource());
        } else if (groupfield.startsWith("@lcslen")) {
          // groupfield = groupfield.substring(8, groupfield.length() - 1);
          // groupFields[j] = new SortField(groupfield, new
          // LCSLengthComparatorSource());
        } else if (groupfield.startsWith("@qlen")) {
          // groupfield = groupfield.substring(6, groupfield.length() - 1);
          // groupFields[j] = new SortField(groupfield, new
          // QueryLenComparatorSource(externalValSource.get("_qlen")));
        } else {
          switch (grouperType) {
          case TYPE_INT:
            valueSources.intValueSources.put(groupfield, ValueSourceFactory.intValueSource(groupfield));
            break;
          case TYPE_STRING:
            valueSources.stringValueSources.put(groupfield, ValueSourceFactory.stringValueSource(groupfield));
            break;
          case TYPE_FLOAT:
            valueSources.floatValueSources.put(groupfield, ValueSourceFactory.floatValueSource(groupfield));
            break;
          case TYPE_DOUBLE:
            valueSources.doubleValueSources.put(groupfield, ValueSourceFactory.doubleValueSource(groupfield));
            break;
          case TYPE_BOOLEAN:
            valueSources.booleanValueSources.put(groupfield, ValueSourceFactory.booleanValueSource(groupfield, srm));
            break;
          }
          groupFields[j] = groupfield;
        }
      }
      mlen = dis.readInt();
      SortField[] sortfields = new SortField[mlen];
      for (int j = 0; j < mlen; j++) {
        String sortfield = Request.readString(dis);
        FieldType sorterType = FieldType.getFieldType(dis.readByte());
        if (sortfield.equals("@score")) {
          sortfields[j] = SortField.FIELD_SCORE;
          dis.readByte();
        } else if (sortfield.equals("@indexboost")) {
          sortfields[j] = new SortField(sortfield, new IndexBoostComparatorSource(readerBoosts), dis.readByte() != 0);
        } else if (sortfield.equals("@docid")) {
          sortfields[j] = SortField.FIELD_DOC;
          dis.readByte();
        } else if (sortfield.startsWith("@expr")) {
          sortfield = sortfield.substring(5);
          sortfields[j] = new SortField(sortfield, new ExpressionComparatorSource(sorterType, cx, scope,
              externalValSource, namedExprs, valueSources.intValueSources, valueSources.floatValueSources,
              valueSources.doubleValueSources, valueSources.booleanValueSources, valueSources.stringValueSources, srm),
              dis.readByte() != 0);
        } else if (sortfield.startsWith("@numwords")) {
          sortfield = sortfield.substring(10, sortfield.length() - 1);
          sortfields[j] = new SortField(sortfield, new NumwordsComparatorSource(), dis.readByte() != 0);
        } else if (sortfield.startsWith("@lcslen")) {
          sortfield = sortfield.substring(8, sortfield.length() - 1);
          sortfields[j] = new SortField(sortfield, new LCSLengthComparatorSource(), dis.readByte() != 0);
        } else if (sortfield.startsWith("@qlen")) {
          sortfield = sortfield.substring(6, sortfield.length() - 1);
          sortfields[j] = new SortField(sortfield, new QueryLenComparatorSource(externalValSource.get("_qlen")),
              dis.readByte() != 0);
        } else {
          sortfields[j] = new SortField(sortfield, FieldType.getSortFieldType(sorterType), dis.readByte() != 0);
        }
      }
      Sort gsort = new Sort(sortfields);
      int groupoffset = dis.readInt();
      int grouplimit = dis.readInt();

      String where = Request.readString(dis);
      String having = Request.readString(dis);
      LinkedHashMap<String, SortField> select = new LinkedHashMap<String, SortField>();
      mlen = dis.readInt();

      for (int j = 0; j < mlen; j++) {
        String key = Request.readString(dis);
        String selectfield = Request.readString(dis);
        FieldType selectType = FieldType.getFieldType(dis.readByte());
        SortField selField;
        if (selectfield.equals("@score")) {
          selField = SortField.FIELD_SCORE;
        } else if (selectfield.equals("@indexboost")) {
          selField = new SortField(selectfield, new IndexBoostComparatorSource(srm.getReaderBoosts()));
        } else if (selectfield.equals("@docid")) {
          selField = SortField.FIELD_DOC;
        } else if (selectfield.startsWith("@expr")) {
          selectfield = selectfield.substring(5);
          selField = new SortField(selectfield, new ExpressionComparatorSource(selectType, cx, scope,
              externalValSource, namedExprs, valueSources.intValueSources, valueSources.floatValueSources,
              valueSources.doubleValueSources, valueSources.booleanValueSources, valueSources.stringValueSources, srm));
        } else if (selectfield.startsWith("@numwords")) {
          selectfield = selectfield.substring(10, selectfield.length() - 1);
          selField = new SortField(selectfield, new NumwordsComparatorSource());
        } else if (selectfield.startsWith("@lcslen")) {
          selectfield = selectfield.substring(8, selectfield.length() - 1);
          selField = new SortField(selectfield, new LCSLengthComparatorSource());
        } else if (selectfield.startsWith("@qlen")) {
          selectfield = selectfield.substring(6, selectfield.length() - 1);
          selField = new SortField(selectfield, new QueryLenComparatorSource(externalValSource.get("_qlen")));
        } else {
          selField = new SortField(selectfield, FieldType.getSortFieldType(fieldTypes, selectfield));
        }
        select.put(key, selField);
      }
      Map<String, GenericGroupCriteria> subcrits = decodeCriteria(dis, cx, scope, externalValSource, namedExprs,
          valueSources, srm, readerBoosts, fieldTypes, expressionFields);
      crits.put(gname, new GenericGroupCriteria(groupFields, gftypes, gsort, groupoffset, grouplimit, where, having,
          select, srm, fieldTypes, cx, scope, expressionFields, subcrits, dis.readByte() == 0 ? false : true,
          externalValSource, namedExprs, valueSources.intValueSources, valueSources.floatValueSources,
          valueSources.doubleValueSources, valueSources.booleanValueSources, valueSources.stringValueSources));

    }

    return crits;
  }

}
