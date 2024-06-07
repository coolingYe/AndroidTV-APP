/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class HandsUpDetector extends Detector {
  private transient long swigCPtr;

  protected HandsUpDetector(long cPtr, boolean cMemoryOwn) {
    super(GestureEstimateJNI.HandsUpDetector_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(HandsUpDetector obj) {
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
        GestureEstimateJNI.delete_HandsUpDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public boolean OnLeftHandUp(Point3fList body) {
    return GestureEstimateJNI.HandsUpDetector_OnLeftHandUp(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public boolean OnRightHandUp(Point3fList body) {
    return GestureEstimateJNI.HandsUpDetector_OnRightHandUp(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public boolean OnBothHandUp(Point3fList body) {
    return GestureEstimateJNI.HandsUpDetector_OnBothHandUp(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public HandsUpDetector() {
    this(GestureEstimateJNI.new_HandsUpDetector(), true);
  }

}
