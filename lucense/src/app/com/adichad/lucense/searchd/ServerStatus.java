/*******************************************************************************
 * ServerStatus.java Accessor for ManagedProcessorPool serverStatus *
 ***************************************************************************/

package com.adichad.lucense.searchd;

//import java.util.Formatter;

public class ServerStatus {
  private long upTime;

  private long totalSearchRequests;

  private int currentConcurrantSearchRequests;

  private String[] loadedIndexes;

  private String[] loadedAuxIndexes;

  public void setUpTime(long upTime) {
    this.upTime = upTime;
  }

  public void setTotalSearchRequests(long totalSearchRequests2) {
    this.totalSearchRequests = totalSearchRequests2;
  }

  public void setCurrentConcurrantSearchRequests(int currentConcurrantSearchRequests) {
    this.currentConcurrantSearchRequests = currentConcurrantSearchRequests;
  }

  public void setLoadedIndexes(String[] loadedIndexes) {
    this.loadedIndexes = loadedIndexes;
  }
  
  public void setLoadedAuxIndexes(String[] loadedAuxIndexes) {
    this.loadedAuxIndexes = loadedAuxIndexes;
  }

  @Override
  public String toString() {
    StringBuilder buff = new StringBuilder();
    // Formatter formatter = new Formatter(buff);
    buff.append("________ search server status ________\n");
    buff.append("             up time : ");
    long time = this.upTime;
    // long millisecs = time % 1000;
    time /= 1000;
    long secs = time % 60;
    time /= 60;
    long mins = time % 60;
    time /= 60;
    long hours = time % 24;
    time /= 24;
    long days = time;
    if (days > 0)
      buff.append(days).append(" day(s) ");
    if (hours > 0)
      buff.append(hours).append(" hour(s) ");
    if (mins > 0)
      buff.append(mins).append(" min(s) ");
    if (secs > 0)
      buff.append(secs).append(" sec(s) ");
    buff.append("\n");

    buff.append("      total requests : ").append(this.totalSearchRequests).append("\n");
    buff.append(" current concurrancy : ").append(this.currentConcurrantSearchRequests).append("\n");
    buff.append("      lucene indexes :-\n");
    for (String loadedIndex : this.loadedIndexes)
      buff.append(loadedIndex).append("\n");
    buff.append("______________________________________\n");

    buff.append("   auxillary indexes :-\n");
    for (String loadedIndex : this.loadedAuxIndexes)
      buff.append(loadedIndex).append("\n");
    buff.append("______________________________________\n");
    return buff.toString();
  }

  
}
/*******************************************************************************
 * ServerStatus.java ENDS *
 ***************************************************************************/

