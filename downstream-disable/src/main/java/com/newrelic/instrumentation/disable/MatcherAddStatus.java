package com.newrelic.instrumentation.disable;

import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.instrumentation.disable.TracerUtils.STATUS;

public class MatcherAddStatus {

	private TracerUtils.STATUS status;
	private ClassAndMethodMatcher old_matcher;
	private ClassAndMethodMatcher new_matcher;
	
	
	
	public MatcherAddStatus(STATUS status, ClassAndMethodMatcher old_matcher, ClassAndMethodMatcher new_matcher) {
		super();
		this.status = status;
		this.old_matcher = old_matcher;
		this.new_matcher = new_matcher;
	}
	
	public TracerUtils.STATUS getStatus() {
		return status;
	}
	
	public ClassAndMethodMatcher getOld_matcher() {
		return old_matcher;
	}
	
	public ClassAndMethodMatcher getNew_matcher() {
		return new_matcher;
	}
	
	
}
