package com.example;

import com.newrelic.api.agent.Trace;
import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(type = MatchType.Interface)
public abstract class ExternalCallInterface {

	@Trace
	public void makeExternalCall() {
		Weaver.callOriginal();
	}
}
