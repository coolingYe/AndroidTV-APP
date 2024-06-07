/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.GestureEstimate;

public class FloatList extends java.util.AbstractList<Float> implements java.util.RandomAccess {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected FloatList(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(FloatList obj) {
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
        GestureEstimateJNI.delete_FloatList(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public FloatList(float[] initialElements) {
    this();
    reserve(initialElements.length);

    for (float element : initialElements) {
      add(element);
    }
  }

  public FloatList(Iterable<Float> initialElements) {
    this();
    for (float element : initialElements) {
      add(element);
    }
  }

  public Float get(int index) {
    return doGet(index);
  }

  public Float set(int index, Float e) {
    return doSet(index, e);
  }

  public boolean add(Float e) {
    modCount++;
    doAdd(e);
    return true;
  }

  public void add(int index, Float e) {
    modCount++;
    doAdd(index, e);
  }

  public Float remove(int index) {
    modCount++;
    return doRemove(index);
  }

  protected void removeRange(int fromIndex, int toIndex) {
    modCount++;
    doRemoveRange(fromIndex, toIndex);
  }

  public int size() {
    return doSize();
  }

  public FloatList() {
    this(GestureEstimateJNI.new_FloatList__SWIG_0(), true);
  }

  public FloatList(FloatList other) {
    this(GestureEstimateJNI.new_FloatList__SWIG_1(FloatList.getCPtr(other), other), true);
  }

  public long capacity() {
    return GestureEstimateJNI.FloatList_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    GestureEstimateJNI.FloatList_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return GestureEstimateJNI.FloatList_isEmpty(swigCPtr, this);
  }

  public void clear() {
    GestureEstimateJNI.FloatList_clear(swigCPtr, this);
  }

  public FloatList(int count, float value) {
    this(GestureEstimateJNI.new_FloatList__SWIG_2(count, value), true);
  }

  private int doSize() {
    return GestureEstimateJNI.FloatList_doSize(swigCPtr, this);
  }

  private void doAdd(float x) {
    GestureEstimateJNI.FloatList_doAdd__SWIG_0(swigCPtr, this, x);
  }

  private void doAdd(int index, float x) {
    GestureEstimateJNI.FloatList_doAdd__SWIG_1(swigCPtr, this, index, x);
  }

  private float doRemove(int index) {
    return GestureEstimateJNI.FloatList_doRemove(swigCPtr, this, index);
  }

  private float doGet(int index) {
    return GestureEstimateJNI.FloatList_doGet(swigCPtr, this, index);
  }

  private float doSet(int index, float val) {
    return GestureEstimateJNI.FloatList_doSet(swigCPtr, this, index, val);
  }

  private void doRemoveRange(int fromIndex, int toIndex) {
    GestureEstimateJNI.FloatList_doRemoveRange(swigCPtr, this, fromIndex, toIndex);
  }

}