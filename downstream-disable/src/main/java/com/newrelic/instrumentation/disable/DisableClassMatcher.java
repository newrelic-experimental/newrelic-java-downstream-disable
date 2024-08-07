package com.newrelic.instrumentation.disable;

import com.newrelic.agent.deps.org.objectweb.asm.ClassReader;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DisableClassMatcher extends ClassMatcher {
  private static DisableClassMatcher INSTANCE = null;
  
  public static DisableClassMatcher getInstance() {
    if (INSTANCE == null)
      INSTANCE = new DisableClassMatcher(); 
    return INSTANCE;
  }
  
  private List<ClassMatcher> matchers = new ArrayList<>();
  
  public void clear() {
    this.matchers.clear();
  }
  
  public boolean isEmpty() {
    return this.matchers.isEmpty();
  }
  
  public void addMatcher(ClassMatcher matcher) {
    this.matchers.add(matcher);
  }
  
  public void addAllMatchers(Collection<ClassMatcher> collection) {
    this.matchers.addAll(collection);
  }
  
  public boolean isMatch(ClassLoader loader, ClassReader cr) {
    boolean b = false;
    for (ClassMatcher matcher : this.matchers) {
      b = matcher.isMatch(loader, cr);
      if (b)
        break; 
    } 
    return b;
  }
  
  public boolean isMatch(Class<?> clazz) {
    boolean b = false;
    for (ClassMatcher matcher : this.matchers) {
      b = matcher.isMatch(clazz);
      if (b)
        break; 
    } 
    return b;
  }
  
  public Collection<String> getClassNames() {
    Collection<String> classNames = new ArrayList<>();
    for (ClassMatcher matcher : this.matchers)
      classNames.addAll(matcher.getClassNames()); 
    return classNames;
  }
}
