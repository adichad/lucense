package com.cleartrip.sw.search.data;

import java.util.List;

import org.apache.lucene.document.Document;

import com.cleartrip.sw.search.schema.Schema;

public interface DataMapper {

  public abstract Document map(List<String> skipped,
      DataMapperState state) throws Exception;

  public abstract DataMapperState init(Object reader, Schema schema)
      throws Exception;

  public abstract void destroy(DataMapperState state) throws Exception;
}
