/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class Point2f {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Point2f(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Point2f obj) {
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
        GestureEstimateJNI.delete_Point2f(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point2f() {
    this(GestureEstimateJNI.new_Point2f__SWIG_0(), true);
  }

  public Point2f(float _x, float _y) {
    this(GestureEstimateJNI.new_Point2f__SWIG_1(_x, _y), true);
  }

  public float dot(Point2f pt) {
    return GestureEstimateJNI.Point2f_dot(swigCPtr, this, Point2f.getCPtr(pt), pt);
  }

  public double ddot(Point2f pt) {
    return GestureEstimateJNI.Point2f_ddot(swigCPtr, this, Point2f.getCPtr(pt), pt);
  }

  public double cross(Point2f pt) {
    return GestureEstimateJNI.Point2f_cross(swigCPtr, this, Point2f.getCPtr(pt), pt);
  }

  public void setX(float value) {
    GestureEstimateJNI.Point2f_x_set(swigCPtr, this, value);
  }

  public float getX() {
    return GestureEstimateJNI.Point2f_x_get(swigCPtr, this);
  }

  public void setY(float value) {
    GestureEstimateJNI.Point2f_y_set(swigCPtr, this, value);
  }

  public float getY() {
    return GestureEstimateJNI.Point2f_y_get(swigCPtr, this);
  }

}