/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class SimpleStaticGesture {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected SimpleStaticGesture(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(SimpleStaticGesture obj) {
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
        GestureEstimateJNI.delete_SimpleStaticGesture(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public boolean OnLeftHandUp(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnLeftHandUp(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public boolean OnRightHandUp(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnRightHandUp(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public boolean OnBothHandUp(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnBothHandUp(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public HandList OnHandUp(Point3fList body) {
    return new HandList(GestureEstimateJNI.SimpleStaticGesture_OnHandUp(swigCPtr, this, Point3fList.getCPtr(body), body), true);
  }

  public void SetLeftHandUpV2Thres(float right_up_arm_right_body_max_angle, float left_small_arm_left_body_min_angle) {
    GestureEstimateJNI.SimpleStaticGesture_SetLeftHandUpV2Thres(swigCPtr, this, right_up_arm_right_body_max_angle, left_small_arm_left_body_min_angle);
  }

  public void SetRightHandUpV2Thres(float left_up_arm_left_body_max_angle, float right_small_arm_right_body_min_angle) {
    GestureEstimateJNI.SimpleStaticGesture_SetRightHandUpV2Thres(swigCPtr, this, left_up_arm_left_body_max_angle, right_small_arm_right_body_min_angle);
  }

  public void SetBothHandUpV2Thres(float right_wirst_shoulder_hip_min_angle, float left_wirst_shoulder_hip_min_angle) {
    GestureEstimateJNI.SimpleStaticGesture_SetBothHandUpV2Thres(swigCPtr, this, right_wirst_shoulder_hip_min_angle, left_wirst_shoulder_hip_min_angle);
  }

  public boolean OnLeftHandUpV2(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnLeftHandUpV2(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public boolean OnRightHandUpV2(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnRightHandUpV2(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public boolean OnBothHandUpV2(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnBothHandUpV2(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public HandList OnHandUpV2(Point3fList body) {
    return new HandList(GestureEstimateJNI.SimpleStaticGesture_OnHandUpV2(swigCPtr, this, Point3fList.getCPtr(body), body), true);
  }

  public boolean OnHandsUpAndFist(Point3fList body, Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleStaticGesture_OnHandsUpAndFist(swigCPtr, this, Point3fList.getCPtr(body), body, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public void SetArmCrossExpandWristThres(float expand_wrist_thres, float arm_shoulder_max_angle, float two_arm_max_angle) {
    GestureEstimateJNI.SimpleStaticGesture_SetArmCrossExpandWristThres(swigCPtr, this, expand_wrist_thres, arm_shoulder_max_angle, two_arm_max_angle);
  }

  public FloatList GetArmCrossExpandWristThres() {
    return new FloatList(GestureEstimateJNI.SimpleStaticGesture_GetArmCrossExpandWristThres(swigCPtr, this), true);
  }

  public OptionalPoint2f OnArmCross(Point3fList body) {
    return new OptionalPoint2f(GestureEstimateJNI.SimpleStaticGesture_OnArmCross(swigCPtr, this, Point3fList.getCPtr(body), body), true);
  }

  public Hand OnQuiet(Point3fList body, Point3fList left, Point3fList right) {
    return Hand.swigToEnum(GestureEstimateJNI.SimpleStaticGesture_OnQuiet(swigCPtr, this, Point3fList.getCPtr(body), body, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right));
  }

  public HandPoseEnum HandPose(Point3fList hand) {
    return HandPoseEnum.swigToEnum(GestureEstimateJNI.SimpleStaticGesture_HandPose(swigCPtr, this, Point3fList.getCPtr(hand), hand));
  }

  public OptionalRect OnTwoIndexFingerRect(Point3fList left, Point3fList right) {
    return new OptionalRect(GestureEstimateJNI.SimpleStaticGesture_OnTwoIndexFingerRect(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right), true);
  }

  public boolean OnHorizontalPalm(Point3fList hand) {
    return GestureEstimateJNI.SimpleStaticGesture_OnHorizontalPalm(swigCPtr, this, Point3fList.getCPtr(hand), hand);
  }

  public boolean OnPrayer(Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleStaticGesture_OnPrayer(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public boolean OnHeart3(Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleStaticGesture_OnHeart3(swigCPtr, this, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public boolean OnHoldFace(Point3fList body, Point3fList left, Point3fList right) {
    return GestureEstimateJNI.SimpleStaticGesture_OnHoldFace(swigCPtr, this, Point3fList.getCPtr(body), body, Point3fList.getCPtr(left), left, Point3fList.getCPtr(right), right);
  }

  public Hand OnLateralRaise(Point3fList body) {
    return Hand.swigToEnum(GestureEstimateJNI.SimpleStaticGesture_OnLateralRaise(swigCPtr, this, Point3fList.getCPtr(body), body));
  }

  public boolean OnClosure(Point3fList body) {
    return GestureEstimateJNI.SimpleStaticGesture_OnClosure(swigCPtr, this, Point3fList.getCPtr(body), body);
  }

  public SimpleStaticGesture() {
    this(GestureEstimateJNI.new_SimpleStaticGesture(), true);
  }

}
