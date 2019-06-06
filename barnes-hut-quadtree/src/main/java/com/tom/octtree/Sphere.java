package com.tom.octtree;

/**
 * unused at this time
 *
 * @author Tom Nelson
 */
public class Sphere {

  public final Point center;
  public final double radius;

  public Sphere(Point center, double radius) {
    this.center = center;
    this.radius = radius;
  }

  public boolean contains(Point p) {
    // fast-fail bounds check
    if (!p.inside(
        center.x - radius,
        center.y - radius,
        center.z - radius,
        center.x + radius,
        center.y + radius,
        center.z + radius)) {
      return false;
    }
    return center.distance(p) <= radius;
  }

  public boolean intersects(Box r) {
    // quick fail with bounding box test
    if (r.maxX < center.x - radius) return false;
    if (r.maxY < center.y - radius) return false;
    if (r.x > center.x + radius) return false;
    if (r.y > center.y + radius) return false;
    // more expensive test
    return squaredDistance(center, r) < radius * radius;
  }

  private double squaredDistance(Point p, Box r) {
    double distSq = 0;
    double cx = p.x;
    if (cx < r.x) {
      distSq += (r.x - cx) * (r.x - cx);
    }
    if (cx > r.maxX) {
      distSq += (cx - r.maxX) * (cx - r.maxX);
    }
    double cy = p.y;
    if (cy < r.y) {
      distSq += (r.y - cy) * (r.y - cy);
    }
    if (cy > r.maxY) {
      distSq += (cy - r.maxY) * (cy - r.maxY);
    }
    return distSq;
  }
}
