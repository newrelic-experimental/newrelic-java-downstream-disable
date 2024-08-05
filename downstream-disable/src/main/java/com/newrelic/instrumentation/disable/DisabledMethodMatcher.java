package com.newrelic.instrumentation.disable;

import java.util.Set;
import java.util.logging.Level;

import com.newrelic.agent.deps.org.objectweb.asm.commons.Method;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;
import com.newrelic.api.agent.NewRelic;

public class DisabledMethodMatcher implements MethodMatcher {
	
	private String name = null;
	private String description = null;
	
	public DisabledMethodMatcher(String n, String d) {
		name = n;
		description = d;
	}

	@Override
	public boolean matches(int access, String name, String desc, Set<String> annotations) {
		if(this.name.equals(name)) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Found method {0}, description: {1}, desc: {2}", this.name,description, desc);
		}
		return this.name.equals(name) && (description == null || description.equals(desc));
	}

	@Override
	public Method[] getExactMethods() {
		// TODO Auto-generated method stub
		return null;
	}

}
