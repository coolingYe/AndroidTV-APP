/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class FaceEulerAngleDetector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected FaceEulerAngleDetector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(FaceEulerAngleDetector obj) {
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
        GestureEstimateJNI.delete_FaceEulerAngleDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public FaceEulerAngleDetector() {
    this(GestureEstimateJNI.new_FaceEulerAngleDetector(), true);
  }

  public OptionalEulerAngle GetFaceEulerAngle(Point3fList face) {
    return new OptionalEulerAngle(GestureEstimateJNI.FaceEulerAngleDetector_GetFaceEulerAngle(swigCPtr, this, Point3fList.getCPtr(face), face), true);
  }

  public ZwnFaceAlignType GetFaceAlignStatus(Point3fList face) {
    return ZwnFaceAlignType.swigToEnum(GestureEstimateJNI.FaceEulerAngleDetector_GetFaceAlignStatus(swigCPtr, this, Point3fList.getCPtr(face), face));
  }

  public void SetFaceAlignThresHold(float pitch_thresh, float yaw_thresh, float roll_thresh) {
    GestureEstimateJNI.FaceEulerAngleDetector_SetFaceAlignThresHold(swigCPtr, this, pitch_thresh, yaw_thresh, roll_thresh);
  }

}
