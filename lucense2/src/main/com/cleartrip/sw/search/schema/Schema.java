package com.cleartrip.sw.search.schema;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;

public class Schema {

  private final Term                        idTerm;
  private final Map<String, FieldTemplate>  optionalFields;
  private final Map<Pattern, FieldTemplate> patternFields;
  private final Map<String, FieldTemplate>  requiredFields;

  public Schema(String idField, Map<String, FieldTemplate> requiredFields,
      Map<String, FieldTemplate> optionalFields,
      Map<Pattern, FieldTemplate> patternFields) {
    this.idTerm = new Term(idField, "");
    this.requiredFields = requiredFields;
    this.optionalFields = optionalFields;
    this.patternFields = patternFields;
  }

  public void warmFieldCache(IndexReader reader, Set<String> fields)
      throws IOException {
    for (String field : fields) {
      FieldTemplate ft;
      if ((ft = requiredFields.get(field)) != null
          || (ft = optionalFields.get(field)) != null) {
        ft.warmFieldCache(reader, field);
      } else {
        for (Pattern pat : patternFields.keySet()) {
          if (pat.matcher(field).matches()) {
            patternFields.get(pat).warmFieldCache(reader, field);
            break;
          }
        }
      }
    }
  }

  public Document initDoc() {
    Document doc = new Document();
    for (String fieldName : Schema.this.requiredFields.keySet()) {
      FieldTemplate template = Schema.this.requiredFields.get(fieldName);
      doc.add(template.createField(fieldName));
    }
    return doc;
  }

  public Term getIdTerm() {
    return idTerm;
  }

  public Document getDocument(Map<String, Object> docMap, Document doc) {
    String idVal = docMap.get(idTerm.field()).toString();
    for (Map.Entry<String, FieldTemplate> e : requiredFields.entrySet()) {
      String name = e.getKey();
      Object val;
      if ((val = docMap.get(name)) == null) {
        throw new RuntimeException("required field: " + name
            + " not found in doc: " + idVal);
      }
      FieldTemplate template = e.getValue();
      Fieldable[] fields = doc.getFieldables(name);
      doc.removeFields(name);

      try {
        fields = template.setValue(name, fields, val);
      } catch (ClassCastException ex) {
        throw new RuntimeException("required field: " + name
            + " not converted in doc: " + idVal, ex);
      }
      for (Fieldable field : fields) {
        doc.add(field);
      }
    }
    docMap.keySet().removeAll(requiredFields.keySet());

    for (Map.Entry<String, FieldTemplate> e : optionalFields.entrySet()) {
      String name = e.getKey();
      FieldTemplate template = e.getValue();
      Fieldable[] fields = doc.getFieldables(name);
      doc.removeFields(name);
      try {
        fields = template.setValue(name, fields, docMap.get(name));
      } catch (ClassCastException ex) {
        throw new RuntimeException("optional field: " + name
            + " not converted in doc: " + idVal, ex);
      }

      for (Fieldable field : fields) {
        doc.add(field);
      }
    }
    docMap.keySet().removeAll(optionalFields.keySet());

    Set<String> removables = new HashSet<>();
    for (Map.Entry<Pattern, FieldTemplate> e : patternFields.entrySet()) {
      Pattern pattern = e.getKey();
      FieldTemplate template = e.getValue();
      List<Fieldable> docFields = doc.getFields();
      Set<String> removableDocFields = new HashSet<>();
      for (Fieldable field : docFields) {
        String name = field.name();
        if (pattern.matcher(name).matches())
          removableDocFields.add(name);
      }
      removableDocFields.removeAll(requiredFields.keySet());
      removableDocFields.removeAll(optionalFields.keySet());
      for (String name : removableDocFields) {
        doc.removeFields(name);
      }
      for (String name : docMap.keySet()) {
        if (pattern.matcher(name).matches()) {
          Fieldable[] fields = doc.getFieldables(name);
          doc.removeFields(name);

          try {
            fields = template.setValue(name, fields, docMap.get(name));
          } catch (ClassCastException ex) {
            throw new RuntimeException("pattern-matched field: " + name
                + " not converted in doc: " + idVal, ex);
          }

          for (Fieldable field : fields) {
            doc.add(field);
          }
          removables.add(name);
        }
      }
    }
    docMap.keySet().removeAll(removables);

    return doc;
  }

}
