package com.newrelic.instrumentation.kronos;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import com.newrelic.agent.TransactionActivity;
import com.newrelic.agent.bridge.TransactionNamePriority;
import com.newrelic.agent.bridge.external.ExternalParameters;
import com.newrelic.agent.config.TransactionTracerConfig;
import com.newrelic.agent.database.SqlObfuscator;
import com.newrelic.agent.trace.TransactionSegment;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.Tracer;
import com.newrelic.api.agent.InboundHeaders;
import com.newrelic.api.agent.OutboundHeaders;

@SuppressWarnings("deprecation")
public class KronosCustomTracer implements Tracer {
  private final TransactionActivity txa;
  
  private final Tracer parentTracer;
  
  private final long startNanos = System.nanoTime();
  
  private final AtomicLong endNanos = new AtomicLong();
  
  private final String segmentName;
  
  private final ClassMethodSignature classMethodSignature;
  
  public KronosCustomTracer(TransactionActivity transactionActivity, ClassMethodSignature sig, String metricPrefix) {
    this.txa = transactionActivity;
    this.parentTracer = this.txa.getLastTracer();
    this.classMethodSignature = sig;
    this.segmentName = metricPrefix + "/" + this.classMethodSignature.getClassName() + "/" + this.classMethodSignature.getMethodName();
  }
  
  public void finish(int opcode, Object returnValue) {
    doFinish(opcode);
  }
  
  public void finish(Throwable throwable) {
    doFinish(191);
  }
  
  private void doFinish(int opcode) {
    this.txa.tracerFinished(this, opcode);
    this.parentTracer.childTracerFinished(this);
  }
  
  public boolean isMetricProducer() {
    return false;
  }
  
  public TransactionActivity getTransactionActivity() {
    return this.txa;
  }
  
  public Tracer getParentTracer() {
    return this.parentTracer;
  }
  
  public TransactionSegment getTransactionSegment(TransactionTracerConfig ttConfig, SqlObfuscator sqlObfuscator, long startTime, TransactionSegment lastSibling) {
    return new TransactionSegment(ttConfig, sqlObfuscator, startTime, this);
  }
  
  public String getTransactionSegmentName() {
    return this.segmentName;
  }
  
  public Map<String, Object> getAgentAttributes() {
    return Collections.emptyMap();
  }
  
  public Map<String, Object> getCustomAttributes() {
    return Collections.emptyMap();
  }
  
  public ClassMethodSignature getClassMethodSignature() {
    return this.classMethodSignature;
  }
  
  public boolean isTransactionSegment() {
    return false;
  }
  
  public long getStartTimeInMillis() {
    return getStartTimeInMilliseconds();
  }
  
  public long getStartTimeInMilliseconds() {
    return TimeUnit.MILLISECONDS.convert(this.startNanos, TimeUnit.NANOSECONDS);
  }
  
  public long getEndTimeInMilliseconds() {
    return TimeUnit.MILLISECONDS.convert(this.endNanos.get(), TimeUnit.NANOSECONDS);
  }
  
  public long getExclusiveDuration() {
    return getDuration();
  }
  
  public long getDuration() {
    return this.endNanos.get() - this.startNanos;
  }
  
  public String getMetricName() {
    return this.segmentName;
  }
  
  public void addCustomAttribute(String key, Number value) {}
  
  public void addCustomAttribute(String key, String value) {}
  
  public void addCustomAttribute(String key, boolean value) {}
  
  public void addCustomAttributes(Map<String, Object> attributes) {}
  
  public long getStartTime() {
    return 0L;
  }
  
  public long getEndTime() {
    return 0L;
  }
  
  public long getDurationInMilliseconds() {
    return 0L;
  }
  
  public long getRunningDurationInNanos() {
    return 0L;
  }
  
  public void setRollupMetricNames(String... metricNames) {}
  
  public void setMetricNameFormatInfo(String metricName, String transactionSegmentName, String transactionSegmentUri) {}
  
  public void addExclusiveRollupMetricName(String... metricNameParts) {}
  
  public void setCustomMetricPrefix(String prefix) {}
  
  public void setTrackChildThreads(boolean shouldTrack) {}
  
  public boolean trackChildThreads() {
    return false;
  }
  
  public void setTrackCallbackRunnable(boolean shouldTrack) {}
  
  public boolean isTrackCallbackRunnable() {
    return false;
  }
  
  public String getTransactionSegmentUri() {
    return null;
  }
  
  public void setAgentAttribute(String key, Object value) {}
  
  public void removeAgentAttribute(String key) {}
  
  public Object getAgentAttribute(String key) {
    return null;
  }
  
  public void childTracerFinished(Tracer child) {}
  
  public int getChildCount() {
    return 0;
  }
  
  public void setParentTracer(Tracer tracer) {}
  
  public boolean isParent() {
    return false;
  }
  
  public boolean isChildHasStackTrace() {
    return false;
  }
  
  public boolean isLeaf() {
    return true;
  }
  
  public boolean isAsync() {
    return false;
  }
  
  public void removeTransactionSegment() {}
  
  public void markFinishTime() {}
  
  public String getGuid() {
    return null;
  }
  
  public void setNoticedError(Throwable throwable) {}
  
  public Throwable getException() {
    return null;
  }
  
  public void setThrownException(Throwable throwable) {}
  
  public boolean wasExceptionSetByAPI() {
    return false;
  }
  
  public void setMetricName(String... metricNameParts) {}
  
  public void addRollupMetricName(String... metricNameParts) {}
  
  public void addOutboundRequestHeaders(OutboundHeaders outboundHeaders) {}
  
  public void readInboundResponseHeaders(InboundHeaders inboundResponseHeaders) {}
  
  public Object invoke(Object proxy, Method method, Object[] args) {
    return null;
  }

@Override
public void reportAsExternal(com.newrelic.api.agent.ExternalParameters externalParameters) {
}

@Override
public void nameTransaction(TransactionNamePriority arg0) {
}

@Override
public void reportAsExternal(ExternalParameters arg0) {
}

@Override
public com.newrelic.agent.bridge.TracedMethod getParentTracedMethod() {
	return parentTracer;
}

@Override
public com.newrelic.api.agent.ExternalParameters getExternalParameters() {
	return null;
}
}
