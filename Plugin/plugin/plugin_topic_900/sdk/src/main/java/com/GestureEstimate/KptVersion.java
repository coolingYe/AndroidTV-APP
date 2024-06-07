/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public final class KptVersion {
  public final static KptVersion KPT_15 = new KptVersion("KPT_15");
  public final static KptVersion KPT_17 = new KptVersion("KPT_17");
  public final static KptVersion KPT_19 = new KptVersion("KPT_19");
  public final static KptVersion KPT_29 = new KptVersion("KPT_29");
  public final static KptVersion KPT_33 = new KptVersion("KPT_33");
  public final static KptVersion KPT_MEDIAPIPE_33 = new KptVersion("KPT_MEDIAPIPE_33");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static KptVersion swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + KptVersion.class + " with value " + swigValue);
  }

  private KptVersion(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private KptVersion(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private KptVersion(String swigName, KptVersion swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static KptVersion[] swigValues = { KPT_15, KPT_17, KPT_19, KPT_29, KPT_33, KPT_MEDIAPIPE_33 };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
