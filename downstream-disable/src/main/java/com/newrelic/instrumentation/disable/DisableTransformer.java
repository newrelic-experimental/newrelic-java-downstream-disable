package com.newrelic.instrumentation.disable;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import com.newrelic.agent.InstrumentationProxy;
import com.newrelic.agent.deps.org.objectweb.asm.commons.Method;
import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.classmatchers.OptimizedClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.OptimizedClassMatcherBuilder;
import com.newrelic.agent.instrumentation.context.ClassMatchVisitorFactory;
import com.newrelic.agent.instrumentation.context.ContextClassTransformer;
import com.newrelic.agent.instrumentation.context.InstrumentationContext;
import com.newrelic.agent.instrumentation.context.InstrumentationContextManager;
import com.newrelic.agent.instrumentation.tracing.TraceDetailsBuilder;

public class DisableTransformer implements ContextClassTransformer {
	private final InstrumentationContextManager contextManager;

	private final Map<String, ClassMatchVisitorFactory> matchers = new HashMap<>();

	public DisableTransformer(InstrumentationContextManager mgr, InstrumentationProxy pInstrumentation) {
		this.contextManager = mgr;
	}

	protected ClassMatchVisitorFactory addMatcher(ClassAndMethodMatcher matcher) {
		OptimizedClassMatcherBuilder builder = OptimizedClassMatcherBuilder.newBuilder();
		builder.addClassMethodMatcher(new ClassAndMethodMatcher[] { matcher });
		ClassMatchVisitorFactory matchVisitor = builder.build();
		this.matchers.put(matcher.getClass().getSimpleName(), matchVisitor);
		this.contextManager.addContextClassTransformer(matchVisitor, this);
		return matchVisitor;
	}

	protected void removeMatcher(ClassAndMethodMatcher matcher) {
		ClassMatchVisitorFactory matchVisitor = this.matchers.get(matcher.getClass().getSimpleName());
		if (matchVisitor != null)
			this.contextManager.removeMatchVisitor(matchVisitor);
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer, InstrumentationContext context,
			OptimizedClassMatcher.Match match) throws IllegalClassFormatException {
		for (Method method : match.getMethods()) {
			for (ClassAndMethodMatcher matcher : match.getClassMatches().keySet()) {
				if (matcher.getMethodMatcher().matches(-1, method.getName(), method.getDescriptor(),
						match.getMethodAnnotations(method)))
					context.putTraceAnnotation(method,
							TraceDetailsBuilder.newBuilder().setTracerFactoryName("DisablePreMain")
									.setInstrumentationSourceName("DisablePreMain").build());
			}
		}
		return null;
	}
}
