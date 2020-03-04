package com.cleartrip.sw.search.searchj;

import java.io.IOException;
import java.io.Writer;

public class SearchResult {

  private String json;

  private int    totalCount;

  public void setDocumentJson(String string) {
    this.json = string;
    // TODO Auto-generated method stub

  }

  public void writeAsJsonTo(Writer writer) throws IOException {
    writer.write(this.json);
  }

  public void setTotalCount(int totalCount) {
    this.totalCount = totalCount;
  }

  public int getTotalCount() {
    return totalCount;
  }

}
