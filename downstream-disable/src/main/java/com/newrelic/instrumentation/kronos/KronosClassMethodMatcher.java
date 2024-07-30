package com.newrelic.instrumentation.kronos;

import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;

public class KronosClassMethodMatcher implements ClassAndMethodMatcher {
  private ClassMatcher classMatcher = null;
  
  private MethodMatcher methodMatcher = null;
  
  public KronosClassMethodMatcher() {
    this.classMatcher = KronosClassMatcher.getInstance();
    this.methodMatcher = KronosMethodMatcher.getInstance();
  }
  
  public ClassMatcher getClassMatcher() {
    return this.classMatcher;
  }
  
  public MethodMatcher getMethodMatcher() {
    return this.methodMatcher;
  }
}
