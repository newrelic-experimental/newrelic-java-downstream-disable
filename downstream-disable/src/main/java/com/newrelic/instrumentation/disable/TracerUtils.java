package com.newrelic.instrumentation.disable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.api.agent.NewRelic;

public class TracerUtils implements AgentConfigListener {

	private static final List<String> removedClasses = new ArrayList<>();
	private static final Map<String, ClassMatcher> matchers = new HashMap<>();

	private static final HashMap<String, ConfiguredClassMethodMatcher>  classAndMethodMatchersMapping = new HashMap<>();
	private static final Set<ConfiguredClassMethodMatcher> classAndMethodMatchers = new HashSet<>();

	private static DisableTransformer transformer = null;


	static {
		NewRelic.getAgent().getLogger().log(Level.INFO, "Initializing TracerUtils");
	}

	public static void setTransformer(DisableTransformer dt) {
		transformer = dt;
	}

	public static DisableTransformer getTransformer() {
		return transformer;
	}

	public static void addClassMethodMatcher(String classname, ConfiguredClassMethodMatcher matcher) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Adding ClassAndMethodMatcher for {0}: {1}", classname, matcher);
		classAndMethodMatchersMapping.put(classname, matcher);
	}

	public static ConfiguredClassMethodMatcher getConfiguredMatcher(String name) {
		return classAndMethodMatchersMapping.get(name);
	}

	public static Collection<ConfiguredClassMethodMatcher> getClassMethodMatchers() {
		Collection<ConfiguredClassMethodMatcher> matcherSet = classAndMethodMatchersMapping.values();
		NewRelic.getAgent().getLogger().log(Level.FINE, "Returning the current set of ClassAndMethodMatchers: {0}", matcherSet);
		return matcherSet;
	}

	public static Set<String> getCurrentTracedClasses() {
		return matchers.keySet();
	}

	public static void setRemoved(Collection<String> removed) {
		removedClasses.clear();
		removedClasses.addAll(removed);
	}

	public static boolean isRemovedClass(String classname) {
		return removedClasses.contains(classname);
	}

	public static void addClassMatcher(String classname, ClassMatcher matcher) {
		matchers.put(classname, matcher);
	}

	public static ClassMatcher getMatcher(String classname) {
		return matchers.get(classname);
	}

	public static void resetMatcherMap() {
		matchers.clear();
	}

	public static Map<String, ClassMatcher> getMatcherMap() {
		return new HashMap<>(matchers);
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

	public static void removeClassMethodMatcher(String classname) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Removing ClassAndMethodMatcher for {0}", classname);
		classAndMethodMatchersMapping.remove(classname);
		matchers.remove(classname);
	}

	@Override
	public void configChanged(String appName, AgentConfig agentConfig) {
		// TODO Auto-generated method stub
	}
}