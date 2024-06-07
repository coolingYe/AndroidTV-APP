/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class RunWalkAndStandDetector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected RunWalkAndStandDetector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(RunWalkAndStandDetector obj) {
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
        GestureEstimateJNI.delete_RunWalkAndStandDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public RunWalkAndStandDetector() {
    this(GestureEstimateJNI.new_RunWalkAndStandDetector(), true);
  }

  public ActionPoseV2Enum On(Point3fList pose) {
    return ActionPoseV2Enum.swigToEnum(GestureEstimateJNI.RunWalkAndStandDetector_On(swigCPtr, this, Point3fList.getCPtr(pose), pose));
  }

  public void SetRunWalkAndStandThres(float foot_chg_thres, float run_wrists_height_thres, int max_frame_count, int chg_status_frame_count) {
    GestureEstimateJNI.RunWalkAndStandDetector_SetRunWalkAndStandThres(swigCPtr, this, foot_chg_thres, run_wrists_height_thres, max_frame_count, chg_status_frame_count);
  }

}
