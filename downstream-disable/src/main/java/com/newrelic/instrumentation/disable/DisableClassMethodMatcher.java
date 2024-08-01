package com.newrelic.instrumentation.disable;

import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;

public class DisableClassMethodMatcher implements ClassAndMethodMatcher {
  private ClassMatcher classMatcher = null;
  
  private MethodMatcher methodMatcher = null;
  
  public DisableClassMethodMatcher() {
    this.classMatcher = DisableClassMatcher.getInstance();
    this.methodMatcher = DisableMethodMatcher.getInstance();
  }
  
  public ClassMatcher getClassMatcher() {
    return this.classMatcher;
  }
  
  public MethodMatcher getMethodMatcher() {
    return this.methodMatcher;
  }
}
