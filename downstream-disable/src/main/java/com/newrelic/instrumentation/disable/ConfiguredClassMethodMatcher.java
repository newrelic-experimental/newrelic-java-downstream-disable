package com.newrelic.instrumentation.disable;

import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;

public class ConfiguredClassMethodMatcher implements ClassAndMethodMatcher {
	
	private ClassMatcher classMatcher = null;
	private MethodMatcher methodMatcher = null;
	
	public ConfiguredClassMethodMatcher(ClassMatcher cm, MethodMatcher mm) {
		classMatcher = cm;
		methodMatcher = mm;
	}

	@Override
	public ClassMatcher getClassMatcher() {
		return classMatcher;
	}

	@Override
	public MethodMatcher getMethodMatcher() {
		return methodMatcher;
	}

	@Override
	public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConfiguredClassMethodMatcher other = (ConfiguredClassMethodMatcher)obj;
        return classMatcher.equals(other.classMatcher) && methodMatcher.equals(other.methodMatcher);
	}

	@Override
	public String toString() {
		return "ConfiguredClassMethodMatcher" + "@" + hashCode();
	}
	
	
	

}
