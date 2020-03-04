package com.adichad.lucense.expression.fieldSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adichad.lucense.bitmap.AuxIndexManager;
import com.adichad.lucense.bitmap.CellDictionaryHandler;
import com.adichad.lucense.bitmap.Row;
import com.adichad.lucense.expression.BooleanLucenseExpression;
import com.adichad.lucense.expression.DoubleLucenseExpression;
import com.adichad.lucense.expression.FloatLucenseExpression;
import com.adichad.lucense.expression.IntLucenseExpression;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.resource.SearchResourceManager;
import com.sleepycat.db.DatabaseException;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ValueSourceFactory {
  public static Set<String> scoreFields = new HashSet<String>();

  public static IntValueSource intValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map) {
    if (map == null)
      return intValueSource(field);
    IntValueSource valSource = null;
    if (field.startsWith("_qlen") && map.containsKey("_qlen")) {
      String infield = field.substring(6, field.length() - 1);
      valSource = new IntegerQueryLengthSource(infield, map.get("_qlen"));
    }
    if (valSource == null)
      return intValueSource(field);

    return valSource;
  }

  public static FloatValueSource floatValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map) {
    if (map == null)
      return floatValueSource(field);
    FloatValueSource valSource = null;
    if (field.startsWith("_qlen") && map.containsKey("_qlen")) {
      String infield = field.substring(6, field.length() - 1);
      valSource = new FloatQueryLengthSource(infield, map.get("_qlen"));
    }
    if (valSource == null)
      return floatValueSource(field);

    return valSource;
  }

  public static DoubleValueSource doubleValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map) {
    return doubleValueSource(field);
  }

  public static BooleanValueSource booleanValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map,
      SearchResourceManager srm) throws Exception {
    return booleanValueSource(field, srm);
  }

  public static StringValueSource stringValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map) {
    if (map == null)
      return stringValueSource(field);
    StringValueSource valSource = null;
    if (field.startsWith("_qlen") && map.containsKey("_qlen")) {
      String infield = field.substring(6, field.length() - 1);
      valSource = new StringQueryLengthSource(infield, map.get("_qlen"));
    }
    if (valSource == null)
      return stringValueSource(field);

    return valSource;
  }

  public static IntValueSource intValueSource(String field) {
    IntValueSource valSource = null;
    if (field.equals("_docid")) {
      valSource = new IntegerDocIdSource();
    } else if (field.equals("_score")) {
      valSource = new IntegerScoreSource();
    } else if (field.startsWith("_numwords")) {
      String infield = field.substring(10, field.length() - 1);
      valSource = new IntegerNumwordsSource(infield, scoreFields);
    } else if (field.startsWith("_lcslen")) {
      String infield = field.substring(8, field.length() - 1);
      valSource = new IntegerLCSLengthSource(infield, scoreFields);
    } else
      valSource = new IntFieldSource(field);

    return valSource;
  }

  public static FloatValueSource floatValueSource(String field) {
    FloatValueSource valSource = null;
    if (field.equals("_docid"))
      valSource = new FloatDocIdSource();
    else if (field.equals("_score"))
      valSource = new FloatScoreSource();
    else if (field.startsWith("_numwords")) {
      String infield = field.substring(10, field.length() - 1);
      valSource = new FloatNumwordsSource(infield, scoreFields);
    } else if (field.startsWith("_lcslen")) {
      String infield = field.substring(8, field.length() - 1);
      valSource = new FloatLCSLengthSource(infield, scoreFields);
    } else
      valSource = new FloatFieldSource(field);

    return valSource;
  }

  public static DoubleValueSource doubleValueSource(String field) {
    DoubleValueSource valSource = null;
    valSource = new DoubleFieldSource(field);

    return valSource;
  }

  public static BooleanValueSource booleanValueSource(String field, SearchResourceManager srm) throws DatabaseException {
    BooleanValueSource valSource = null;
    if (field.startsWith("_isexact")) {
      String infield = field.substring(9, field.length() - 1);
      valSource = new BooleanIsExactSource(infield, scoreFields);
    } else if (field.startsWith("_isall")) {
      String infield = field.substring(7, field.length() - 1);
      valSource = new BooleanIsAllSource(infield, scoreFields);
    } else if (field.startsWith("_auxlookup")) {
      String[] temp = field.substring(11, field.length() - 1).split("_");
      String indexName = temp[0];
      String rowid = temp[1];
      AuxIndexManager im = srm.getAuxIndexer(indexName);
      valSource = new BooleanAuxIndexValueSource(field, im.getLucenseFieldName(), im.getRowHandler().loadRow(
          rowid.getBytes()), im.getCellDictionary());
    } else
      valSource = new BooleanFieldSource(field);

    return valSource;
  }

  public static StringValueSource stringValueSource(String field) {
    StringValueSource valSource = null;
    if (field.equals("_docid"))
      valSource = new StringDocIdSource();
    else if (field.equals("_score"))
      valSource = new StringScoreSource();
    else if (field.startsWith("_numwords")) {
      String infield = field.substring(10, field.length() - 1);
      valSource = new StringNumwordsSource(infield, scoreFields);
    } else if (field.startsWith("_lcslen")) {
      String infield = field.substring(8, field.length() - 1);
      valSource = new StringLCSLengthSource(infield, scoreFields);
    } else
      valSource = new StringFieldSource(field);

    return valSource;
  }

  public static ValueSource fieldSource(String field, LucenseExpression expr) {
    ValueSource fieldSource;
    // System.out.println("3 value source creation: "+field);
    switch (expr.getType()) {
    case TYPE_INT:
      fieldSource = new IntegerExpressionFieldSource(field, (IntLucenseExpression) expr);
      break;
    case TYPE_BOOLEAN:
      fieldSource = new BooleanExpressionFieldSource(field, (BooleanLucenseExpression) expr);
      break;
    case TYPE_FLOAT:
      fieldSource = new FloatExpressionFieldSource(field, (FloatLucenseExpression) expr);
      break;
    case TYPE_STRING:
      fieldSource = new StringExpressionFieldSource(field, (StringLucenseExpression) expr);
      break;
    case TYPE_DOUBLE:
      fieldSource = new DoubleExpressionFieldSource(field, (DoubleLucenseExpression) expr);
      break;
    default:
      fieldSource = new StringExpressionFieldSource(field, (StringLucenseExpression) expr);
      break;
    }
    return fieldSource;
  }

}
