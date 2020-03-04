package com.adichad.lucense.expression.fieldSource;

import java.util.HashMap;

import org.apache.lucene.search.SortField;

public enum FieldType {
  TYPE_STRING, TYPE_INT, TYPE_DOUBLE, TYPE_FLOAT, TYPE_BOOLEAN, TYPE_ARRAY;

  public static FieldType getFieldType(byte b) {
    switch (b) {
    case 0:
      return TYPE_STRING;
    case 1:
      return TYPE_INT;
    case 2:
      return TYPE_DOUBLE;
    case 3:
      return TYPE_FLOAT;
    case 4:
      return TYPE_BOOLEAN;
    case 5:
      return TYPE_ARRAY;
    default:
      return TYPE_INT;
    }
  }

  public static FieldType getFieldType(String type) {
    type = type.toLowerCase();
    if (type.equals("boolean"))
      return TYPE_BOOLEAN;
    if (type.equals("string"))
      return TYPE_STRING;
    if (type.equals("integer"))
      return TYPE_INT;
    if (type.equals("double"))
      return TYPE_DOUBLE;
    if (type.equals("float"))
      return TYPE_FLOAT;

    if (type.equals("array"))
      return TYPE_ARRAY;
    return TYPE_INT;

  }

  public static int getSortFieldType(HashMap<String, FieldType> fieldTypes,
      String name) {
    if (fieldTypes.containsKey(name)) {
      switch (fieldTypes.get(name)) {
      case TYPE_STRING:
        return SortField.STRING;
      case TYPE_INT:
        return SortField.INT;
      case TYPE_DOUBLE:
        return SortField.DOUBLE;
      case TYPE_FLOAT:
        return SortField.FLOAT;
      case TYPE_BOOLEAN:
        return SortField.INT;
      default:
        return SortField.INT;
      }
    } else {
      return SortField.INT;
    }
  }

  public static int getSortFieldType(FieldType type) {
    switch (type) {
    case TYPE_STRING:
      return SortField.STRING;
    case TYPE_INT:
      return SortField.INT;
    case TYPE_DOUBLE:
      return SortField.DOUBLE;
    case TYPE_FLOAT:
      return SortField.FLOAT;
    case TYPE_BOOLEAN:
      return SortField.INT;
    default:
      return SortField.INT;
    }
  }

  @Override
  public String toString() {
    switch (ordinal()) {
    case 0:
      return "str";
    case 1:
      return "int";
    case 2:
      return "dbl";
    case 3:
      return "flt";
    case 4:
      return "bln";
    case 5:
      return "arr";
    default:
      return "int";
    }
  }

}
