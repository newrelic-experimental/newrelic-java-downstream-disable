package com.example;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;

@Weave(type = MatchType.BaseClass)
public abstract class AbstractBaseClass {

	@Trace
	public abstract void makeExternalCall();

}
