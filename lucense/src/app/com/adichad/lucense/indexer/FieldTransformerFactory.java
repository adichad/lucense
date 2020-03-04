/*
 * @(#)com.adichad.lucense.indexer.FieldTransformerFactory.java
 * ===========================================================================
 * Licensed Materials - Property of InfoEdge 
 * "Restricted Materials of Adichad.Com" 
 * (C) Copyright <TBD> All rights reserved.
 * ===========================================================================
 */
package com.adichad.lucense.indexer;

import org.apache.commons.configuration.tree.ConfigurationNode;

public abstract class FieldTransformerFactory {
  public abstract FieldTransformer getFieldTransformer(ConfigurationNode params) throws Exception;
}
