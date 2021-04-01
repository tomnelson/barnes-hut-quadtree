package com.tom.octtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Node in the BarnesHutOctTree. Has a box dimension and a ForceObject that may be either a graph
 * node or a representation of the combined forces of the child nodes. May have 8 child nodes.
 *
 * @author Tom Nelson
 */
public class Node<T> {

  private static final Logger log = LoggerFactory.getLogger(Node.class);
  /**
   * threshold value for determining whether to use the forces in an inner node as a summary value,
   * or to descend the quad tree to another inner node or leaf node. theta should be between 0 and
   * 1, with 0.5 as a commonly selected value. Note that a theta value of 0 will result in no
   * optimization in the BarnesHutOctTree and all force visitors will descend to a leaf node instead
   * of using an inner node summary force vector.
   */
  public static final double DEFAULT_THETA = 0.5;

  // a node contains a ForceObject and possibly 8 Nodes
  protected ForceObject<T> forceObject;

  Node BNW;
  Node BNE;
  Node BSE;
  Node BSW;
  Node FNW;
  Node FNE;
  Node FSE;
  Node FSW;

  protected double theta = DEFAULT_THETA;

  private Box volume;

  public static class Builder<T> {
    protected double theta = DEFAULT_THETA;
    protected Box volume;

    public Node.Builder<T> withVolume(
        double x, double y, double z, double width, double height, double depth) {
      return withVolume(new Box(x, y, z, width, height, depth));
    }

    public Node.Builder<T> withVolume(Box volume) {
      this.volume = volume;
      return this;
    }

    public Node.Builder<T> withTheta(double theta) {
      this.theta = theta;
      return this;
    }

