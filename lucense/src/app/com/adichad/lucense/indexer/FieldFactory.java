/**
 * 
 */
package com.adichad.lucense.indexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.StringLucenseExpression;
import com.adichad.lucense.expression.parse.ParseException;
import com.adichad.lucense.resource.IndexerConfigParser.FieldModifier;

/**
 * @author adichad
 * 
 */
public class FieldFactory {
  private HashMap<String, FieldModifier> fieldModifiers;
  private LinkedHashMap<Pattern, FieldModifier> regexModifiers;

  private List<Field> knownFields;

  private final HashMap<String, Integer> fieldPositions;

  private HashMap<String, FieldModifier> transformingMods;

  public FieldFactory(HashMap<String, FieldModifier> fieldModifiers, LinkedHashMap<Pattern, FieldModifier> regexModifiers) {
    this.fieldModifiers = fieldModifiers;
    this.regexModifiers = regexModifiers;
    this.transformingMods = new HashMap<String, FieldModifier>();
    this.knownFields = new ArrayList<Field>();
    this.fieldPositions = new HashMap<String, Integer>();

    for (String modName : fieldModifiers.keySet()) {
      if (((fieldModifiers.get(modName).getTransformString() != null) && !fieldModifiers.get(modName)
          .getTransformString().trim().equals(""))
          || fieldModifiers.get(modName).transformer != null) {
        this.transformingMods.put(modName, fieldModifiers.get(modName));
      }
    }
  }

  public void addContext(Context cx) throws ParseException {
    for (FieldModifier modifier : this.fieldModifiers.values()) {
      modifier.addContext(cx);
    }
  }

  public Field createField(FieldModifier mod, String fieldName) {
    Field f;
    Field.Store store;
    Field.Index index;
    Field.TermVector termv;
    store = mod.getStoreMode();
    index = mod.getIndexMode();
    termv = mod.getTermVector();

    switch (mod.getFieldType()) {
    case TYPE_STRING:
      f = new Field(fieldName, "", store, index, termv);
      break;
    case TYPE_INT:
      f = new Field(fieldName, new NumericTokenStream().setIntValue(Integer.parseInt("")), termv);
      break;
    case TYPE_DOUBLE:
      f = new Field(fieldName, new NumericTokenStream().setDoubleValue(Double.parseDouble("")), termv);
      break;
    case TYPE_FLOAT:
      f = new Field(fieldName, new NumericTokenStream().setFloatValue(Float.parseFloat("")), termv);
      break;
    default:
      f = new Field(fieldName, "", store, index, termv);
      break;
    }
    f.setOmitNorms(mod.getOmitNorms());
    return f;
  }
  
  public Field initField(String columnLabel) {
    Field f;
    if (this.fieldModifiers.containsKey(columnLabel)) {
      f = createField(fieldModifiers.get(columnLabel), columnLabel);
    } else {
      
      Field.Store store;
      Field.Index index;
      Field.TermVector termv;
      store = Field.Store.NO;
      index = Field.Index.ANALYZED_NO_NORMS;
      termv = Field.TermVector.YES;
      f = new Field(columnLabel, "", store, index, termv);
      f.setOmitNorms(true);
    }

    knownFields.add(f);
    fieldPositions.put(columnLabel, knownFields.size() - 1);
    return f;
  }

  public Field getField(int i, String string) {
    Field f = knownFields.get(i);
    string = (string == null) ? "" : string;
    f.setValue(string);
    return f;
  }

  public Field getField(String name, String string) {
    Field f = knownFields.get(fieldPositions.get(name));
    string = (string == null) ? "" : string;
    f.setValue(string);
    return f;
  }

  public boolean hasField(String name) {
    return fieldPositions.containsKey(name);
  }
  
  
  public void transform(Document doc, Context cx) {
    for (String name : this.transformingMods.keySet()) {
      FieldModifier fm = this.fieldModifiers.get(name);
      if (fm.getTransformString() != null && !fm.getTransformString().trim().equals("")) {
        StringLucenseExpression trans = fm.getTransform(cx);
        ((Field) doc.getFieldable(name)).setValue(trans.evaluate(doc).toString());
      } else {
        fm.transformer.transform(doc, cx);
      }
    }
  }
  
  
  public Map<String, StringLucenseExpression> getTransformExpressions(Context cx, Map<String, StringLucenseExpression> transformExpressions) throws ParseException {
    if(transformExpressions == null) {
      transformExpressions = new HashMap<String, StringLucenseExpression>();
    }
    for(String name : this.transformingMods.keySet()) {
      FieldModifier fm = this.fieldModifiers.get(name);
      if (fm.getTransformString() != null && !fm.getTransformString().trim().equals("")) {
        StringLucenseExpression trans = fm.getTransformExpression(cx);
        transformExpressions.put(name, trans);
      }
    }
    return transformExpressions;
  }
  
  public List<FieldTransformer> getCustomTransforms(Context cx, List<FieldTransformer> customTransforms) {
    if(customTransforms == null) {
      customTransforms = new LinkedList<FieldTransformer>();
    }
    for(String name : this.transformingMods.keySet()) {
      FieldModifier fm = this.fieldModifiers.get(name);
      if (fm.getTransformString() == null || fm.getTransformString().trim().equals("")) {
        customTransforms.add(fm.transformer);
      }
    }
    return customTransforms;
  }
  
  public void transformNew(Document doc, Map<String, StringLucenseExpression> transformExpressions, List<FieldTransformer> customTransforms, Context cx) throws ParseException {
    for (String name : transformExpressions.keySet()) {
      StringLucenseExpression trans = transformExpressions.get(name);
      ((Field) doc.getFieldable(name)).setValue(trans.evaluate(doc).toString());
    }
    
    for(FieldTransformer trans: customTransforms) {
      trans.transform(doc, cx);
    }
  }

  public List<Field> initConcreteFields(Document doc) {
    List<Field> fields = new ArrayList<Field>(fieldModifiers.size());
    for(FieldModifier mod: fieldModifiers.values()) {
      Field f = createField(mod, mod.getFieldName());
      fields.add(f);
      doc.add(f);
    }
    return fields;
  }

  public void scrapeConcreteFields(List<Field> cfields, Map<String, String> mdoc) {
    for(Field field: cfields) {
      field.setValue(mdoc.remove(field.name()));
    }
  }

  public List<Field> scrapeSoftFields(Map<String, String> mdoc) {
    List<Field> fields = new LinkedList<Field>();
    List<String> currfields = new LinkedList<String>();
    for(Pattern regex: regexModifiers.keySet()) {
      FieldModifier mod = regexModifiers.get(regex);
      currfields.clear();
      for(String fn: mdoc.keySet()) {
        if(regex.matcher(fn).matches()) {
          Field f = createField(mod, fn);
          f.setValue(mdoc.get(fn));
          fields.add(f);
          currfields.add(fn);
        }
      }
      mdoc.keySet().removeAll(currfields);
    }
    return fields;
  }

}
