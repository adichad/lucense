/*
 * @(#)com.adichad.lucense.expression.ExpressionParsingTest.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.expression;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.Context;

public class ExpressionParsingTest {
  public ArrayList<String> exprs;
  @Before
  public void setup() {
    exprs = new ArrayList<String>();
    exprs.add("if([string]RESDEX_FLAG==\"d\"||[string]RESDEX_FLAG==\"c\", \"ignore\", \"merge\")");
    exprs.add("if([int]RESID==3||[int]RESID==3, \"ignore\", \"merge\")");
  }
  
  @Test
  public void testGetExpressionTreeFromString() {
    try {
      Context cx = Context.enter();
      for(String expr : exprs) {
        ExpressionFactory.getExpressionTreeFromString(expr, cx, null);
      }
      
    } catch (Throwable e) {
      //e.getCause().printStackTrace();
      fail(e.getMessage());
    } finally {
      Context.exit();
    }
    
  }

}
