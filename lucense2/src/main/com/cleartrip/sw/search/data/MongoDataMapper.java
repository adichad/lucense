package com.cleartrip.sw.search.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.codehaus.jackson.JsonParseException;

import com.cleartrip.sw.search.schema.Schema;

public final class MongoDataMapper implements DataMapper {

  public MongoDataMapper() {
  }

  private class MongoDataMapperState implements DataMapperState {

    private final List<Map<String, Object>> source;
    private final Map<String, Object>       vals;
    private final Schema                    schema;
    private final Document                  doc;
    private int                             counter;

    MongoDataMapperState(List<Map<String, Object>> source, Schema schema) {
      this.source = source;
      this.vals = new HashMap<>();
      this.counter = 0;
      this.schema = schema;
      this.doc = schema.initDoc();
    }

    @Override
    public Document getDocument() {
      // TODO Auto-generated method stub
      return doc;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.cleartrip.sw.search.data.DataMapper#map(com.cleartrip.sw.search.schema
   * .Schema, java.util.List)
   */
  @Override
  public Document map(List<String> skipped, DataMapperState state)
      throws Exception {
    MongoDataMapperState mstate = (MongoDataMapperState) state;

    List<Map<String, Object>> docs = mstate.source;

    if (mstate.counter++ >= docs.size())
      return null;

    Map<String, Object> docMap = docs.get(mstate.counter);

    Document doc = mstate.getDocument();
    try {
      doc = mstate.schema.getDocument(docMap, doc);
      if (doc == null) {
        System.out.println("null for: " + docMap);
        skipped.add((String) docMap.get(mstate.schema.getIdTerm().field()));
      }
    } catch (ClassCastException | NullPointerException e) {
      e.printStackTrace();
      skipped.add((String) docMap.get(mstate.schema.getIdTerm().field()));
    }

    return doc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.cleartrip.sw.search.data.DataMapper#init(java.lang.Object)
   */
  @Override
  public DataMapperState init(Object source, Schema schema) throws Exception {
    MongoDataMapperState state = new MongoDataMapperState(
        (List<Map<String, Object>>) source, schema);

    return state;
  }

  @Override
  public void destroy(DataMapperState state) {

  }

}
