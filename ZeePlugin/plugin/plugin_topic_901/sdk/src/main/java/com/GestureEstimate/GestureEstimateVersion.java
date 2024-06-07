/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class GestureEstimateVersion {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected GestureEstimateVersion(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(GestureEstimateVersion obj) {
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
        GestureEstimateJNI.delete_GestureEstimateVersion(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public void setMajor(int value) {
    GestureEstimateJNI.GestureEstimateVersion_major_set(swigCPtr, this, value);
  }

  public int getMajor() {
    return GestureEstimateJNI.GestureEstimateVersion_major_get(swigCPtr, this);
  }

  public void setMinor(int value) {
    GestureEstimateJNI.GestureEstimateVersion_minor_set(swigCPtr, this, value);
  }

  public int getMinor() {
    return GestureEstimateJNI.GestureEstimateVersion_minor_get(swigCPtr, this);
  }

  public void setPatch(int value) {
    GestureEstimateJNI.GestureEstimateVersion_patch_set(swigCPtr, this, value);
  }

  public int getPatch() {
    return GestureEstimateJNI.GestureEstimateVersion_patch_get(swigCPtr, this);
  }

  public void setBuild_time(String value) {
    GestureEstimateJNI.GestureEstimateVersion_build_time_set(swigCPtr, this, value);
  }

  public String getBuild_time() {
    return GestureEstimateJNI.GestureEstimateVersion_build_time_get(swigCPtr, this);
  }

  public GestureEstimateVersion() {
    this(GestureEstimateJNI.new_GestureEstimateVersion(), true);
  }

}
