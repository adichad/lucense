package com.adichad.lucense.expression;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.util.HashMap;
import java.util.Map;

public class VarContext {
  public Object2IntOpenHashMap<String>     intVals     = new Object2IntOpenHashMap<String>();
  public Object2FloatOpenHashMap<String>   floatVals   = new Object2FloatOpenHashMap<String>();
  public Object2DoubleOpenHashMap<String>  doubleVals  = new Object2DoubleOpenHashMap<String>();
  public Object2BooleanOpenHashMap<String> booleanVals = new Object2BooleanOpenHashMap<String>();
  public Map<String, String>               stringVals  = new HashMap<String, String>();

  public VarContext() {
  }
}
