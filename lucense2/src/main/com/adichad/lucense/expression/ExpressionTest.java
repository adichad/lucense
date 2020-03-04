package com.adichad.lucense.expression;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.mozilla.javascript.Context;

import com.adichad.lucense.expression.parse.ExpressionParser;

public class ExpressionTest {
  public static void main(String[] args) {
    Context cx = Context.enter();
    
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    try {
      String str = reader.readLine().trim();
      while (!str.equals("")) {
        try {
          ExpressionTree tree = evaluate(str, reader, cx);
          Slates slates = tree.initState(cx);
          System.out.print("evaluation: ");
          if (tree instanceof IntExpressionTree)
            System.out.println(((IntExpressionTree) tree).evaluate(slates, cx));
          else if (tree instanceof FloatExpressionTree)
            System.out.println(((FloatExpressionTree) tree).evaluate(slates, cx));
          else if (tree instanceof StringExpressionTree)
            System.out.println(((StringExpressionTree) tree).evaluate(slates, cx));
          else if (tree instanceof BooleanExpressionTree)
            System.out.println(((BooleanExpressionTree) tree).evaluate(slates, cx));

        } catch (Exception e) {
          System.err.println(e.getMessage());
          e.printStackTrace();
          System.out.println();
        }
        str = reader.readLine().trim();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      System.out.println("bye.");
      Context.exit();
    }
  }

  private static ExpressionTree evaluate(String str, BufferedReader reader, Context cx) throws Exception {
    ExpressionParser parser = new ExpressionParser(new StringReader(str));
    ExpressionTree tree = parser.parse(null);
    Set<String> intVars = new HashSet<String>();
    Set<String> floatVars = new HashSet<String>();
    Set<String> doubleVars = new HashSet<String>();
    Set<String> booleanVars = new HashSet<String>();
    Set<String> stringVars = new HashSet<String>();

    Slates slates = tree.initState(cx);
    intVars.addAll(tree.getIntVariables());
    floatVars.addAll(tree.getFloatVariables());
    doubleVars.addAll(tree.getDoubleVariables());
    booleanVars.addAll(tree.getBooleanVariables());
    stringVars.addAll(tree.getStringVariables());
    while (!(intVars.isEmpty() && floatVars.isEmpty() && doubleVars.isEmpty() && booleanVars.isEmpty() && stringVars
        .isEmpty())) {
      for (String var : intVars) {
        System.out.print("[" + var + "(int)] ");
        String val = reader.readLine().trim();
        slates.setIntVariableValue(var, Integer.parseInt(val));
      }
      for (String var : floatVars) {
        System.out.print("[" + var + "(int)] ");
        String val = reader.readLine().trim();
        slates.setFloatVariableValue(var, Float.parseFloat(val));
      }
      for (String var : doubleVars) {
        System.out.print("[" + var + "(int)] ");
        String val = reader.readLine().trim();
        slates.setDoubleVariableValue(var, Double.parseDouble(val));
      }
      for (String var : booleanVars) {
        System.out.print("[" + var + "(int)] ");
        String val = reader.readLine().trim();
        slates.setBooleanVariableValue(var, Boolean.parseBoolean(val));
      }
      for (String var : stringVars) {
        System.out.print("[" + var + "(int)] ");
        String val = reader.readLine().trim();
        slates.setStringVariableValue(var, val);
      }
    }
    return tree;
  }
}
