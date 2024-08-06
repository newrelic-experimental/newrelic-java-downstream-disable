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
import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.api.agent.NewRelic;

public class TracerUtils implements AgentConfigListener {
	
	public enum STATUS { NEW, UPDATED, EXISTING};
	
	private static final List<String> removedClasses = new ArrayList<>();

	private static final HashMap<String, ClassAndMethodMatcher>  classAndMethodMatchersMapping = new HashMap<>();
	private static final HashMap<String, ClassAndMethodMatcher>  removedclassAndMethodMatchersMapping = new HashMap<>();
	private static final HashSet<ClassMethodConfig> currentConfigs = new HashSet<>();
	
	private static DisableTransformer transformer = null;
	
	static {
		NewRelic.getAgent().getLogger().log(Level.INFO, "Initializing TracerUtils");
	}
	
	public static void addTracerConfig(ClassMethodConfig config) {
		currentConfigs.add(config);
	}
	
	public static Set<ClassMethodConfig> getCurrentConfigs() {
		return currentConfigs;
	}
	
	public static boolean removeConfig(ClassMethodConfig config) {
		return currentConfigs.remove(config);
	}
	
	public static void setTransformer(DisableTransformer dt) {
		transformer = dt;
	}
	
	public static DisableTransformer getTransformer() {
		return transformer;
	}
	
	public static void setupForReload() {
		// Purge the removed ClassAndMatchers from the mapping, remove matcher from transformer
		if(!removedClasses.isEmpty()) {
			for(String className : removedClasses) {
				classAndMethodMatchersMapping.remove(className);
				ClassAndMethodMatcher removedMatcher = removedclassAndMethodMatchersMapping.get(className);
				transformer.removeMatcher(removedMatcher);
			}
		}
		removedclassAndMethodMatchersMapping.clear();
	}
	
	public static Map<String, ClassAndMethodMatcher> getCurrentMap() {
		return classAndMethodMatchersMapping;
	}
	
	/*
	 * 
	 */
	public static MatcherAddStatus addClassMethodMatcher(String classname, ClassAndMethodMatcher matcher) {
		NewRelic.getAgent().getLogger().log(Level.FINE, "Call to TracerUtils.addClassMethodMatcher({0},{1})", classname,matcher);
		ClassAndMethodMatcher current = classAndMethodMatchersMapping.get(classname);
		if(current != null) {
			// check if existing is the same, if not then replace
			if(!current.equals(matcher)) {
				classAndMethodMatchersMapping.put(classname, matcher);
				NewRelic.getAgent().getLogger().log(Level.FINE, "Matcher {1} is an update for class {0}", classname,matcher);
				// return true since it was replaced
				return new MatcherAddStatus(STATUS.UPDATED, current, matcher);
			}
		} else {
			classAndMethodMatchersMapping.put(classname, matcher);
			NewRelic.getAgent().getLogger().log(Level.FINE, "Matcher {1} is an new for class {0}", classname,matcher);
			// return true since it is new
			return new MatcherAddStatus(STATUS.NEW, current, matcher);
		}
		// return false because it exists and is the same
		NewRelic.getAgent().getLogger().log(Level.FINE, "Matcher {1} already exists for class {0}", classname,matcher);
		return new MatcherAddStatus(STATUS.EXISTING,matcher,matcher);
	}
	
	public static ClassAndMethodMatcher getConfiguredMatcher(String name) {
		return classAndMethodMatchersMapping.get(name);
	}
	
	public static Collection<ClassAndMethodMatcher> getClassMethodMatchers() {
		Collection<ClassAndMethodMatcher> matcherSet = classAndMethodMatchersMapping.values();
		NewRelic.getAgent().getLogger().log(Level.FINE, "Returning the current set of  ClassAndMethodMatchers: {0}",matcherSet);
		
		return matcherSet;
	}
	
	public static Set<String> getCurrentTracedClasses() {
		return classAndMethodMatchersMapping.keySet();
	}
	
	public static void setRemoved(Collection<String> removed) {
		removedClasses.clear();
		removedClasses.addAll(removed);
	}
	
	public static boolean isRemovedClass(String classname) {
		return removedClasses.contains(classname);
	}

	public static void setRemoved(Map<String, ClassAndMethodMatcher> existingMap, Collection<ClassAndMethodMatcher> removed) {
		removedClasses.clear();
		
		for(ClassAndMethodMatcher matcher : removed) {
			for(String className : existingMap.keySet()) {
				ClassAndMethodMatcher existingMatcher = existingMap.get(className);
				if(matcher.equals(existingMatcher)) {
					removedClasses.add(className);
					removedclassAndMethodMatchersMapping.put(className, matcher);
				}
			}
		}
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
