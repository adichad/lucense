package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

import com.cleartrip.sw.search.analysis.filters.payload.ConcatAndPayloadFilter;

public class ConcatAndPayloadFilterSource extends TokenFilterSource {

  private final int offset;
  private final int len;

  public ConcatAndPayloadFilterSource(Map<String, ?> params, Properties env)
      throws Exception {
    super(params, env);
    this.offset = (Integer) params.get("payloadOffset");
    this.len = (Integer) params.get("payloadLen");
  }

  @Override
  public TokenFilter getTokenStream(TokenStream tokenStream) {
    return new ConcatAndPayloadFilter(tokenStream, offset, len);
  }

}
