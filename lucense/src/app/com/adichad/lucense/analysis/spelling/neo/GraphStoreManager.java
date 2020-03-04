package com.adichad.lucense.analysis.spelling.neo;

import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.search.Query;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.RelationshipIndex;
import org.neo4j.kernel.EmbeddedGraphDatabase;

class GraphStoreManager {
  private GraphDatabaseService graphDb;
  private Index<Node>          termIndex;
  private RelationshipIndex    proxIndex;
  private final int            maxEdgeLen;

  public GraphStoreManager(String graphPath, int maxEdgeLen) {
    this.graphDb = new EmbeddedGraphDatabase(graphPath);
    IndexManager index = this.graphDb.index();
    this.termIndex = index.forNodes("terms");
    this.proxIndex = index.forRelationships("proximities");
    this.maxEdgeLen = maxEdgeLen;
  }

  public final Node ensureExistsNode(String label) {
    IndexHits<Node> h = this.termIndex.get("label", label);
    Node c = h.getSingle();
    h.close();
    if (c == null) {
      c = graphDb.createNode();
      c.setProperty("label", label);
      this.termIndex.add(c, "label", label);
    }
    c.setProperty("freq", (Integer) c.getProperty("freq", 0) + 1);
    Node ref = this.graphDb.getReferenceNode();
    ref.setProperty("totalfreq", (Integer) ref.getProperty("totalfreq", 0) + 1);

    return c;
  }

  public final Relationship ensureExistsRelationship(Node node1, Node node2,
      int edgeLen) {
    RelationshipType relType = DynamicRelationshipType.withName("COOCCUR"
        );
    IndexHits<Relationship> h = proxIndex.query("type", relType.name(), node1,
        node2);
    Relationship r = h.getSingle();
    h.close();
    if (r == null) {
      r = node1.createRelationshipTo(node2, relType);
      this.proxIndex.add(r, "type", relType.name());
    }
    r.setProperty("freq", (Integer) r.getProperty("freq", 0) + 1);

    String outFreqLabel = "outfreq";
    node1.setProperty(outFreqLabel,
        (Integer) node1.getProperty(outFreqLabel, 0) + 1);

    String inFreqLabel = "infreq";
    node2.setProperty(inFreqLabel,
        (Integer) node2.getProperty(inFreqLabel, 0) + 1);

    return r;
  }

  public Transaction beginTx() {
    return this.graphDb.beginTx();
  }

  public void purge() {
    Iterable<Node> nodes = this.graphDb.getAllNodes();
    for (Node n : nodes) {
      Iterable<Relationship> rels = n.getRelationships();
      for (Relationship r : rels) {
        r.delete();
      }
      n.delete();
    }

    this.termIndex.delete();
    this.proxIndex.delete();
  }

  public void normalize()  {
    float totalFreq = ((Integer) this.graphDb.getReferenceNode().getProperty(
        "totalfreq", 0)).floatValue();

    Iterable<Node> nodes = this.graphDb.getAllNodes();
    for (Node n : nodes) {
      float nodeFreq = ((Integer) n.getProperty("freq", 0)).floatValue();
      n.setProperty("sup", nodeFreq / totalFreq);

      for (int i = 1; i <= maxEdgeLen; i++) {
        RelationshipType relType = DynamicRelationshipType.withName("COOCCUR"
            );

        float outFreq = ((Integer) n.getProperty("outfreq", 0))
            .floatValue();
        Iterable<Relationship> rels = n.getRelationships(Direction.OUTGOING,
            relType);
        for (Relationship r : rels) {
          float relFreq = ((Integer) r.getProperty("freq", 0)).floatValue();
          r.setProperty("outsup", relFreq / outFreq);
          // how much does this edge contribute to the outfreq of its source
          // node?
          // aka: P(dest|source)
        }

        float inFreq = ((Integer) n.getProperty("infreq", 0)).floatValue();
        rels = n.getRelationships(Direction.INCOMING, relType);
        for (Relationship r : rels) {
          float relFreq = ((Integer) r.getProperty("freq", 0)).floatValue();
          r.setProperty("insup", relFreq / inFreq);
          // how much does this edge contribute to the infreq of its destination
          // node?
          // aka: P(source|dest)
        }

      }

    }

  }

  private float getForwardEdgeProbability(Node node1, Node node2,
      RelationshipType relType) {

    IndexHits<Relationship> h = proxIndex.query("type", relType.name(), node1,
        node2);

    Relationship r = null;
    r = h.getSingle();
    h.close();
    if (r == null) {
      return 1.0f;
    }
    return ((Float) r.getProperty("outsup", 1.0f)).floatValue();
  }

  private float getBackwardEdgeProbability(Node node1, Node node2,
      RelationshipType relType) {
    IndexHits<Relationship> h = proxIndex.query("type", relType.name(), node1,
        node2);
    Relationship r = h.getSingle();
    h.close();
    if (r == null) {
      return 1.0f;
    }
    return ((Float) r.getProperty("insup", 1.0f)).floatValue();
  }

  public void close() {
    this.graphDb.shutdown();
  }

  public void filterCorrections(final String label,
      Map<Correction, Correction> corrs, final String refl,
      final Map<Correction, Correction> corrs2, final Direction d) {
    
    IndexHits<Node> h = this.termIndex.get("label", refl);
    Node ref = h.getSingle();
    h.close();
    if (ref == null) {
      return;
    }

    RelationshipType relType = DynamicRelationshipType.withName("COOCCUR"
        );
    
    for (Correction corr : corrs.values()) {
      
      IndexHits<Node> hits = this.termIndex.get("label", corr.label);
      Node node = hits.getSingle();
      hits.close();
      float rsup = (d.equals(Direction.OUTGOING)) ? this
          .getForwardEdgeProbability(node, ref, relType) : this
          .getBackwardEdgeProbability(ref, node, relType);
      corr.score *= rsup;
    }

  }

  public void queryFilter(Query query, Map<Correction, Correction> corrs) {
    IndexHits<Node> hits = termIndex.query(query);
    for (Node node : hits) {
      Correction corr = new Correction();
      corr.label = (String) node.getProperty("label");
      corr.score = (Float) node.getProperty("sup");
      corrs.put(corr, corr);
      //System.out.println("from fuzzy query: "+corr);
    }

  }
}
