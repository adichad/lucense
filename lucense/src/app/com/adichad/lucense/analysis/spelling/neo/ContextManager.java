package com.adichad.lucense.analysis.spelling.neo;

import java.util.Map;
import java.util.TreeMap;

import org.neo4j.graphdb.Direction;

class ContextManager {

  private final TermNode[]  terms;
  private int               head = -1, tail = -1;

  private GraphStoreManager graphStoreManager;

  public ContextManager(int max, GraphStoreManager graphWriter) {
    this.graphStoreManager = graphWriter;
    this.terms = new TermNode[max];
  }

  private TermNode append(String label, Map<Correction, Correction> corrs) {
    if ((tail + 1) % terms.length == head) { // full
      tail = head;
      terms[tail].label = label;
      terms[tail].next = null;
      head = (head + 1) % terms.length;
    } else {
      tail = (tail + 1) % terms.length;
      terms[tail] = new TermNode(label, null);
      if (head == -1) // empty
        head = 0;
    }
    terms[tail].corrs = corrs;
    return terms[tail];
  }

  private TermNode suppend(String label, Map<Correction, Correction> corrs) {
    if (head == -1)
      throw new IllegalStateException("Cannot suppend to an empty context");

    TermNode n = new TermNode(label, null);
    n.next = terms[tail];
    n.corrs = corrs;
    terms[tail] = n;
    return terms[tail];
  }

  private void appendAndPersist(String label) {
    this.append(label, null).node = graphStoreManager.ensureExistsNode(label);
    for (int curr = head; curr != tail; curr = (curr + 1) % terms.length) {
      int edgeLen = (curr < tail) ? (tail - curr)
          : (tail + terms.length - curr);
      TermNode n = terms[curr];
      do {
        graphStoreManager.ensureExistsRelationship(n.node, terms[tail].node,
            edgeLen);
      } while ((n = n.next()) != null);
    }
  }

  private void suppendAndPersist(String label) {
    this.suppend(label, null).node = graphStoreManager.ensureExistsNode(label);
    for (int curr = head; curr != tail; curr = (curr + 1) % terms.length) {
      int edgeLen = (curr < tail) ? (tail - curr)
          : (tail + terms.length - curr);
      TermNode n = terms[curr];
      do {
        graphStoreManager.ensureExistsRelationship(n.node, terms[tail].node,
            edgeLen);
      } while ((n = n.next()) != null);
    }
  }

  public void addAndPersist(String label, int posIncr) {
    if (posIncr < 0)
      throw new IllegalArgumentException(
          "Position increment must be non-negative");
    if (posIncr == 0) {
      suppendAndPersist(label);
    } else {
      while (--posIncr > 0)
        appendAndPersist("");

      appendAndPersist(label);
    }
  }

  public void contextFilter(String label, int posIncr,
      Map<Correction, Correction> corrs) {
    if (posIncr < 0)
      throw new IllegalArgumentException(
          "Position increment must be non-negative");
    if (posIncr == 0) {
      suppend(label, corrs);
    } else {
      while (--posIncr > 0)
        append("", null);
      
      append(label, corrs);
    }
    filter();
  }

  private void updateTail(int ref) {
    int curr = tail;
    Direction d = Direction.INCOMING;
    if (terms[curr].corrs != null) {
      for (TermNode temp = terms[ref]; temp != null; temp = temp.next) {
//        if (d.equals(Direction.OUTGOING))
//          System.out.println(terms[curr] + "=o=" + len + "=>" + temp);
//        else
//          System.out.println(temp + "<=" + len + "=i=" + terms[curr]);
        this.graphStoreManager.filterCorrections(terms[curr].label,
            terms[curr].corrs, temp.label, temp.corrs, d);
      }
    }
  }

  private void updateHead(int ref) {
    int curr = head;
    Direction d = Direction.OUTGOING;
    if (terms[curr].corrs != null) {
      for (TermNode temp = terms[ref]; temp != null; temp = temp.next) {
        for(TermNode t2 = terms[curr]; t2 != null; t2 = t2.next) {
//          if (d.equals(Direction.OUTGOING))
//            System.out.println(t2 + "=o=" + len + "=>" + temp);
//          else
//            System.out.println(temp + "<=" + len + "=i=" + t2);
          this.graphStoreManager.filterCorrections(t2.label,
              t2.corrs, temp.label, temp.corrs, d);
        }
      }
    }
  }
  
  private void filter() {
    int curr = head;
    if (curr != tail) {
      updateTail(curr); // tail basis head
      for (curr = (curr + 1) % terms.length; curr != tail; curr = (curr + 1)
          % terms.length) {
        updateHead(curr); // head basis curr
        updateTail(curr); // tail basis curr
      }
    }

    if (curr != head) {
      updateHead(curr); // head basis tail
    }
  }
  
  public boolean advanceNoInput() {
    if(head==tail || head==-1) {
      head = -1; 
      tail = -1;
      return false;
    }
    head = (head+1)%terms.length;
    int curr = head;
    
    
    if (curr != tail) {
      for (curr = (curr + 1) % terms.length; curr != tail; curr = (curr + 1)
          % terms.length) {
        updateHead(curr); // head basis curr
      }
    }

    if (curr != head) {
      updateHead(curr); // head basis tail
    }
    return true;
  }

  public void reset() {
    head = -1;
    tail = -1;
  }
}
