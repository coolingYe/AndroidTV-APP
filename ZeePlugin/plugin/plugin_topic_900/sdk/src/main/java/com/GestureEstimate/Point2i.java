/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class Point2i {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Point2i(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Point2i obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        GestureEstimateJNI.delete_Point2i(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point2i() {
    this(GestureEstimateJNI.new_Point2i__SWIG_0(), true);
  }

  public Point2i(int _x, int _y) {
    this(GestureEstimateJNI.new_Point2i__SWIG_1(_x, _y), true);
  }

  public int dot(Point2i pt) {
    return GestureEstimateJNI.Point2i_dot(swigCPtr, this, Point2i.getCPtr(pt), pt);
  }

  public double ddot(Point2i pt) {
    return GestureEstimateJNI.Point2i_ddot(swigCPtr, this, Point2i.getCPtr(pt), pt);
  }

  public double cross(Point2i pt) {
    return GestureEstimateJNI.Point2i_cross(swigCPtr, this, Point2i.getCPtr(pt), pt);
  }

  public void setX(int value) {
    GestureEstimateJNI.Point2i_x_set(swigCPtr, this, value);
  }

  public int getX() {
    return GestureEstimateJNI.Point2i_x_get(swigCPtr, this);
  }

  public void setY(int value) {
    GestureEstimateJNI.Point2i_y_set(swigCPtr, this, value);
  }

  public int getY() {
    return GestureEstimateJNI.Point2i_y_get(swigCPtr, this);
  }

}
