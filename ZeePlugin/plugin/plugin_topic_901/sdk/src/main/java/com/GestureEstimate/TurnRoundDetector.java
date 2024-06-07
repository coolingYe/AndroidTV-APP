/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class TurnRoundDetector {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected TurnRoundDetector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(TurnRoundDetector obj) {
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
        GestureEstimateJNI.delete_TurnRoundDetector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public TurnRoundDetector() {
    this(GestureEstimateJNI.new_TurnRoundDetector(), true);
  }

  public void SetTrunRoundParam(float little_angle, float angle, float large_angle) {
    GestureEstimateJNI.TurnRoundDetector_SetTrunRoundParam(swigCPtr, this, little_angle, angle, large_angle);
  }

  public float GetTurnRoundAngle(Point3fList body) {
    return GestureEstimateJNI.TurnRoundDetector_GetTurnRoundAngle(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public TurnRoundType GetTrunRoundStatus(Point3fList body) {
    return TurnRoundType.swigToEnum(GestureEstimateJNI.TurnRoundDetector_GetTrunRoundStatus(swigCPtr, this, Point3fList.getCPtr(body), body));
  }

}
