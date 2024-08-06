package com.newrelic.instrumentation.disable;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import com.newrelic.agent.InstrumentationProxy;
import com.newrelic.agent.TracerService;
import com.newrelic.agent.core.CoreService;
import com.newrelic.agent.deps.org.json.simple.parser.ParseException;
import com.newrelic.agent.instrumentation.ClassTransformerService;
import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.context.ClassMatchVisitorFactory;
import com.newrelic.agent.instrumentation.context.InstrumentationContextManager;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.api.agent.NewRelic;

public class DisablePreMain {
	public static final String TRACER_FACTORY_NAME = "DisablePreMain";

	public DisablePreMain() {
	}

	protected void doStart() throws Exception {
		boolean started = setup();
		if (!started)
			Executors.newSingleThreadScheduledExecutor().schedule(new SetupThread(), 3L, TimeUnit.SECONDS);
	}

	public boolean setup() {
		TracerService tracerService = ServiceFactory.getTracerService();
		ClassTransformerService classTransformerService = ServiceFactory.getClassTransformerService();
		CoreService coreService = ServiceFactory.getCoreService();
		if (classTransformerService != null && coreService != null && tracerService != null) {
			tracerService.registerTracerFactory("DisablePreMain", new DisableTracerFactory());
			Set<ClassMatchVisitorFactory> classMatchers = new HashSet<>();
			InstrumentationContextManager contextMgr = classTransformerService.getContextManager();
			InstrumentationProxy proxy = coreService.getInstrumentation();
			if (contextMgr != null && proxy != null) {
				DisableTransformer transformer = new DisableTransformer(contextMgr, proxy);
				TracerUtils.setTransformer(transformer);
				try {
					DisableConfigListener.initialize();
				} catch (IOException e) {
					NewRelic.getAgent().getLogger().log(Level.FINE, e, "IOException while processing Disable Config");
				} catch (ParseException e) {
					NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to parse Disable Config");
				}
				for(ClassAndMethodMatcher matcher : TracerUtils.getClassMethodMatchers()) {
					ClassMatchVisitorFactory matchVisitor = transformer.addMatcher(matcher);
					classMatchers.add(matchVisitor);
				}
				NewRelic.getAgent().getLogger().log(Level.INFO, "Disable transformer started");
				Class<?>[] allLoadedClasses = ServiceFactory.getCoreService().getInstrumentation()
						.getAllLoadedClasses();
				ServiceFactory.getClassTransformerService().retransformMatchingClassesImmediately(allLoadedClasses,
						classMatchers);
				return true;
			}
		}
		return false;
	}

	private class SetupThread extends Thread {
		private SetupThread() {
		}

		@Override
		public void run() {
			boolean started = false;
			while (!started) {
				started = DisablePreMain.this.setup();
				if (!started)
					try {
						Thread.sleep(500L);
					} catch (InterruptedException interruptedException) {
					}
			}
		}
	}

	public static void premain(String agentArgs, Instrumentation inst) {
		NewRelic.getAgent().getLogger().log(Level.INFO, "DisablePreMain premain method invoked");
		DisablePreMain service = new DisablePreMain();
		try {
			service.doStart();
			// Schedule DisableConfigListener to run every minute
			ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(new DisableConfigListener(), 0, 1, TimeUnit.MINUTES);
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.SEVERE, e, "Failed to start DisablePreMain");
		}
	}
}