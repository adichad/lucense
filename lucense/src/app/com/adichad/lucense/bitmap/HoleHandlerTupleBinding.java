package com.adichad.lucense.bitmap;

import java.util.Iterator;
import java.util.TreeSet;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class HoleHandlerTupleBinding extends TupleBinding<HoleHandler> {

  public void objectToEntry(HoleHandler o, TupleOutput out) {
    HoleHandler p = (HoleHandler) o;
    TreeSet<Hole> holes = p.getHoles();
    out.writeInt(holes.size());

    for (Iterator iterator = holes.iterator(); iterator.hasNext();) {
      Hole hole = (Hole) iterator.next();
      out.writeInt(hole.getStart());
      out.writeInt(hole.getEnd());
    }

  }

  public HoleHandler entryToObject(TupleInput in) {
    int count = in.readInt();
    HoleHandler hl = null;
    if (count > 0) {
      hl = new HoleHandler();
      for (int i = 0; i < count; i++) {
        hl.add(in.readInt(), in.readInt());
      }
    }
    return hl;
  }

}
