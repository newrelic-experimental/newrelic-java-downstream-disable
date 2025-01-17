package com.newrelic.instrumentation.disable;

import com.newrelic.agent.deps.org.objectweb.asm.commons.Method;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DisableMethodMatcher implements MethodMatcher {
  private static DisableMethodMatcher INSTANCE = null;
  
  private List<String> matches;
  
  public static DisableMethodMatcher getInstance() {
    if (INSTANCE == null)
      INSTANCE = new DisableMethodMatcher(); 
    return INSTANCE;
  }
  
  private DisableMethodMatcher() {
    this.matches = new ArrayList<>();
  }
  
  protected boolean isEmpty() {
    return this.matches.isEmpty();
  }
  
  protected void clear() {
    this.matches.clear();
  }
  
  public void addMethod(String name, String desc) {
    this.matches.add(name + "-" + desc);
  }
  
  public Method[] getExactMethods() {
    return null;
  }
  
  public boolean matches(int access, String name, String desc, Set<String> annotations) {
    if (this.matches.contains(name + "-" + desc))
      return true; 
    return false;
  }
}
