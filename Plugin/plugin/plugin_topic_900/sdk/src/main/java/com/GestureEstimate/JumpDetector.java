/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class JumpDetector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected JumpDetector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(JumpDetector obj) {
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
        GestureEstimateJNI.delete_JumpDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public JumpDetector() {
    this(GestureEstimateJNI.new_JumpDetector(), true);
  }

  public boolean IsJump(Point3fList pose) {
    return GestureEstimateJNI.JumpDetector_IsJump(swigCPtr, this, Point3fList.getCPtr(pose), pose);
  }

  public void SetJumpThres(float up_shoulder_thres, float up_hip_thres, float up_ankle_thres, int max_frame_count) {
    GestureEstimateJNI.JumpDetector_SetJumpThres(swigCPtr, this, up_shoulder_thres, up_hip_thres, up_ankle_thres, max_frame_count);
  }

}
