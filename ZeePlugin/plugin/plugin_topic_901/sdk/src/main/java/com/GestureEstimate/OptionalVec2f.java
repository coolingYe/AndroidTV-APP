/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class OptionalVec2f {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected OptionalVec2f(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(OptionalVec2f obj) {
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
        GestureEstimateJNI.delete_OptionalVec2f(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OptionalVec2f() {
    this(GestureEstimateJNI.new_OptionalVec2f__SWIG_0(), true);
  }

  public OptionalVec2f(Vec2f value) {
    this(GestureEstimateJNI.new_OptionalVec2f__SWIG_1(Vec2f.getCPtr(value), value), true);
  }

  public OptionalVec2f(OptionalVec2f other) {
    this(GestureEstimateJNI.new_OptionalVec2f__SWIG_2(OptionalVec2f.getCPtr(other), other), true);
  }

  public OptionalVec2f assign(Vec2f value) {
    return new OptionalVec2f(GestureEstimateJNI.OptionalVec2f_assign__SWIG_0(swigCPtr, this, Vec2f.getCPtr(value), value), false);
  }

  public OptionalVec2f assign(OptionalVec2f other) {
    return new OptionalVec2f(GestureEstimateJNI.OptionalVec2f_assign__SWIG_1(swigCPtr, this, OptionalVec2f.getCPtr(other), other), false);
  }

  public void swap(OptionalVec2f other) {
    GestureEstimateJNI.OptionalVec2f_swap(swigCPtr, this, OptionalVec2f.getCPtr(other), other);
  }

  public Vec2f value() {
    return new Vec2f(GestureEstimateJNI.OptionalVec2f_value(swigCPtr, this), false);
  }

  public Vec2f value_or(Vec2f deflt) {
    return new Vec2f(GestureEstimateJNI.OptionalVec2f_value_or(swigCPtr, this, Vec2f.getCPtr(deflt), deflt), false);
  }

  public boolean is_specified() {
    return GestureEstimateJNI.OptionalVec2f_is_specified(swigCPtr, this);
  }

  public void clear() {
    GestureEstimateJNI.OptionalVec2f_clear(swigCPtr, this);
  }

}
