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

}
