package com.example;

import com.newrelic.api.agent.Trace;

public abstract class AbstractBaseClass {
	@Trace
	public abstract void makeExternalCall();
}