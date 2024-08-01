package com.example;

import com.newrelic.api.agent.Trace;

public interface ExternalCallInterface {
	@Trace
	void makeExternalCall();
}