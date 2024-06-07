/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class Point3f {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Point3f(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Point3f obj) {
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
        GestureEstimateJNI.delete_Point3f(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public Point3f() {
    this(GestureEstimateJNI.new_Point3f__SWIG_0(), true);
  }

  public Point3f(float _x, float _y, float _z) {
    this(GestureEstimateJNI.new_Point3f__SWIG_1(_x, _y, _z), true);
  }

  public float dot(Point3f pt) {
    return GestureEstimateJNI.Point3f_dot(swigCPtr, this, Point3f.getCPtr(pt), pt);
  }

  public double ddot(Point3f pt) {
    return GestureEstimateJNI.Point3f_ddot(swigCPtr, this, Point3f.getCPtr(pt), pt);
  }

  public Point3f cross(Point3f pt) {
    return new Point3f(GestureEstimateJNI.Point3f_cross(swigCPtr, this, Point3f.getCPtr(pt), pt), true);
  }

  public void setX(float value) {
    GestureEstimateJNI.Point3f_x_set(swigCPtr, this, value);
  }

  public float getX() {
    return GestureEstimateJNI.Point3f_x_get(swigCPtr, this);
  }

  public void setY(float value) {
    GestureEstimateJNI.Point3f_y_set(swigCPtr, this, value);
  }

  public float getY() {
    return GestureEstimateJNI.Point3f_y_get(swigCPtr, this);
  }

  public void setZ(float value) {
    GestureEstimateJNI.Point3f_z_set(swigCPtr, this, value);
  }

  public float getZ() {
    return GestureEstimateJNI.Point3f_z_get(swigCPtr, this);
  }

}