package com.tom.octtree;

import java.util.Objects;

/** @author Tom Nelson */
public class Point {

  public final double x;
  public final double y;
  public final double z;
  public static final Point ORIGIN = new Point(0, 0, 0);

  public static Point of(double x, double y, double z) {
    return new Point(x, y, z);
  }

  private Point(double x, double y, double z) {
    if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)) {
      this.x = 0;
      this.y = 0;
      this.z = 0;
      return;
    }
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public Point add(Point other) {
    return add(other.x, other.y, other.z);
  }

  public Point add(double dx, double dy, double dz) {
    return new Point(x + dx, y + dy, z + dz);
  }

  public double distanceSquared(Point other) {
    return distanceSquared(other.x, other.y, other.z);
  }

  public double distanceSquared(double ox, double oy, double oz) {
    double dx = x - ox;
    double dy = y - oy;
    double dz = z - oz;
    return dx * dx + dy * dy + dz * dz;
  }

  public boolean inside(Sphere c) {
    //  fast-fail bounds check
    if (!inside(
        c.center.x - c.radius,
        c.center.y - c.radius,
        c.center.z - c.radius,
        c.center.x + c.radius,
        c.center.y + c.radius,
        c.center.z + c.radius)) {
      return false;
    }
    return c.center.distance(this) <= c.radius;
  }

  public boolean inside(Box r) {
    return inside(r.x, r.y, r.z, r.maxX, r.maxY, r.maxZ);
  }

  public boolean inside(
      double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
    if (x < minX || maxX < x || y < minY || maxY < y || z < minZ || maxZ < z) {
      return false;
    }
    return true;
  }

  public double length() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  public double distance(Point other) {
    return Math.sqrt(distanceSquared(other));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Point)) {
      return false;
    }

    Point other = (Point) o;

    return (Double.compare(other.x, x) == 0
        && Double.compare(other.y, y) == 0
        && Double.compare(other.z, z) == 0);
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }

  @Override
  public String toString() {
    return "Point{" + "x=" + x + ", y=" + y + ", z=" + z + '}';
  }
}
