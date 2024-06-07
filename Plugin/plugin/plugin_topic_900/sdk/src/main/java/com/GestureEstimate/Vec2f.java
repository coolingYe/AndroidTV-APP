/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class Vec2f {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected Vec2f(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(Vec2f obj) {
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
        GestureEstimateJNI.delete_Vec2f(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setX(float value) {
    GestureEstimateJNI.Vec2f_x_set(swigCPtr, this, value);
  }

  public float getX() {
    return GestureEstimateJNI.Vec2f_x_get(swigCPtr, this);
  }

  public void setY(float value) {
    GestureEstimateJNI.Vec2f_y_set(swigCPtr, this, value);
  }

  public float getY() {
    return GestureEstimateJNI.Vec2f_y_get(swigCPtr, this);
  }

  public Vec2f() {
    this(GestureEstimateJNI.new_Vec2f(), true);
  }

}