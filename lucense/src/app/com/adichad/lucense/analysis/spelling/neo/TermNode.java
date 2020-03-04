package com.adichad.lucense.analysis.spelling.neo;

import java.util.Map;

import org.neo4j.graphdb.Node;

class TermNode {
  String                    label;
  Node                      node;
  TermNode                  next;
  Map<Correction, Correction> corrs;

  public TermNode(String label, Node node) {
    this.label = label;
    this.node = node;
  }

  @Override
  public String toString() {
    return label;
  }

  TermNode next() {
    return next;
  }

  boolean hasNext() {
    return next != null;
  }

}
