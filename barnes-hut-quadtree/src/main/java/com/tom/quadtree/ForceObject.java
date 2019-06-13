package com.tom.quadtree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance used to gather forces while visiting the BarnesHut QuadTree.
 *
 * @author Tom Nelson
 */
public class ForceObject<T> {

  private static final Logger log = LoggerFactory.getLogger(ForceObject.class);

  public static class Builder<T> {
    protected double mass = 1;
    protected Point p = Point.ORIGIN;
    protected Point f = Point.ORIGIN;
    protected T element;

    public Builder<T> element(T element) {
      this.element = element;
      return this;
    }

    public Builder<T> point(Point p) {
      this.p = p;
      return this;
    }

    public Builder<T> force(Point f) {
      this.f = f;
      return this;
    }

    public Builder<T> mass(double mass) {
      this.mass = mass;
      return this;
    }


    public ForceObject<T> build() {
      return new ForceObject(this);
    }
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }


  /** location of p */
  public final Point p;

  /** force vector */
  public Point f;

  /** mass */
  protected double mass;

  private final T element;

  ForceObject(Builder<T> builder) {
    this(builder.element, builder.p, builder.mass);
  }

  private ForceObject(T element, Point p, double mass) {
    this.element = element;
    this.p = p;
    this.f = Point.ORIGIN;
    this.mass = mass;
  }

  private ForceObject(T element, Point p) {
    this(element, p, 1);
  }

  private ForceObject(T element, double x, double y) {
    this(element, Point.of(x, y), 1);
  }

  private ForceObject(T element, double x, double y, double mass) {
    this.element = element;
    p = Point.of(x, y);
    this.mass = mass;
  }

  /**
   * override in the layoutAlgorithm to apply forces in a way that is consistent with the chosen
   * implementation. See FRBHVisitorLayoutAlgorithm and SpringVisitorLayoutAlgorithm.
   *
   * @param other the ForceObject (a node or a force vector) to apply force from
   */
  protected <S> void addForceFrom(ForceObject<T> other) {
    // no op
  }

  public ForceObject add(ForceObject<T> other) {
    double totalMass = this.mass + other.mass;
    Point p =
        Point.of(
            (this.p.x * this.mass + other.p.x * other.mass) / totalMass,
            (this.p.y * this.mass + other.p.y * other.mass) / totalMass);

    return new ForceObject<>("force", p, totalMass);
  }

  public T getElement() {
    return element;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ForceObject<?> that = (ForceObject<?>) o;

    if (Double.compare(that.mass, mass) != 0) return false;
    if (p != null ? !p.equals(that.p) : that.p != null) return false;
    return element != null ? element.equals(that.element) : that.element == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = p != null ? p.hashCode() : 0;
    temp = Double.doubleToLongBits(mass);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (element != null ? element.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ForceObject{"
        + "element="
        + element
        + ", p="
        + p
        + ", mass="
        + mass
        + ", force="
        + f
        + '}';
  }
}
