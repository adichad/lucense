/**
 * Request.java
 */
package com.adichad.lucense.request;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.search.SortField;

import com.adichad.lucense.resource.SearchResourceManager;

/**
 * @author adichad
 * 
 */
public abstract class Request {
  protected Socket sock;

  protected int version;

  protected int id;

  protected enum SortMode {
    SORT_SQL, SORT_CMP;

    public static SortMode getSortMode(byte b) {
      return (b == 1) ? SORT_CMP : SORT_SQL;
    }

    @Override
    public String toString() {
      return (name().equals("SORT_SQL")) ? "sql" : "cmp";
    }
  }

  public enum ScorerType {
    SCORE_DEFAULT, SCORE_LCSFIELD, SCORE_BOOLFIELD, SCORE_EXPRESSION, SCORE_NONE;

    public static ScorerType getScorerType(byte b) {
      switch (b) {
      case 0:
        return SCORE_DEFAULT;
      case 1:
        return SCORE_LCSFIELD;
      case 2:
        return SCORE_BOOLFIELD;
      case 3:
        return SCORE_EXPRESSION;
      default:
        return SCORE_NONE;
      }
    }

    @Override
    public String toString() {
      switch (ordinal()) {
      case 0:
        return "def";
      case 1:
        return "lcs";
      case 2:
        return "fld";
      case 3:
        return "exp";
      default:
        return "nil";
      }
    }
  }

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

    public static int getSortFieldType(HashMap<String, FieldType> fieldTypes, String name) {
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

  protected abstract void readFrom(InputStream in) throws Exception;

  protected abstract void sendTo(OutputStream out) throws IOException;

  public static String readString(DataInputStream dis) throws IOException {
    int len = dis.readInt();
    byte[] b = new byte[len];
    dis.readFully(b, 0, len);
    return new String(b);
  }

  public static byte[] readStringInBytes(DataInputStream dis) throws IOException {
    int len = dis.readInt();
    byte[] b = new byte[len];
    dis.readFully(b, 0, len);
    return b;

  }

  public Request(Socket sock, int version, int id) {
    this.sock = sock;
    this.version = version;
    this.id = id;
  }

  public void process(SearchResourceManager context, ExecutorService executor, boolean closeSock) {
    try {
      readFrom(this.sock.getInputStream());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
/**
 * Request.java ENDS
 */
