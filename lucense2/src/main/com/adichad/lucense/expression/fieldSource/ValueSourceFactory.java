package com.adichad.lucense.expression.fieldSource;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.Map;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.BooleanLucenseExpression;
import com.adichad.lucense.expression.DoubleLucenseExpression;
import com.adichad.lucense.expression.FloatLucenseExpression;
import com.adichad.lucense.expression.IntLucenseExpression;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.StringLucenseExpression;

public class ValueSourceFactory {
  //public static Set<String> scoreFields = new HashSet<String>();

  public static IntValueSource intValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map, Set<String> scoreFields) {
    if (map == null)
      return intValueSource(field, scoreFields);
    IntValueSource valSource = null;
    if (field.startsWith("_qlen") && map.containsKey("_qlen")) {
      String infield = field.substring(6, field.length() - 1);
      valSource = new IntegerQueryLengthSource(infield, map.get("_qlen"));
    }
    if (valSource == null)
      return intValueSource(field, scoreFields);

    return valSource;
  }

  public static FloatValueSource floatValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map, Set<String> scoreFields) {
    if (map == null)
      return floatValueSource(field, scoreFields);
    FloatValueSource valSource = null;
    if (field.startsWith("_qlen") && map.containsKey("_qlen")) {
      String infield = field.substring(6, field.length() - 1);
      valSource = new FloatQueryLengthSource(infield, map.get("_qlen"));
    }
    if (valSource == null)
      return floatValueSource(field, scoreFields);

    return valSource;
  }

  public static DoubleValueSource doubleValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map) {
    return doubleValueSource(field);
  }

  public static BooleanValueSource booleanValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map, Set<String> scoreFields
     ) throws Exception {
    return booleanValueSource(field, scoreFields);
  }

  public static StringValueSource stringValueSource(String field, Map<String, Object2IntOpenHashMap<String>> map, Set<String> scoreFields) {
    if (map == null)
      return stringValueSource(field, scoreFields);
    StringValueSource valSource = null;
    if (field.startsWith("_qlen") && map.containsKey("_qlen")) {
      String infield = field.substring(6, field.length() - 1);
      valSource = new StringQueryLengthSource(infield, map.get("_qlen"));
    }
    if (valSource == null)
      return stringValueSource(field, scoreFields);

    return valSource;
  }

  public static IntValueSource intValueSource(String field, Set<String> scoreFields) {
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
    } else if (field.startsWith("_fieldnorm")) {
      String infield = field.substring(11, field.length() - 1);
      valSource = new IntegerFieldNormSource(infield);
    } else if (field.startsWith("_boostminpos")) {
      String infield = field.substring(13, field.length() - 1);
      valSource = new IntegerMinPosPayloadBoostSource(infield, scoreFields, 1);
    } else
      valSource = new IntFieldSource(field);

    return valSource;
  }

  public static FloatValueSource floatValueSource(String field, Set<String> scoreFields) {
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

  public static BooleanValueSource booleanValueSource(String field, Set<String> scoreFields) {
    BooleanValueSource valSource = null;
    if (field.startsWith("_isexact")) {
      String infield = field.substring(9, field.length() - 1);
      valSource = new BooleanIsExactSource(infield, scoreFields);
    } else if (field.startsWith("_isall")) {
      String infieldstr = field.substring(7, field.length() - 1);
      String[] infields = infieldstr.split("@");
      valSource = new BooleanIsAllSource(scoreFields, infields);
    } else if (field.startsWith("_isfullexact")) {
      String infieldstr = field.substring(13, field.length() - 1);
      String[] infields = infieldstr.split("@");
      valSource = new BooleanIsFullExactSource(scoreFields, infields);
    } else 
      valSource = new BooleanFieldSource(field);

    return valSource;
  }

  public static StringValueSource stringValueSource(String field, Set<String> scoreFields) {
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

  public static ValueSource fieldSource(String field, LucenseExpression expr, Context cx) {
    ValueSource fieldSource;
    // System.out.println("3 value source creation: "+field);
    switch (expr.getType()) {
    case TYPE_INT:
      fieldSource = new IntegerExpressionFieldSource(field, (IntLucenseExpression) expr, cx);
      break;
    case TYPE_BOOLEAN:
      fieldSource = new BooleanExpressionFieldSource(field, (BooleanLucenseExpression) expr, cx);
      break;
    case TYPE_FLOAT:
      fieldSource = new FloatExpressionFieldSource(field, (FloatLucenseExpression) expr, cx);
      break;
    case TYPE_STRING:
      fieldSource = new StringExpressionFieldSource(field, (StringLucenseExpression) expr, cx);
      break;
    case TYPE_DOUBLE:
      fieldSource = new DoubleExpressionFieldSource(field, (DoubleLucenseExpression) expr, cx);
      break;
    default:
      fieldSource = new StringExpressionFieldSource(field, (StringLucenseExpression) expr, cx);
      break;
    }
    return fieldSource;
  }

}
