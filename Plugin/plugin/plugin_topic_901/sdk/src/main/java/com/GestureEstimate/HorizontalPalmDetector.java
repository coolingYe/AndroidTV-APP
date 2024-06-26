/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class HorizontalPalmDetector extends Detector {
  private transient long swigCPtr;

  protected HorizontalPalmDetector(long cPtr, boolean cMemoryOwn) {
    super(GestureEstimateJNI.HorizontalPalmDetector_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(HorizontalPalmDetector obj) {
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
        GestureEstimateJNI.delete_HorizontalPalmDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public boolean On(Point3fList hand) {
    return GestureEstimateJNI.HorizontalPalmDetector_On(swigCPtr, this, Point3fList.getCPtr(hand), hand);
  }

  public void setDist_thres(float value) {
    GestureEstimateJNI.HorizontalPalmDetector_dist_thres_set(swigCPtr, this, value);
  }

  public float getDist_thres() {
    return GestureEstimateJNI.HorizontalPalmDetector_dist_thres_get(swigCPtr, this);
  }

  public HorizontalPalmDetector() {
    this(GestureEstimateJNI.new_HorizontalPalmDetector(), true);
  }

}
