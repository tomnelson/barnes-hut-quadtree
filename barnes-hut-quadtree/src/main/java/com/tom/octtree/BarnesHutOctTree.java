package com.tom.octtree;

import com.google.common.base.Preconditions;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A OctTree that can gather combined forces from visited nodes. Inspired by
 * http://arborjs.org/docs/barnes-hut
 * http://www.cs.princeton.edu/courses/archive/fall03/cs126/assignments/barnes-hut.html
 * https://github.com/chindesaurus/BarnesHut-N-Body
 *
 * @author Tom Nelson
 */
public class BarnesHutOctTree<T> {

  private static final Logger log = LoggerFactory.getLogger(BarnesHutOctTree.class);

  private Node<T> root;

  public static class Builder<T> {
    protected double theta = Node.DEFAULT_THETA;
    protected Box bounds;

    public BarnesHutOctTree.Builder bounds(Box bounds) {
      this.bounds = bounds;
      return this;
    }

    public BarnesHutOctTree.Builder bounds(
        double x, double y, double z, double width, double height, double depth) {
      bounds(new Box(x, y, z, width, height, depth));
      return this;
    }

    public BarnesHutOctTree.Builder bounds(double width, double height, double depth) {
      bounds(new Box(0, 0, 0, width, height, depth));
      return this;
    }

    public BarnesHutOctTree.Builder theta(double theta) {
      this.theta = theta;
      return this;
    }

    public BarnesHutOctTree<T> build() {
      return new BarnesHutOctTree(this);
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public Box getBounds() {
    return root.getBounds();
  }

  public Node<T> getRoot() {
    return root;
  }

  private Object lock = new Object();

  private BarnesHutOctTree(Builder<T> builder) {
    this.root = Node.<T>builder().withVolume(builder.bounds).withTheta(builder.theta).build();
  }

  /*
   * Clears the quadtree
   */
  public void clear() {
    root.clear();
  }

  /**
   * visit nodes in the quad tree and accumulate the forces to apply to the element for the passed
   * node
   *
   * @param node // * @param userData
   */
  public void visit(ForceObject<T> node) {
    if (root != null && root.forceObject != node) {
      root.visit(node);
    }
  }

  /*
   * Insert the object into the quadtree. If the node exceeds the capacity, it
   * will split and add all objects to their corresponding nodes.
   */
  protected void insert(ForceObject<T> node) {
    synchronized (lock) {
      root.insert(node);
      log.trace("after inserting {}, now the tree is {}", node, this);
    }
  }

  public void rebuild(Map<T, Point> locations) {
    clear();
    synchronized (lock) {
      for (Map.Entry<T, Point> entry : locations.entrySet()) {
        ForceObject<T> forceObject = new ForceObject<T>(entry.getKey(), entry.getValue());
        insert(forceObject);
      }
    }
  }

  public void applyForcesTo(ForceObject<T> visitor) {
    Preconditions.checkArgument(visitor != null, "Cannot apply forces to a null ForceObject");
    if (root != null && root.forceObject != visitor) {
      root.applyForcesTo(visitor);
    }
  }

  @Override
  public String toString() {
    return "Tree:" + root;
  }
}
