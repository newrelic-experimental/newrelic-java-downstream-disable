package com.newrelic.instrumentation.disable;

import com.newrelic.agent.tracers.ClassMethodSignature;

public class ClassMethodConfig {
	private String className;

	private String methodName;

	private ClassMethodSignature sig;

	private TraceType type = TraceType.EXACTCLASS;

	public String getClassName() {
		return this.className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return this.methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public TraceType getType() {
		return this.type;
	}

	public void setType(TraceType type) {
		this.type = type;
	}

	public ClassMethodSignature getSig() {
		return this.sig;
	}

	public void setSig(ClassMethodSignature sig) {
		this.sig = sig;
	}

	@Override
	public String toString() {
		return "ClassMethodConfig [className=" + this.className + ", methodName=" + this.methodName
				+  ", sig=" + this.sig + ", type=" + this.type + "]";
	}
}