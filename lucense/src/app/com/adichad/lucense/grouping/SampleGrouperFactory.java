package com.adichad.lucense.grouping;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.request.Request;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.request.Request.ScorerType;
import com.adichad.lucense.resource.SearchResourceManager;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class SampleGrouperFactory extends GrouperFactory {

  public SampleGrouperFactory() {
    super();
    System.out.println("SampleGrouperFactory");
  }

  @Override
  public Grouper decoder(DataInputStream dis, Context cx, Scriptable scope,
      HashMap<String, Object2IntOpenHashMap<String>> externalValSource, Map<String, LucenseExpression> namedExprs,
      ValueSources valueSources, SearchResourceManager searchResourceManager, Map<String, Float> readerBoosts,
      HashMap<String, FieldType> fieldTypes, HashSet<String> expressionFields, ScorerType scorerType)
      throws IOException {
    int arrayLen = dis.readInt();
    System.out.println("Item Array Length : " + arrayLen);
    System.out.println("Will print ABC");
    System.out.println(Request.readString(dis));

    System.out.println("Will print Value of ABC");
    System.out.println(Request.readString(dis));
    System.out.println("Will print SS ");
    System.out.println(Request.readString(dis));
    System.out.println("aaaaaaa");
    System.out.println(dis.readInt());
    System.out.println("aaaa           aaaaaaaaaaaaaaaaa");
    System.out.println(dis.readByte());
    System.out.println("dadsfadsffadfdfa");
    System.out.println(dis.readInt());
    return null;

  }
}
