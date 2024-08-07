package com.newrelic.instrumentation.disable;

import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;

public class DisableClassAndMethodMatcher implements ClassAndMethodMatcher {
	private final ClassMatcher classMatcher;
	private final MethodMatcher methodMatcher;

	public DisableClassAndMethodMatcher(ClassMatcher classMatcher, MethodMatcher methodMatcher) {
		this.classMatcher = classMatcher;
		this.methodMatcher = methodMatcher;
	}

	public DisableClassAndMethodMatcher() {
		this.classMatcher = DisableClassMatcher.getInstance();
		this.methodMatcher = DisableMethodMatcher.getInstance();
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