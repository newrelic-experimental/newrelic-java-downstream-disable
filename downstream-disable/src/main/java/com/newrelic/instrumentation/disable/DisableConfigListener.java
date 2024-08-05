package com.newrelic.instrumentation.disable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.json.simple.JSONArray;
import com.newrelic.agent.deps.org.json.simple.JSONObject;
import com.newrelic.agent.deps.org.json.simple.parser.JSONParser;
import com.newrelic.agent.deps.org.json.simple.parser.ParseException;
import com.newrelic.agent.instrumentation.classmatchers.ChildClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ExactClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.InterfaceMatcher;
import com.newrelic.agent.instrumentation.classmatchers.OptimizedClassMatcherBuilder;
import com.newrelic.agent.instrumentation.context.ClassMatchVisitorFactory;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.api.agent.NewRelic;

public class DisableConfigListener implements Runnable {
	private static final String CLASSNAME = "classname";
	private static final String CLASSTYPE = "classType";
	private static final String EXACTCLASS = "exactclass";
	private static final String BASECLASS = "baseclass";
	private static final String INTERFACE = "interface";
	private static File configFile = null;
	private static long lastModified = System.currentTimeMillis();
	private static Set<ClassMatchVisitorFactory> classMatchers = new HashSet<>();

	static {
		String configFileName = "downstream-disable.json"; // Updated file name
		File agentDir = ConfigFileHelper.getNewRelicDirectory();
		configFile = new File(agentDir, configFileName);
	}

	protected static void initialize() throws IOException, ParseException {
		JSONObject json = new JSONObject();
		JSONParser parser = new JSONParser();
		FileReader reader = new FileReader(configFile);
		json = (JSONObject) parser.parse(reader);
		process(json);
	}

	@Override
	public void run() {
		if (configFile != null && configFile.lastModified() > lastModified) {
			try {
				JSONObject json = new JSONObject();
				JSONParser parser = new JSONParser();
				FileReader reader = new FileReader(configFile);
				json = (JSONObject) parser.parse(reader);
				process(json);
				lastModified = configFile.lastModified();
				retransformMatchingClasses();
			} catch (FileNotFoundException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e,
						"Failed to find downstream-disable.json in the agent directory");
			} catch (IOException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e,
						"Failed to open downstream-disable.json in the agent directory");
			} catch (ParseException e) {
				NewRelic.getAgent().getLogger().log(Level.FINE, e,
						"Failed to parse the contents of downstream-disable.json in the agent directory");
			}
		}
	}

	protected static void process(JSONObject json) {
		if (json != null) {
			JSONArray jArray = (JSONArray) json.get("toDisable");
			DisableMethodMatcher methodMatcher = DisableMethodMatcher.getInstance();

			DisableClassMatcher disableClassMatcher = DisableClassMatcher.getInstance();
			disableClassMatcher.clear(); // Clear existing class matchers
			classMatchers.clear(); // Clear existing class match visitor factories
			if (!methodMatcher.isEmpty())
				methodMatcher.clear();

			List<ClassMatcher> matchers = new ArrayList<>();
			if (jArray != null && !jArray.isEmpty()) {
				for (Object obj : jArray) {
					if (obj instanceof JSONObject) {
						JSONObject classJson = (JSONObject) obj;
						String className = (String) classJson.get(CLASSNAME);
						TraceType type = TraceType.EXACTCLASS;
						if (className != null && !className.isEmpty()) {
							ChildClassMatcher childClassMatcher = null;
							InterfaceMatcher interfaceMatcher = null;
							ExactClassMatcher exactClassMatcher = null;
							String classType = (String) classJson.get(CLASSTYPE);
							if (classType != null && !classType.isEmpty()) {
								if (classType.toLowerCase().equals(BASECLASS)) {
									type = TraceType.BASECLASS;
								} else if (classType.toLowerCase().equals(INTERFACE)) {
									type = TraceType.INTERFACE;
								} else if (classType.toLowerCase().equals(EXACTCLASS)) {
									type = TraceType.EXACTCLASS;
								}
							}
							switch (type) {
							case BASECLASS:
								childClassMatcher = new ChildClassMatcher(className, false);
								break;
							case INTERFACE:
								interfaceMatcher = new InterfaceMatcher(className);
								break;
							case EXACTCLASS:
								exactClassMatcher = new ExactClassMatcher(className);
								break;
							}
							if (exactClassMatcher != null)
								matchers.add(exactClassMatcher);
							if (interfaceMatcher != null)
								matchers.add(interfaceMatcher);
							if (childClassMatcher != null)
								matchers.add(childClassMatcher);
							JSONArray methodArray = (JSONArray) classJson.get("methods");
							if (methodArray != null && !methodArray.isEmpty()) {
								for (Object obj2 : methodArray) {
									if (obj2 instanceof JSONObject) {
										JSONObject methodObj = (JSONObject) obj2;
										String methodName = (String) methodObj.get("methodName");
										String returnType = (String) methodObj.get("returnType");
										JSONArray argsArray = (JSONArray) methodObj.get("args");
										String methodDesc = null;
										if (methodName != null && returnType != null && argsArray != null) {
											String[] args = new String[argsArray.size()];
											for (int i = 0; i < argsArray.size(); i++)
												args[i] = argsArray.get(i).toString();
											String desc = TracerUtils.getMethodDescriptor(returnType, args);
											methodMatcher.addMethod(methodName, desc);
											methodDesc = desc;
										}
										ClassMethodConfig config = new ClassMethodConfig();
										config.setClassName(className);
										config.setMethodName(methodName);
										config.setType(type);
										ClassMethodSignature sig = new ClassMethodSignature(className, methodName,
												methodDesc);
										config.setSig(sig);
										Object positionObj = methodObj.get("collectionPosition");
										if (positionObj != null && positionObj instanceof Number) {
											Number number = (Number) positionObj;
											config.setCollectionMarker(number.intValue());
										}
										NewRelic.getAgent().getLogger().log(Level.FINE, "Adding ClassMethodConfig: {0}",
												config);
										TracerUtils.addTracerConfig(config);
									}
								}
							}
						}
					}
				}
			}
			if (!matchers.isEmpty()) {
				// DisableClassMatcher disableClassMatcher = DisableClassMatcher.getInstance();
				disableClassMatcher.clear();
				disableClassMatcher.addAllMatchers(matchers);

				OptimizedClassMatcherBuilder builder = OptimizedClassMatcherBuilder.newBuilder();
				builder.addClassMethodMatcher(new DisableClassAndMethodMatcher(disableClassMatcher, methodMatcher));
				classMatchers.add(builder.build());
			}
		}
	}

	private void retransformMatchingClasses() {
		try {
			Class<?>[] allLoadedClasses = ServiceFactory.getCoreService().getInstrumentation().getAllLoadedClasses();
			ServiceFactory.getClassTransformerService().retransformMatchingClassesImmediately(allLoadedClasses,
					classMatchers);
			NewRelic.getAgent().getLogger().log(Level.INFO,
					"Re-transformed matching classes based on updated configuration");
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.SEVERE, e, "Failed to re-transform matching classes");
		}
	}
}