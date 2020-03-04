package com.adichad.lucense.bitmap;

public class DataStoreFactory {

  public static DataStore getInstance() {
    // switch
    return new BDBDataStore();
  }

}
