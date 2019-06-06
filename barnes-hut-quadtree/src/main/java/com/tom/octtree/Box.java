package com.tom.octtree;

/** @author Tom Nelson */
public class Box {

  public final double x;
  public final double y;
  public final double z;
  public final double width;
  public final double height;
  public final double depth;
  public final double maxX;
  public final double maxY;
  public final double maxZ;

  public Box(double x, double y, double z, double width, double height, double depth) {
    //    Preconditions.checkArgument(
    //        width >= 0 && height >= 0 && depth >= 0, "width and height and depth must be non-negative");
    this.x = x;
    this.y = y;
    this.z = z;
    this.width = width;
    this.height = height;
    this.depth = depth;
    this.maxX = x + width;
    this.maxY = y + height;
    this.maxZ = z + depth;
  }

  public double getCenterX() {
    return x + width / 2;
  }

  public double getCenterY() {
    return y + height / 2;
  }

  public double getCenterZ() {
    return y + depth / 2;
  }

  /**
   * fail-fast implementation to reduce computation
   *
   * @param other
   * @return
   */
  public boolean intersects(Box other) {
    if (maxX < other.x
        || other.maxX < x
        || maxY < other.y
        || other.maxY < y
        || maxZ < other.z
        || other.maxZ < z) {
      return false;
    }
    return true;
  }

  public boolean contains(Point p) {
    return contains(p.x, p.y, p.z);
  }

  public boolean contains(double x, double y, double z) {
    if (x < this.x || x > maxX || y < this.y || y > maxY || z < this.z || z > maxZ) {
      return false;
    }
    return true;
  }
}
