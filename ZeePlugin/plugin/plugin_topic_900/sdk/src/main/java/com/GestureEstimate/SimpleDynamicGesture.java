/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class SimpleDynamicGesture {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SimpleDynamicGesture(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SimpleDynamicGesture obj) {
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
        GestureEstimateJNI.delete_SimpleDynamicGesture(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public OptionalVec2f OnLeftTwoFingerSliding(Point3fList left) {
    return new OptionalVec2f(GestureEstimateJNI.SimpleDynamicGesture_OnLeftTwoFingerSliding(swigCPtr, this, Point3fList.getCPtr(left), left), true);
  }

  public OptionalVec2f OnRightTwoFingerSliding(Point3fList right) {
    return new OptionalVec2f(GestureEstimateJNI.SimpleDynamicGesture_OnRightTwoFingerSliding(swigCPtr, this, Point3fList.getCPtr(right), right), true);
  }

  public OptionalVec2f OnRightArmDown(Point3fList body) {
    return new OptionalVec2f(GestureEstimateJNI.SimpleDynamicGesture_OnRightArmDown(swigCPtr, this, Point3fList.getCPtr(body), body), true);
  }

  public OptionalVec2f OnLeftArmDown(Point3fList body) {
    return new OptionalVec2f(GestureEstimateJNI.SimpleDynamicGesture_OnLeftArmDown(swigCPtr, this, Point3fList.getCPtr(body), body), true);
  }

  public boolean OnFistBump(Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnFistBump(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public boolean OnLeftPunching(Point3fList body, Point3fList left) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnLeftPunching(swigCPtr, this, Point3fList.getCPtr(body), body, Point3fList.getCPtr(left), left);
  }

  public boolean OnRightPunching(Point3fList body, Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnRightPunching(swigCPtr, this, Point3fList.getCPtr(body), body, Point3fList.getCPtr(right), right);
  }

  public OptionalFloat OnZoom(Point3fList left, Point3fList right) {
    return new OptionalFloat(GestureEstimateJNI.SimpleDynamicGesture_OnZoom(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right), true);
  }

  public boolean OnWaveLeftHand(Point3fList left) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnWaveLeftHand(swigCPtr, this, Point3fList.getCPtr(left), left);
  }

  public boolean OnWaveRightHand(Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnWaveRightHand(swigCPtr, this, Point3fList.getCPtr(right), right);
  }

  public boolean OnFlipLeftPalm(Point3fList left) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnFlipLeftPalm(swigCPtr, this, Point3fList.getCPtr(left), left);
  }

  public boolean OnFlipRightPalm(Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnFlipRightPalm(swigCPtr, this, Point3fList.getCPtr(right), right);
  }

  public boolean OnClapHands(Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnClapHands(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public boolean OnTranslateLeftFist(Point3fList left) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnTranslateLeftFist(swigCPtr, this, Point3fList.getCPtr(left), left);
  }

  public boolean OnTranslateRightFist(Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnTranslateRightFist(swigCPtr, this, Point3fList.getCPtr(right), right);
  }

  public boolean OnCloseOpen(Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnCloseOpen(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public boolean OnDragLeft(Point3fList left) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnDragLeft(swigCPtr, this, Point3fList.getCPtr(left), left);
  }

  public boolean OnDragRight(Point3fList right) {
    return GestureEstimateJNI.SimpleDynamicGesture_OnDragRight(swigCPtr, this, Point3fList.getCPtr(right), right);
  }

  public ActionPoseV2Enum OnActionV2(Point3fList body) {
    return ActionPoseV2Enum.swigToEnum(GestureEstimateJNI.SimpleDynamicGesture_OnActionV2(swigCPtr, this, Point3fList.getCPtr(body), body));
  }

  public SlipEnum OnSlip(Point3fList body) {
    return SlipEnum.swigToEnum(GestureEstimateJNI.SimpleDynamicGesture_OnSlip(swigCPtr, this, Point3fList.getCPtr(body), body));
  }

  public void SetJumpThres(float up_shoulder_thres, float up_hip_thres, float up_ankle_thres, int max_frame_count) {
    GestureEstimateJNI.SimpleDynamicGesture_SetJumpThres(swigCPtr, this, up_shoulder_thres, up_hip_thres, up_ankle_thres, max_frame_count);
  }

  public boolean IsJump(Point3fList body) {
    return GestureEstimateJNI.SimpleDynamicGesture_IsJump(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public void SetRunWalkAndStandThres(float foot_chg_thres, float run_wrists_height_thres, int max_frame_count, int chg_status_frame_count) {
    GestureEstimateJNI.SimpleDynamicGesture_SetRunWalkAndStandThres(swigCPtr, this, foot_chg_thres, run_wrists_height_thres, max_frame_count, chg_status_frame_count);
  }

  public ActionPoseV2Enum OnRunWalkAndStandV2(Point3fList body) {
    return ActionPoseV2Enum.swigToEnum(GestureEstimateJNI.SimpleDynamicGesture_OnRunWalkAndStandV2(swigCPtr, this, Point3fList.getCPtr(body), body));
  }

  public SlipEnum OnSlipV2LeftHand(Point3fList left, Point3fList body) {
    return SlipEnum.swigToEnum(GestureEstimateJNI.SimpleDynamicGesture_OnSlipV2LeftHand(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(body), body));
  }

  public SlipEnum OnSlipV2RightHand(Point3fList right, Point3fList body) {
    return SlipEnum.swigToEnum(GestureEstimateJNI.SimpleDynamicGesture_OnSlipV2RightHand(swigCPtr, this, Point3fList.getCPtr(right), right, Point3fList.getCPtr(body), body));
  }

  public void SetSlipV2MoveThres(float hand_horizontal_move_thres, float hand_vertical_move_thres, float hand_horizontal_move_slope_angle, float hand_vertical_move_slope_angle) {
    GestureEstimateJNI.SimpleDynamicGesture_SetSlipV2MoveThres(swigCPtr, this, hand_horizontal_move_thres, hand_vertical_move_thres, hand_horizontal_move_slope_angle, hand_vertical_move_slope_angle);
  }

  public SimpleDynamicGesture() {
    this(GestureEstimateJNI.new_SimpleDynamicGesture(), true);
  }

}
