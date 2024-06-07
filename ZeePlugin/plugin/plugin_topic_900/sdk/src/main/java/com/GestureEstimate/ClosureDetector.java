/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class ClosureDetector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected ClosureDetector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(ClosureDetector obj) {
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
        GestureEstimateJNI.delete_ClosureDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public ClosureDetector() {
    this(GestureEstimateJNI.new_ClosureDetector(), true);
  }

  public boolean OnClosure(Point3fList body) {
    return GestureEstimateJNI.ClosureDetector_OnClosure(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

}
