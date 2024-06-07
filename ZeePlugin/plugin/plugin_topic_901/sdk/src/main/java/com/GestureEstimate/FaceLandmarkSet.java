/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class FaceLandmarkSet {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected FaceLandmarkSet(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(FaceLandmarkSet obj) {
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
        GestureEstimateJNI.delete_FaceLandmarkSet(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setFace(Point3fList value) {
    GestureEstimateJNI.FaceLandmarkSet_face_set(swigCPtr, this, Point3fList.getCPtr(value), value);
  }

  public Point3fList getFace() {
    long cPtr = GestureEstimateJNI.FaceLandmarkSet_face_get(swigCPtr, this);
    return (cPtr == 0) ? null : new Point3fList(cPtr, false);
  }

  public FaceLandmarkSet() {
    this(GestureEstimateJNI.new_FaceLandmarkSet(), true);
  }

}
