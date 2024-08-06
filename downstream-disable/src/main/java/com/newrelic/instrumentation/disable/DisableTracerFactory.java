package com.newrelic.instrumentation.disable;

import java.util.logging.Level;

import com.newrelic.agent.Transaction;
import com.newrelic.agent.tracers.AbstractTracerFactory;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.DefaultTracer;
import com.newrelic.agent.tracers.Tracer;
import com.newrelic.agent.tracers.TracerFlags;
import com.newrelic.agent.tracers.metricname.ClassMethodMetricNameFormat;
import com.newrelic.api.agent.NewRelic;

public class DisableTracerFactory extends AbstractTracerFactory {

	@Override
	public Tracer doGetTracer(Transaction transaction, ClassMethodSignature sig, Object object, Object[] args) {
		if (transaction == null || sig == null) {
			NewRelic.getAgent().getLogger().log(Level.FINE, "Transaction or ClassMethodSignature is null");
			return null;
		}
		
		String className = sig.getClassName();
		ClassMethodMetricNameFormat metricNameFormat = new ClassMethodMetricNameFormat(sig, object);
		if(TracerUtils.isRemovedClass(className)) {
			
			return new DefaultTracer(transaction, sig, object, metricNameFormat);
		}

		int tracerFlags = TracerFlags.GENERATE_SCOPED_METRIC | TracerFlags.TRANSACTION_TRACER_SEGMENT
				| TracerFlags.LEAF;

		return new DefaultTracer(transaction, sig, object, metricNameFormat, tracerFlags);
	}
}