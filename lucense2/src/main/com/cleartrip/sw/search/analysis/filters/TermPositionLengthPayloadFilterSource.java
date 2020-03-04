package com.cleartrip.sw.search.analysis.filters;

import java.util.Map;
import java.util.Properties;

import org.apache.lucene.analysis.TokenStream;

import com.cleartrip.sw.search.analysis.filters.payload.TermPositionLengthPayloadFilter;

public class TermPositionLengthPayloadFilterSource extends TokenFilterSource {

  private final int offset;
  private final int len;
  private final int termOffset;

  public TermPositionLengthPayloadFilterSource(Map<String, ?> params,
      Properties env) throws Exception {
    super(params, env);
    this.offset = (Integer) params.get("payloadOffset");
    this.len = (Integer) params.get("payloadLen");
    this.termOffset = (Integer) params.get("termOffset");
  }

  @Override
  public TokenStream getTokenStream(TokenStream tokenStream) {
    return new TermPositionLengthPayloadFilter(tokenStream, offset, len,
        termOffset);
  }

}
