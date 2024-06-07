/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public final class SlipEnum {
  public final static SlipEnum SlipUp = new SlipEnum("SlipUp");
  public final static SlipEnum SlipDown = new SlipEnum("SlipDown");
  public final static SlipEnum SlipLeft = new SlipEnum("SlipLeft");
  public final static SlipEnum SlipRight = new SlipEnum("SlipRight");
  public final static SlipEnum Unkonwn = new SlipEnum("Unkonwn");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static SlipEnum swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + SlipEnum.class + " with value " + swigValue);
  }

  private SlipEnum(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private SlipEnum(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private SlipEnum(String swigName, SlipEnum swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static SlipEnum[] swigValues = { SlipUp, SlipDown, SlipLeft, SlipRight, Unkonwn };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
