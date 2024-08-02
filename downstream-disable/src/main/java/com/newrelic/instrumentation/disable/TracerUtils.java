package com.newrelic.instrumentation.disable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.api.agent.NewRelic;

public class TracerUtils implements AgentConfigListener {
	private static final ConcurrentHashMap<ClassMethodSignature, ClassMethodConfig> mappings = new ConcurrentHashMap<>();

	static {
		NewRelic.getAgent().getLogger().log(Level.INFO, "Initializing TracerUtils");
	}

	public static ClassMethodConfig getClassMethodConfig(ClassMethodSignature sig) {
		return mappings.get(sig);
	}

	public static void addTracerConfig(ClassMethodConfig config) {
		ClassMethodSignature sig = config.getSig();
		if (!mappings.containsKey(sig))
			mappings.put(sig, config);
	}

	public static String getMethodDescriptor(String returnType, String... args) {
		StringBuffer sb = new StringBuffer("(");
		for (String arg : args)
			sb.append(getDescriptor(arg));
		sb.append(')');
		sb.append(getDescriptor(returnType));
		return sb.toString();
	}

	private static String getDescriptor(String s) {
		String tmp = s.replace('.', '/');
		if (tmp.equals("int"))
			return "I";
		if (tmp.equals("void"))
			return "V";
		if (tmp.equals("boolean"))
			return "Z";
		if (tmp.equals("byte"))
			return "B";
		if (tmp.equals("char"))
			return "C";
		if (tmp.equals("short"))
			return "S";
		if (tmp.equals("double"))
			return "D";
		if (tmp.equals("float"))
			return "F";
		if (tmp.equals("long"))
			return "J";
		if (tmp.contains("[]"))
			return "[" + tmp.replace("[]", "");
		return "L" + tmp + ";";
	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		// TODO Auto-generated method stub

	}
}