    public Node<T> build() {
      return new Node(this);
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  private Node(Node.Builder<T> builder) {
    this(builder.volume, builder.theta);
  }

  private Node(double x, double y, double z, double width, double height, double depth) {
    this(new Box(x, y, z, width, height, depth));
  }

  private Node(Box r, double theta) {
    this.volume = r;
    this.theta = theta;
  }

  private Node(Box r) {
    volume = r;
  }

  public ForceObject<T> getForceObject() {
    return forceObject;
  }

  /**
   * if all child Nodes are null, this is a leaf
   *
   * @return true it this is a leaf node
   */
  public boolean isLeaf() {
    return BNW == null
        && BNE == null
        && BSE == null
        && BSW == null
        && FNW == null
        && FNE == null
        && FSE == null
        && FSW == null;
  }

  /**
   * insert a new ForceObject into the tree. This changes the combinedMass and the forceVector for
   * any Node that it is inserted into
   *
   * @param element
   */
  public void insert(ForceObject<T> element) {

    log.debug("insert {} into {}", element, this);

    if (forceObject == null) {
      forceObject = element;
      return;
    }
    if (isLeaf()) {
      // there already is a forceObject, so split
      log.trace("must split {}", this);
      split();
      log.trace("into this {} and re-insert {} and {}", this, this.forceObject, element);
      // put the current resident and the new one into the correct quardrants
      insertForceObject(this.forceObject);
      insertForceObject(element);
      // update the centerOfMass, Mass, and Force on this node
      this.forceObject = this.forceObject.add(element);

    } else {
      if (forceObject == element) {
        log.error("can't insert {} into {}", element, this.forceObject);
      }
      // we're already split, update the forceElement for this new element
      forceObject = forceObject.add(element);
      //and follow down the tree to insert
      insertForceObject(element);
    }
  }

  private void insertForceObject(ForceObject<T> forceObject) {
    if (FNW.volume.contains(forceObject.p)) {
      FNW.insert(forceObject);
    } else if (FNE.volume.contains(forceObject.p)) {
      FNE.insert(forceObject);
    } else if (FSE.volume.contains(forceObject.p)) {
      FSE.insert(forceObject);
    } else if (FSW.volume.contains(forceObject.p)) {
      FSW.insert(forceObject);
    } else if (BNW.volume.contains(forceObject.p)) {
      BNW.insert(forceObject);
    } else if (BNE.volume.contains(forceObject.p)) {
      BNE.insert(forceObject);
    } else if (BSE.volume.contains(forceObject.p)) {
      BSE.insert(forceObject);
    } else if (BSW.volume.contains(forceObject.p)) {
      BSW.insert(forceObject);
    } else {
      log.error("no home for {} in {}", forceObject, this);
    }
  }

  public Box getBounds() {
    return volume;
  }

  public void clear() {
    forceObject = null;
    FNW = FNE = FSW = FSE = BNW = BNE = BSW = BSE = null;
  }

  /*
   * Splits the Octtree into 4 sub-QuadTrees
   */
  protected void split() {
    if (log.isTraceEnabled()) {
      log.info("splitting {}", this);
    }
    double width = volume.width / 2;
    double height = volume.height / 2;
    double depth = volume.depth / 2;
    double x = volume.x;
    double y = volume.y;
    double z = volume.z;
    FNE = new Node(x + width, y, z + depth, width, height, depth);
    FNW = new Node(x, y, z + depth, width, height, depth);
    FSW = new Node(x, y + height, z + depth, width, height, depth);
    FSE = new Node(x + width, y + height, z + depth, width, height, depth);
    BNE = new Node(x + width, y, z, width, height, depth);
    BNW = new Node(x, y, z, width, height, depth);
    BSW = new Node(x, y + height, z, width, height, depth);
    BSE = new Node(x + width, y + height, z, width, height, depth);
    if (log.isTraceEnabled()) {
      log.trace("after split, this node is {}", this);
    }
  }

  public void visit(ForceObject<T> target) {
    if (this.forceObject == null || target.getElement().equals(this.forceObject.getElement())) {
      return;
    }

    if (isLeaf()) {
      if (log.isTraceEnabled()) {
        log.trace(
            "isLeaf, Node {} at {} visiting {} at {}",
            this.forceObject.getElement(),
            this.forceObject.p,
            target.getElement(),
            target.p);
      }
      target.addForceFrom(this.forceObject);
      log.trace("added force from {} so its now {}", this.forceObject, target);
    } else {
      // not a leaf
      //  this node is an internal node
      //  calculate s/d
      double s = this.volume.width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = this.forceObject.p.distance(target.p);
      if (s / d < DEFAULT_THETA) {
        // this node is sufficiently far away
        // just use this node's forces
        if (log.isTraceEnabled()) {
          log.trace(
              "Node {} at {} visiting {} at {}",
              this.forceObject.getElement(),
              this.forceObject.p,
              target.getElement(),
              target.p);
        }
        target.addForceFrom(this.forceObject);
        log.trace("added force from {} so its now {}", this.forceObject, target);

      } else {
        // down the tree we go
        FNW.visit(target);
        FNE.visit(target);
        FSW.visit(target);
        FSE.visit(target);
        BNW.visit(target);
        BNE.visit(target);
        BSW.visit(target);
        BSE.visit(target);
      }
    }
  }

  static String asString(Box r) {
    return "["
        + (int) r.x
        + ","
        + (int) r.y
        + ","
        + (int) r.z
        + ","
        + (int) r.width
        + ","
        + (int) r.height
        + ","
        + (int) r.depth
        + "]";
  }

  static <T> String asString(Node<T> node, String margin) {
    StringBuilder s = new StringBuilder();
    s.append("\n");
    s.append(margin);
    s.append("bounds=");
    s.append(asString(node.getBounds()));
    ForceObject forceObject = node.getForceObject();
    if (forceObject != null) {
      s.append(", forceObject:=");
      s.append(forceObject.toString());
    }
    if (node.FNW != null) s.append(asString(node.FNW, margin + marginIncrement));
    if (node.FNE != null) s.append(asString(node.FNE, margin + marginIncrement));
    if (node.FSW != null) s.append(asString(node.FSW, margin + marginIncrement));
    if (node.FSE != null) s.append(asString(node.FSE, margin + marginIncrement));
    if (node.BNW != null) s.append(asString(node.BNW, margin + marginIncrement));
    if (node.BNE != null) s.append(asString(node.BNE, margin + marginIncrement));
    if (node.BSW != null) s.append(asString(node.BSW, margin + marginIncrement));
    if (node.BSE != null) s.append(asString(node.BSE, margin + marginIncrement));

    return s.toString();
  }

  /**
   * accept a visit from the visitor force object, and add this node's forces to the visitor
   *
   * @param visitor the visitor
   */
  public void applyForcesTo(ForceObject<T> visitor) {
    if (this.forceObject == null || visitor.getElement().equals(this.forceObject.getElement())) {
      return;
    }

    if (isLeaf()) {

      visitor.addForceFrom(this.forceObject);

    } else {
      // not a leaf. this node is an internal node
      //  calculate s/d
      double s = this.volume.width;
      //      distance between the incoming node's position and
      //      the center of mass for this node
      double d = this.forceObject.p.distance(visitor.p);
      if (s / d < theta) {
        // this node is sufficiently far away, just use this node's forces
        visitor.addForceFrom(this.forceObject);

      } else {
        // down the tree we go
        FNW.applyForcesTo(visitor);
        FNE.applyForcesTo(visitor);
        FSW.applyForcesTo(visitor);
        FSE.applyForcesTo(visitor);
        BNW.applyForcesTo(visitor);
        BNE.applyForcesTo(visitor);
        BSW.applyForcesTo(visitor);
        BSE.applyForcesTo(visitor);
      }
    }
  }

  static String marginIncrement = "   ";

  @Override
  public String toString() {
    return asString(this, "");
  }
}
