package com.cleartrip.sw.search.searchj;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopFieldDocs;
import org.codehaus.jackson.io.JsonStringEncoder;
import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.ExpressionCollector;
import com.adichad.lucense.expression.ExpressionCollectorFactory;
import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.cleartrip.sw.search.filters.SearchFilter;

public class CustomFieldSelector implements FieldSelector {

  private static final long                       serialVersionUID = 1L;
  private final Map<Pattern, ReturnFieldFormat>   fieldPatterns;
  private final Map<String, ReturnFieldFormat>    nameFields;
  private Map<String, ExpressionCollectorFactory> expressions;

  public CustomFieldSelector(Map<String, ReturnFieldFormat> nameFields,
      Map<Pattern, ReturnFieldFormat> fieldPatterns,
      Map<String, ExpressionCollectorFactory> expressions) {
    this.nameFields = nameFields;
    this.fieldPatterns = fieldPatterns;
    this.expressions = expressions;
  }

  @Override
  public FieldSelectorResult accept(String fieldName) {
    if (nameFields.containsKey(fieldName))
      return FieldSelectorResult.LOAD;
    for (Pattern pattern : fieldPatterns.keySet()) {
      // System.out.println("matching("+pattern+"): "+fieldName);
      if (pattern.matcher(fieldName).matches()) {
        // System.out.println("matched("+pattern+"): "+fieldName);
        return FieldSelectorResult.LOAD;
      }
    }
    return FieldSelectorResult.NO_LOAD;
  }

  public void decantAsJson(Document doc, StringBuilder sb,
      SearchFilter lastFilter, IndexSearcher searcher) throws IOException {
    JsonStringEncoder encoder = JsonStringEncoder.getInstance();
    List<Fieldable> fields = doc.getFields();
    Map<String, List<String>> mdoc = new LinkedHashMap<>(fields.size());

    for (Fieldable field : fields) {
      String name = field.name();
      String val = field.stringValue();
      if (!mdoc.containsKey(name)) {
        mdoc.put(name, new LinkedList<String>());
      }
      mdoc.get(name).add(val);
    }
    /*
     * if (lastFilter != null) { List<String> placeTypes =
     * mdoc.get("place_type_geo_planet"); if (placeTypes != null) { List<String>
     * ids = mdoc.get("geo_path_ids"); if (ids != null) { List<String> names =
     * mdoc.get("geo_path_names"); for (int i = 0; i < ids.size(); i++) { String
     * placeType = ""; if (!(placeType = lastFilter.selectByValue(ids.get(i),
     * searcher)) .equals("")) { String name = names.remove(i); name += " (" +
     * placeType + ")"; names.add(i, name); } } } } }
     */
    sb.append("{");

    for (String name : mdoc.keySet()) {
      sb.append("\"").append(name).append("\"").append(": ");

      boolean found = false;
      if (nameFields.containsKey(name)) {
        nameFields.get(name).formatAsJSON(sb, mdoc.get(name), encoder);
        found = true;
      } else {
        for (Pattern pat : fieldPatterns.keySet()) {
          if (pat.matcher(name).matches()) {
            fieldPatterns.get(pat).formatAsJSON(sb, mdoc.get(name), encoder);
            found = true;
            break;
          }
        }
      }
      if (!found) {
        List<String> vals = mdoc.get(name);
        if (vals.size() > 1)
          sb.append("[");
        int j = 0;
        for (String val : vals) {
          if (j != 0)
            sb.append(", ");
          sb.append("\"").append(new String(encoder.quoteAsUTF8(val)))
              .append("\"");
          j++;
        }
        if (vals.size() > 1)
          sb.append("]");

      }
      sb.append(",");
    }
    sb.deleteCharAt(sb.length() - 1).append("}\n");
  }

  public static void main(String[] args) {
    JsonStringEncoder encoder = new JsonStringEncoder();
    System.out.println("Hello.\"KOLKATA");
    System.out.println(new String(encoder.quoteAsUTF8("Hello.\"KOLKATA")));
    System.out.println(new String(encoder.quoteAsUTF8(new String(encoder
        .quoteAsUTF8("Hello.\"KOLKATA")))));
    System.out.println(new String(encoder.quoteAsUTF8("Hello.\"KOLKATA"))
        .equals("Hello.\"KOLKATA"));
    System.out.println(new String(encoder.quoteAsUTF8("Hello.\\\"KOLKATA")));
  }

  public Document fillAuxFields(IndexSearcher searcher, Query query,
      Document sdoc, ScoreDoc sd, TopFieldDocs tfd) throws IOException {
    if (this.nameFields.keySet().contains("@score")) {
      sdoc.add(new Field("@score", new Float(sd.score).toString(), Store.YES,
          Index.NO));
    }
    if (this.nameFields.keySet().contains("@doc")) {
      sdoc.add(new Field("@doc", new Integer(sd.doc).toString(), Store.YES,
          Index.NO));
    }
    if (this.nameFields.keySet().contains("@explain")) {
      sdoc.add(new Field("@explain",
          searcher.explain(query, sd.doc).toString(), Store.YES, Index.NO));
    }

    for (int i = 0; i < tfd.fields.length; i++) {

      if (this.nameFields.containsKey(tfd.fields[i].getField())) {
        sdoc.add(new Field(tfd.fields[i].getField(), ((FieldDoc) sd).fields[i]
            .toString(), Store.YES, Index.NO));
      }
    }
    /*
     * display term vector TermFreqVector tvec =
     * searcher.getIndexReader().getTermFreqVector(sd.doc, "guide_text"); if
     * (tvec != null) { List<String> terms = new ArrayList<>(); for (String term
     * : tvec.getTerms()) { terms.add(term); } sdoc.add(new
     * Field("guide_text_vector", terms.toString(), Store.YES, Index.NO)); }
     */
    return sdoc;
  }

  public Document fillExpressionValues(
      Map<String, ExpressionCollector> collectors, Document sdoc, ScoreDoc sd) {
    for (String ecfName : collectors.keySet()) {
      ExpressionCollector col = collectors.get(ecfName);
      sdoc.add(new Field(ecfName, col.getVal(sd.doc).toString(), Store.YES,
          Index.NO));
    }
    return sdoc;
  }

  public Collector getExpressionCollectors(Collector c,
      Map<String, Object2IntOpenHashMap<String>> externalValSource,
      Map<String, LucenseExpression> namedExprs, ValueSources valueSources,
      Set<String> scoreFields, Map<String, ExpressionCollector> collectors,
      Context cx) throws Exception {
    for (String ecfName : this.expressions.keySet()) {
      ExpressionCollectorFactory ecf = this.expressions.get(ecfName);
      c = ecf.getCollector(c, externalValSource, namedExprs, valueSources,
          scoreFields, cx);
      collectors.put(ecfName, (ExpressionCollector) c);
    }
    return c;
  }

}
