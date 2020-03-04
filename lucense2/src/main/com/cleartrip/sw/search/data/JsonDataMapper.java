package com.cleartrip.sw.search.data;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import com.cleartrip.sw.search.schema.Schema;

public final class JsonDataMapper implements DataMapper {

  private static final JsonFactory jf = new JsonFactory();

  private static class JsonDataMapperState implements DataMapperState {

    private final Document            doc;
    private final JsonParser          parser;
    private final Map<String, Object> vals;
    private final Schema              schema;

    JsonDataMapperState(JsonParser parser, Schema schema) {
      this.schema = schema;
      this.doc = schema.initDoc();
      this.parser = parser;
      this.vals = new HashMap<String, Object>();
    }

    @Override
    public Document getDocument() {
      return doc;
    }

    JsonParser parser() {
      return parser;
    }

    Map<String, Object> vals() {
      return vals;
    }

  }

  public JsonDataMapper() {
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
    JsonDataMapperState mstate = (JsonDataMapperState) state;
    JsonParser parser = mstate.parser();
    String fieldName = null;
    Map<String, Object> docMap = mstate.vals;
    Document doc = mstate.doc;
    boolean skipDocument = false;
    do {
      if (parser.nextToken() != JsonToken.START_OBJECT)
        return null;
      docMap.clear();
      JsonToken tok;

      skipDocument = false;
      try {
        while ((tok = parser.nextToken()) != null
            && tok != JsonToken.END_OBJECT) {
          switch (tok) {
          case VALUE_STRING: {
            docMap.put(fieldName, parser.getText());
            break;
          }
          case VALUE_FALSE: {
            docMap.put(fieldName, "false");
            break;
          }
          case VALUE_TRUE: {
            docMap.put(fieldName, "true");
            break;
          }
          case VALUE_NUMBER_FLOAT: {
            docMap.put(fieldName, parser.getDoubleValue());
            break;
          }
          case VALUE_NUMBER_INT: {
            docMap.put(fieldName, parser.getIntValue());
            break;
          }
          case VALUE_NULL: {
            break;
          }

          case START_ARRAY: {
            List<Object> vals = new LinkedList<Object>();
            while ((tok = parser.nextToken()) != null
                && tok != JsonToken.END_ARRAY) {
              switch (tok) {
              case VALUE_STRING: {
                vals.add(parser.getText());
                break;
              }
              case VALUE_FALSE: {
                vals.add("false");
                break;
              }
              case VALUE_TRUE: {
                vals.add("true");
                break;
              }
              case VALUE_NUMBER_FLOAT: {
                vals.add(parser.getDoubleValue());
                break;
              }
              case VALUE_NUMBER_INT: {
                vals.add(parser.getIntValue());
                break;
              }
              case VALUE_NULL: {
                break;
              }
              case START_ARRAY: {
                short i = 1;

                while (i != 0 && (tok = parser.nextToken()) != null) {
                  // skip nesting
                  switch (tok) {
                  case START_ARRAY: {
                    i++;
                    break;
                  }
                  case END_ARRAY: {
                    i--;
                    break;
                  }

                  default:
                    break;
                  }

                }
                break;
              }
              case START_OBJECT: {
                short i = 1;

                while (i != 0 && (tok = parser.nextToken()) != null) {
                  // skip nesting
                  switch (tok) {
                  case START_OBJECT: {
                    i++;
                    break;
                  }
                  case END_OBJECT: {
                    i--;
                    break;
                  }

                  default:
                    break;
                  }

                }
                break;
              }

              default:
                break;
              }
            }
            docMap.put(fieldName, vals.toArray());
            break;
          }
          case START_OBJECT: {
            short i = 1;

            while (i != 0 && (tok = parser.nextToken()) != null) {
              // skip nesting
              switch (tok) {
              case START_OBJECT: {
                i++;
                break;
              }
              case END_OBJECT: {
                i--;
                break;
              }

              default:
                break;
              }

            }
            break;
          }
          case FIELD_NAME: {
            fieldName = parser.getCurrentName();
            break;
          }
          default:
            break;
          }

        }
        doc = mstate.schema.getDocument(docMap, doc);
        if (doc == null) {
          skipDocument = true;
          
          if (skipped.size() < 5)
            skipped.add((String) docMap.get(mstate.schema.getIdTerm().field()));
        }
      } catch (Exception e) {
        skipDocument = true;
        if (skipped.size() < 5) {
          e.printStackTrace();
          skipped.add(e.getMessage());
        }
        
      }
    } while (skipDocument);
    return doc;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.cleartrip.sw.search.data.DataMapper#init(java.lang.Object)
   */
  @Override
  public DataMapperState init(Object reader, Schema schema) throws JsonParseException,
      IOException {
    JsonParser parser = jf.createJsonParser((Reader) reader);
    parser.nextToken();// skip [
    return new JsonDataMapperState(parser, schema);
  }

  public void destroy(DataMapperState state) throws Exception {
    JsonDataMapperState mstate = (JsonDataMapperState) state;
    mstate.parser.close();
  }
}
