package com.newrelic.instrumentation.kronos;

import com.newrelic.agent.Transaction;
import com.newrelic.agent.tracers.AbstractTracerFactory;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.Tracer;
import com.newrelic.api.agent.NewRelic;
import java.util.Collection;
import java.util.logging.Level;

public class KronosTracerFactory extends AbstractTracerFactory {
  public Tracer doGetTracer(Transaction transaction, ClassMethodSignature sig, Object object, Object[] args) {
    ClassMethodConfig cmConfig = TracerUtils.getClassMethodConfig(sig);
    int size = -1;
    if (cmConfig != null && 
      cmConfig.getCollectionMarker() != null) {
      int marker = cmConfig.getCollectionMarker().intValue();
      if (marker <= args.length) {
        Object obj = args[marker];
        if (obj instanceof Collection) {
          size = ((Collection)obj).size();
        } else {
          NewRelic.getAgent().getLogger().log(Level.FINE, "The {0} argument of {1}.{2} is not a colletion object, please check the configuration", Integer.valueOf(marker), sig.getClassName(), sig.getMethodName());
        } 
      } else {
        NewRelic.getAgent().getLogger().log(Level.FINE, "There is no {0} argument of {1}.{2} is not a colletion object, please check the configuration", Integer.valueOf(marker), sig.getClassName(), sig.getMethodName());
      } 
    } 
    if (size < 0)
      for (Object param : args) {
        if (param instanceof Collection) {
          size = ((Collection)param).size();
          break;
        } 
      }  
    return TracerUtils.getTracer(size, sig, object);
  }
}
