package com.adichad.lucense.bitmap;

import java.io.FileNotFoundException;

import com.sleepycat.db.DatabaseException;

public class BenchMarkThread extends Thread {

  /**
   * @param args
   */
  private int whatToPerform = 0;

  private AuxIndexManager im = null;

  public BenchMarkThread(int whatToPerform, AuxIndexManager im) {
    this.im = im;
    this.whatToPerform = whatToPerform;
  }

  public static void main(String[] args) {

    try {
      String[] keys = { "1" };
      AuxIndexManager im = new AuxIndexManager("/tmp/bdb/data");
      // im.benchMark(false, true, keys) ;im.benchMarkOptimize(false, keys) ;
      // im.benchMarkRead(keys) ;System.exit(0) ;

      BenchMarkThread bt1 = new BenchMarkThread(im.Put, im);
      // bt1.start() ;
      // Thread.sleep(3000) ;
      BenchMarkThread bt6 = new BenchMarkThread(im.Put, im);
      // bt6.start() ;
      BenchMarkThread bt7 = new BenchMarkThread(im.Search, im);
      bt7.start();
      BenchMarkThread bt8 = new BenchMarkThread(im.Search, im);
      // bt8.start() ;

      BenchMarkThread bt2 = new BenchMarkThread(im.Search, im);
      // bt2.start() ;
      Thread.sleep(10);
      BenchMarkThread bt3 = new BenchMarkThread(im.Optimize, im);
      // bt3.start() ;
      // BenchMarkThread bt4 = new BenchMarkThread(im.Search,im) ;
      // bt4.start() ;

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (DatabaseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // TODO Auto-generated method stub

  }

  @Override
  public void run() {
    String keys[] = { "1" };

    if (whatToPerform == AuxIndexManager.Put) {
      for (int i = 0; i < keys.length; i++) {

        int values[] = new int[300000];
        for (int j = 0; j < 300000; j++) {
          values[j] = j;
        }
        System.out.println("putting :" + keys[i]);
        im.add(keys[i], values);
        System.out.println("put Ends :" + keys[i]);

      }

      // im.benchMark(false, true, keys) ;
    } else if (whatToPerform == AuxIndexManager.Optimize) {
      im.benchMarkOptimize(false, keys);// (false, true, keys) ;
    } else {
      im.benchMarkRead(keys);
    }
    // TODO Auto-generated method stub

  }

}
