package com.newrelic.instrumentation.disable;

import com.newrelic.agent.InstrumentationProxy;
import com.newrelic.agent.TracerService;
import com.newrelic.agent.core.CoreService;
import com.newrelic.agent.deps.org.json.simple.parser.ParseException;
import com.newrelic.agent.instrumentation.ClassTransformerService;
import com.newrelic.agent.instrumentation.context.ClassMatchVisitorFactory;
import com.newrelic.agent.instrumentation.context.InstrumentationContextManager;
import com.newrelic.agent.service.AbstractService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.tracers.TracerFactory;
import com.newrelic.api.agent.NewRelic;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class DisableThresholdService extends AbstractService {
    public static final String TRACER_FACTORY_NAME = "DisableThreshold";

    public DisableThresholdService() {
        super("DisableThresholdService");
    }

    public boolean isEnabled() {
        return true;
    }

    protected void doStart() throws Exception {
        boolean started = setup();
        if (!started)
            Executors.newSingleThreadScheduledExecutor().schedule(new SetupThread(), 3L, TimeUnit.SECONDS);
    }

    public boolean setup() {
        DisableClassMethodMatcher matcher = new DisableClassMethodMatcher();
        TracerService tracerService = ServiceFactory.getTracerService();
        ClassTransformerService classTransformerService = ServiceFactory.getClassTransformerService();
        CoreService coreService = ServiceFactory.getCoreService();
        if (classTransformerService != null && coreService != null && tracerService != null) {
            try {
                DisableConfigListener.initialize();
            } catch (IOException e) {
                NewRelic.getAgent().getLogger().log(Level.FINE, e, "IOException while processing Disable Config");
            } catch (ParseException e) {
                NewRelic.getAgent().getLogger().log(Level.FINE, (Throwable) e, "Failed to parse Disable Config");
            }
            tracerService.registerTracerFactory("DisableThreshold", (TracerFactory) new DisableTracerFactory());
            Set<ClassMatchVisitorFactory> classMatchers = new HashSet<>();
            InstrumentationContextManager contextMgr = classTransformerService.getContextManager();
            InstrumentationProxy proxy = coreService.getInstrumentation();
            if (contextMgr != null && proxy != null) {
                DisableTransformer transformer = new DisableTransformer(contextMgr, proxy);
                ClassMatchVisitorFactory matchVisitor = transformer.addMatcher(matcher);
                classMatchers.add(matchVisitor);
                NewRelic.getAgent().getLogger().log(Level.INFO, "Disable transformer started");
                Class<?>[] allLoadedClasses = ServiceFactory.getCoreService().getInstrumentation().getAllLoadedClasses();
                ServiceFactory.getClassTransformerService().retransformMatchingClassesImmediately(allLoadedClasses, classMatchers);
                return true;
            }
        }
        return false;
    }

    protected void doStop() throws Exception {}

    private class SetupThread extends Thread {
        private SetupThread() {}

        public void run() {
            boolean started = false;
            while (!started) {
                started = DisableThresholdService.this.setup();
                if (!started)
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException interruptedException) {}
            }
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        NewRelic.getAgent().getLogger().log(Level.INFO, "DisableThresholdService premain method invoked");
        DisableThresholdService service = new DisableThresholdService();
        ServiceFactory.getServiceManager().addService(service);
        try {
            service.doStart();
        } catch (Exception e) {
            NewRelic.getAgent().getLogger().log(Level.SEVERE, e, "Failed to start DisableThresholdService");
        }
    }
}