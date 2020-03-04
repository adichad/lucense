/**
 * 
 */
package com.adichad.lucense.analysis.spelling;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class TermSequenceGraph {

  protected static class TermSequenceNode {

    private Long label;

    private HashMap<TermSequenceNode, Long> outgoingEdges;

    private HashMap<TermSequenceNode, Long> incomingEdges;

    private long totalOutgoingWeight;

    private long totalIncomingWeight;

    public TermSequenceNode(Long label) {
      this.label = label;
      this.outgoingEdges = new HashMap<TermSequenceNode, Long>();
      this.incomingEdges = new HashMap<TermSequenceNode, Long>();
    }

    public void writeTo(DataOutputStream out) throws IOException {
      out.writeLong(this.label);
      out.writeLong(this.totalOutgoingWeight);
      out.writeLong(this.totalIncomingWeight);
      out.writeInt(this.outgoingEdges.size());
      for (TermSequenceNode node : this.outgoingEdges.keySet()) {
        out.writeLong(node.label);
        out.writeLong(this.outgoingEdges.get(node));
      }
      out.writeInt(this.incomingEdges.size());
      for (TermSequenceNode node : this.incomingEdges.keySet()) {
        out.writeLong(node.label);
        out.writeLong(this.incomingEdges.get(node));
      }
    }

    public static TermSequenceNode readFrom(DataInputStream in) throws IOException {
      long label = in.readLong();
      TermSequenceNode node = new TermSequenceNode(label);

      return node;
    }

    @Override
    public int hashCode() {
      return this.label.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      return this.label.equals(((TermSequenceNode) o).label);
    }

    @Override
    public String toString() {
      return this.label.toString();
    }

    public void addOutgoingEdge(Long label) {
      addOutgoingEdge(new TermSequenceNode(label));
    }

    private void addOutgoingEdge(TermSequenceNode termSequenceNode) {
      if (!this.outgoingEdges.containsKey(termSequenceNode)) {
        this.outgoingEdges.put(termSequenceNode, 1l);
      } else {
        this.outgoingEdges.put(termSequenceNode, this.outgoingEdges.get(termSequenceNode) + 1);
      }
      this.totalOutgoingWeight++;
    }

    public void addIncomingEdge(Long label) {
      addIncomingEdge(new TermSequenceNode(label));
    }

    private void addIncomingEdge(TermSequenceNode termSequenceNode) {
      if (!this.incomingEdges.containsKey(termSequenceNode)) {
        this.incomingEdges.put(termSequenceNode, 1l);
      } else {
        this.incomingEdges.put(termSequenceNode, this.incomingEdges.get(termSequenceNode) + 1);
      }
      this.totalIncomingWeight++;
    }

    private Long getOutgoingEdgeWeight(Long label) {
      return getOutgoingEdgeWeight(new TermSequenceNode(label));
    }

    private Long getOutgoingEdgeWeight(TermSequenceNode termSequenceNode) {
      if (!this.outgoingEdges.containsKey(termSequenceNode)) {
        return 0l;
      }
      return this.outgoingEdges.get(termSequenceNode);
    }

    private Long getIncomingEdgeWeight(Long label) {
      return getIncomingEdgeWeight(new TermSequenceNode(label));
    }

    private Long getIncomingEdgeWeight(TermSequenceNode termSequenceNode) {
      if (!this.incomingEdges.containsKey(termSequenceNode)) {
        return 0l;
      }
      return this.incomingEdges.get(termSequenceNode);
    }

    private Long getTotalOutgoingWeight() {
      return this.totalOutgoingWeight;
    }

    private Long getTotalIncomingWeight() {
      return this.totalIncomingWeight;
    }

  }

  HashMap<Long, TermSequenceNode> termSequenceNodes;

  TermSequenceNode prev;

  TermDictionary termDictionary;

  public TermSequenceGraph() {
    this.termSequenceNodes = new HashMap<Long, TermSequenceNode>();
    this.termDictionary = new TermDictionary();
    reset();
  }

  public boolean add(String label) {
    boolean added = false;
    if (this.termDictionary.addTerm(label)) {
      TermSequenceNode termSequenceNode = new TermSequenceNode(this.termDictionary.getID(label));
      this.termSequenceNodes.put(this.termDictionary.getID(label), termSequenceNode);
      added = true;
    }
    if (this.prev != null) {
      this.prev.addOutgoingEdge(this.termSequenceNodes.get(this.termDictionary.getID(label)));
      this.termSequenceNodes.get(this.termDictionary.getID(label)).addIncomingEdge(this.prev);
    }
    this.prev = this.termSequenceNodes.get(this.termDictionary.getID(label));
    return added;
  }

  public String getMostFreqNextWord(String label) {
    TermSequenceNode mostFreq = null;
    HashMap<TermSequenceNode, Long> edges = this.termSequenceNodes.get(this.termDictionary.getID(label)).outgoingEdges;
    for (TermSequenceNode next : edges.keySet()) {
      if ((mostFreq == null) || (edges.get(mostFreq) < edges.get(next))) {
        mostFreq = next;
      }

    }
    return null;
  }

  public Double getProbabilityGivenPrevious(String label, String prev) {
    Double p = this.termDictionary.getProbability(label);
    TermSequenceNode prevNode = this.termSequenceNodes.get(this.termDictionary.getID(prev));
    if (prevNode != null) {
      Long nextLabel = this.termDictionary.getID(label);
      p = prevNode.getOutgoingEdgeWeight(nextLabel).doubleValue() / prevNode.getTotalOutgoingWeight().doubleValue();
    }
    return p;
  }

  public Double getProbabilityGivenNext(String label, String next) {
    Double p = this.termDictionary.getProbability(label);
    TermSequenceNode nextNode = this.termSequenceNodes.get(this.termDictionary.getID(next));
    if (nextNode != null) {
      Long prevLabel = this.termDictionary.getID(label);
      p = nextNode.getIncomingEdgeWeight(prevLabel).doubleValue() / nextNode.getTotalIncomingWeight().doubleValue();
    }
    return p;
  }

  public void reset() {
    this.prev = null;
  }

  public TermDictionary getDictionary() {
    return this.termDictionary;
  }

  @Override
  public String toString() {
    Stack<TermSequenceNode> list = new Stack<TermSequenceNode>();
    HashSet<TermSequenceNode> visited = new HashSet<TermSequenceNode>();
    list.addAll(this.termSequenceNodes.values());
    StringBuilder s = new StringBuilder("");
    TermSequenceNode termSequenceNode;
    while (!list.isEmpty()) {
      termSequenceNode = list.pop();
      if (!visited.contains(termSequenceNode)) {
        s.append(termSequenceNode).append("(").append(termSequenceNode.totalOutgoingWeight).append(",")
            .append(termSequenceNode.totalIncomingWeight).append(")");
        visited.add(termSequenceNode);
        s.append("[next: ");
        for (TermSequenceNode next : termSequenceNode.outgoingEdges.keySet()) {
          s.append(" ").append(next).append(",").append(termSequenceNode.outgoingEdges.get(next));
          list.add(next);
        }
        s.append("] [prev: ");
        for (TermSequenceNode prev : termSequenceNode.incomingEdges.keySet()) {
          s.append(" ").append(prev).append(",").append(termSequenceNode.incomingEdges.get(prev));
        }
        s.append("]\n");
      }
    }
    return s.toString();
  }

  public void writeTo(OutputStream out) throws IOException {
    Stack<TermSequenceNode> list = new Stack<TermSequenceNode>();
    HashSet<TermSequenceNode> visited = new HashSet<TermSequenceNode>();
    list.addAll(this.termSequenceNodes.values());

    DataOutputStream b = new DataOutputStream(out);
    TermSequenceNode termSequenceNode;
    this.termDictionary.writeTo(b);
    b.writeInt(list.size());
    while (!list.isEmpty()) {
      termSequenceNode = list.pop();
      if (!visited.contains(termSequenceNode)) {
        b.writeLong(termSequenceNode.label);
        b.writeLong(termSequenceNode.totalOutgoingWeight);
        b.writeLong(termSequenceNode.totalIncomingWeight);
        visited.add(termSequenceNode);
        b.writeInt(termSequenceNode.outgoingEdges.size());
        for (TermSequenceNode next : termSequenceNode.outgoingEdges.keySet()) {
          b.writeLong(next.label);
          b.writeLong(termSequenceNode.outgoingEdges.get(next));
          list.add(next);
        }
      }
    }
  }

  public static TermSequenceGraph readFrom(InputStream in) throws IOException {
    DataInputStream din = new DataInputStream(in);

    TermSequenceGraph g = new TermSequenceGraph();
    g.termDictionary = TermDictionary.readFrom(din);
    HashMap<Long, TermSequenceNode> visited = new HashMap<Long, TermSequenceNode>();

    int len = din.readInt();
    for (int i = 0; i < len; i++) {
      long label = din.readLong();

      if (!g.termSequenceNodes.containsKey(label)) {
        TermSequenceNode node;
        if (visited.containsKey(label)) {
          node = visited.get(label);
        } else {
          node = new TermSequenceNode(label);
        }
        node.totalOutgoingWeight = din.readLong();
        node.totalIncomingWeight = din.readLong();
        int size = din.readInt();
        for (int j = 0; j < size; j++) {
          long nlabel = din.readLong();
          long value = din.readLong();
          TermSequenceNode oNode;
          if (visited.containsKey(nlabel)) {
            oNode = visited.get(nlabel);
          } else {
            oNode = new TermSequenceNode(nlabel);
            visited.put(nlabel, oNode);
          }
          node.outgoingEdges.put(oNode, value);
          oNode.incomingEdges.put(node, value);
        }
        visited.put(label, node);
        g.termSequenceNodes.put(label, node);
      }
    }
    return g;
  }
}
