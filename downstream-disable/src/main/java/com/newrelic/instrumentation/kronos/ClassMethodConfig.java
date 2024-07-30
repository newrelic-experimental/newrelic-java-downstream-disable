package com.newrelic.instrumentation.kronos;

import com.newrelic.agent.tracers.ClassMethodSignature;

public class ClassMethodConfig {
  private String className;
  
  private String methodName;
  
  private Integer collectionMarker;
  
  private ClassMethodSignature sig;
  
  private TraceType type = TraceType.EXACTCLASS;
  
  public String getClassName() {
    return this.className;
  }
  
  public void setClassName(String className) {
    this.className = className;
  }
  
  public String getMethodName() {
    return this.methodName;
  }
  
  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }
  
  public Integer getCollectionMarker() {
    return this.collectionMarker;
  }
  
  public void setCollectionMarker(int collectionMarker) {
    this.collectionMarker = Integer.valueOf(collectionMarker);
  }
  
  public TraceType getType() {
    return this.type;
  }
  
  public void setType(TraceType type) {
    this.type = type;
  }
  
  public ClassMethodSignature getSig() {
    return this.sig;
  }
  
  public void setSig(ClassMethodSignature sig) {
    this.sig = sig;
  }
  
  public String toString() {
    return "ClassMethodConfig [className=" + this.className + ", methodName=" + this.methodName + ", collectionMarker=" + this.collectionMarker + ", sig=" + this.sig + ", type=" + this.type + "]";
  }
}
