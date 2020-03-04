/*
 * @(#)com.adichad.lucense.resource.AuxIndexManagerConfig.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.resource;

import java.io.FileNotFoundException;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.adichad.lucense.bitmap.AuxIndexManager;
import com.sleepycat.db.DatabaseException;

public class AuxIndexManagerConfig {

  private HashMap<String, String> curAuxEnvConfig;

  private int curMiniHoleSize;

  private HashMap<String, String> curHoleDBConfig;

  private HashMap<String, String> curAuxDBConfig;

  private HashMap<String, String> curMainDBConfig;

  private String auxLucenseField;

  private String curCellDictName;

  private String curAuxIndexName;
  private static final Logger errorLogger = Logger.getLogger("ErrorLogger");
  private static final Logger statusLogger = Logger.getLogger("StatusLogger");

  public AuxIndexManagerConfig(HashMap<String, String> curAuxEnvConfig, int curMiniHoleSize, String curCellDictName,
      HashMap<String, String> curHoleDBConfig, HashMap<String, String> curAuxDBConfig,
      HashMap<String, String> curMainDBConfig, String auxLucenseField, String curAuxIndexName) {
    this.curAuxEnvConfig = curAuxEnvConfig;
    this.curMiniHoleSize = curMiniHoleSize;
    this.curCellDictName = curCellDictName;
    this.curHoleDBConfig = curHoleDBConfig;
    this.curAuxDBConfig = curAuxDBConfig;
    this.curMainDBConfig = curMainDBConfig;
    this.auxLucenseField = auxLucenseField;
    this.curAuxIndexName = curAuxIndexName;
  }

  public AuxIndexManager getAuxIndexManager() throws Exception {
    AuxIndexManager aim = new AuxIndexManager(curAuxEnvConfig, curMiniHoleSize, curCellDictName, curHoleDBConfig, curAuxDBConfig,
      curMainDBConfig, this.auxLucenseField); 
      
    statusLogger.log(Level.INFO, "Aux-Index[" + curAuxIndexName + "] Loaded successfully");
    return aim;
  }

  @Override
  public boolean equals(Object o) {
    if(!(o instanceof AuxIndexManagerConfig))
      return false;
    AuxIndexManagerConfig x = (AuxIndexManagerConfig)o;
    return this.curAuxIndexName == x.curAuxIndexName && this.auxLucenseField == x.auxLucenseField ;
    
  }

}
