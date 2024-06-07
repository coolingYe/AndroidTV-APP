/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class HandsUpAndFistDetector extends Detector {
  private transient long swigCPtr;

  protected HandsUpAndFistDetector(long cPtr, boolean cMemoryOwn) {
    super(GestureEstimateJNI.HandsUpAndFistDetector_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(HandsUpAndFistDetector obj) {
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
        GestureEstimateJNI.delete_HandsUpAndFistDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public boolean OnHandsUpAndFist(Point3fList body, Point3fList left, Point3fList right) {
    return GestureEstimateJNI.HandsUpAndFistDetector_OnHandsUpAndFist(swigCPtr, this, Point3fList.getCPtr(body), body, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public HandsUpAndFistDetector() {
    this(GestureEstimateJNI.new_HandsUpAndFistDetector(), true);
  }

}