/**
 * 
 */
package com.adichad.lucense.result;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;

import com.browseengine.bobo.api.BrowseFacet;
import com.browseengine.bobo.api.BrowseResult;
import com.browseengine.bobo.api.FacetAccessible;

import com.adichad.lucense.grouping.GroupingResult;

/**
 * @author adichad
 */
public class SearchResult implements Result {
  private int requestID;

  private Document[] retdocs;

  private int displayCount;

  private String[] selectFields;

  private HashMap<String, GroupingResult> groupings;

  private Map<String, Map<String, Set<String>>> highlightables;

  protected static final String response = "SEARCH";

  public SearchResult(int requestID) {
    this.requestID = requestID;
    this.displayCount = 0;
    this.groupings = new HashMap<String, GroupingResult>();
  }

  /*
   * (non-Javadoc)
   * @see com.adichad.lucense.request.Result#writeTo(java.io.OutputStream)
   */
  @Override
  public void writeTo(OutputStream out) throws IOException {
    DataOutputStream dos = new DataOutputStream(out);
    dos.writeByte(2);
    dos.writeInt(this.requestID);
    dos.writeInt(this.displayCount);
    dos.writeInt(this.retdocs.length);
    for (Document doc : this.retdocs) {
      List<Field> fields = new ArrayList<Field>();

      for (String fieldname : this.selectFields) {
        Fieldable[] selected = doc.getFieldables(fieldname);
        for (Fieldable f : selected) {
          fields.add((Field) f);
        }
      }
      dos.writeInt(fields.size());
      for (Field field : fields) {
        String name = field.name();
        dos.writeInt(name.length());
        dos.writeBytes(name);
        String value = doc.get(name);
        dos.writeInt(value.length());
        dos.writeBytes(value);
      }
    }

    writeGroupings(this.groupings, dos);
    if (this.highlightables != null) {
      dos.writeByte(1);
      dos.writeInt(this.highlightables.size());
      for (String field : this.highlightables.keySet()) {
        dos.writeInt(field.length());
        dos.writeBytes(field);
        Map<String, Set<String>> invMap = this.highlightables.get(field);
        dos.writeInt(invMap.size());
        for (String invOrig : invMap.keySet()) {
          dos.writeInt(invOrig.length());
          dos.writeBytes(invOrig);
          Set<String> invs = invMap.get(invOrig);
          dos.writeInt(invs.size());
          for (String inv : invs) {
            dos.writeInt(inv.length());
            dos.writeBytes(inv);
          }
        }
      }
    } else
      dos.writeByte(0);
  }

  private void writeGroupings(Map<String, GroupingResult> groupings, DataOutputStream dos) throws IOException {
    if (groupings == null) {
      dos.writeInt(0);
      return;
    }
    dos.writeInt(groupings.size());
    for (String gname : groupings.keySet()) {
      GroupingResult groupres = groupings.get(gname);
      
      Map<List<Comparable<?>>, List<Comparable<?>>> grouping = groupres.getBaseResult();

      dos.writeInt(gname.length());
      dos.writeBytes(gname);
      dos.writeInt(groupres.getBaseSize());
      if (groupres.getFieldNames() == null)
        dos.writeInt(0);
      else {
        dos.writeInt(groupres.getFieldNames().size());
        for (String field : groupres.getFieldNames()) {
          dos.writeInt(field.length());
          dos.writeBytes(field);
        }
      }
      dos.writeInt(grouping.keySet().size());
      for (List<Comparable<?>> key : grouping.keySet()) {
        dos.writeInt(key.size());
        for (Comparable<?> keyElem : key) {
          if (keyElem != null) {
            dos.writeInt(keyElem.toString().length());
            dos.writeBytes(keyElem.toString());
          } else {
            dos.writeInt("(null)".length());
            dos.writeBytes("(null)");
          }
        }
        List<Comparable<?>> selectList = grouping.get(key);
        dos.writeInt(selectList.size());
        for (Comparable<?> val : selectList) {
          String str = val.toString();
          dos.writeInt(str.length());
          dos.writeBytes(str);
        }
      }
      writeGroupings(groupres.getSubResults(), dos);
    }

  }

  @Override
  public void readFrom(InputStream in) throws IOException {

  }

  public void setResults(Document[] retdocs) {
    this.retdocs = retdocs;
  }

  public void setDisplayCount(int displayCount) {
    this.displayCount = displayCount;
  }

  public void setSelectFields(String[] selectedFields) {
    this.selectFields = selectedFields;
  }

  public void addGrouping(String gname, GroupingResult result) {
    this.groupings.put(gname, result);
  }

  public void addHighlightables(Map<String, Map<String, Set<String>>> inversions) {
    this.highlightables = inversions;
  }

  public void addBoboGroupings(BrowseResult bresult) {
    Map<String, FacetAccessible> facetMap = bresult.getFacetMap();
    for (String name : facetMap.keySet()) {
      FacetAccessible entry = facetMap.get(name);
      List<BrowseFacet> facets = entry.getFacets();
      Map<List<Comparable<?>>, List<Comparable<?>>> grouping = new LinkedHashMap<List<Comparable<?>>, List<Comparable<?>>>();

      for (BrowseFacet facet : facets) {
        List<Comparable<?>> key = new ArrayList<Comparable<?>>();
        key.add(facet.getValue());
        List<Comparable<?>> val = new ArrayList<Comparable<?>>();
        val.add(facet.getFacetValueHitCount());
        grouping.put(key, val);
      }
      LinkedHashMap<String, SortField> selectFields = new LinkedHashMap<String, SortField>();
      selectFields.put("count", null);
      this.groupings.put(name, new GroupingResult(grouping, 0, selectFields, null, true));
    }
  }
}
