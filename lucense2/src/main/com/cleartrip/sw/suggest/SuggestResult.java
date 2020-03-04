package com.cleartrip.sw.suggest;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;

public class SuggestResult {

  public final Set<String> results;

  public SuggestResult(Set<String> results) {
    this.results = results;
  }

  public void writeAsJsonTo(Writer writer) throws IOException {
    writer.append("[");
    int i = 0;
    for (String result : results) {
      if (i++ > 0)
        writer.append(", ");
      writer.append("\"");
      writer.append(result);
      writer.append("\"");
    }

    writer.append("]");
  }

}
