package com.cleartrip.sw.result;

import java.io.IOException;
import java.io.Writer;

public class AnalyzeResult {
  
  private final StringBuilder sb;

  public AnalyzeResult(StringBuilder sb) {
    this.sb = sb;
  }

  public void writeAsJsonTo(Writer writer) throws IOException {
    writer.append(sb);
  }
}
