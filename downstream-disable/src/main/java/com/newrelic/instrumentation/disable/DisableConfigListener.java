package com.newrelic.instrumentation.disable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.newrelic.agent.instrumentation.classmatchers.ClassAndMethodMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ExactClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.InterfaceMatcher;
import com.newrelic.agent.instrumentation.classmatchers.OptimizedClassMatcherBuilder;
import com.newrelic.agent.instrumentation.context.ClassMatchVisitorFactory;
import com.newrelic.agent.instrumentation.methodmatchers.ExactMethodMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.MethodMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.NameMethodMatcher;
import com.newrelic.agent.instrumentation.methodmatchers.OrMethodMatcher;
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
	private static Set<ClassMatchVisitorFactory> classMatchFactories = new HashSet<>();

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
			NewRelic.getAgent().getLogger().log(Level.FINE, "The file {0} has been modified, will reprocess classes", configFile);
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
			List<String> existingClasses = new ArrayList<String>(TracerUtils.getCurrentTracedClasses()); //disableClassMatcher.getClassNames();
			if(existingClasses != null) {

				NewRelic.getAgent().getLogger().log(Level.FINE, "Existing classes being traced: {0}", existingClasses);
			}
			disableClassMatcher.clear(); // Clear existing class matchers

			// clear classmatcher map
			TracerUtils.resetMatcherMap();

			classMatchFactories.clear(); // Clear existing class match visitor factories
			if (!methodMatcher.isEmpty())
				methodMatcher.clear();

			if (jArray != null && !jArray.isEmpty()) {
				for (Object obj : jArray) {
					if (obj instanceof JSONObject) {
						JSONObject classJson = (JSONObject) obj;
						String className = (String) classJson.get(CLASSNAME);
						existingClasses.remove(className);
						TraceType type = TraceType.EXACTCLASS;
						if (className != null && !className.isEmpty()) {
							//							ChildClassMatcher childClassMatcher = null;
							//							InterfaceMatcher interfaceMatcher = null;
							//							ExactClassMatcher exactClassMatcher = null;
							ClassMatcher classMatcher = null;
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
								classMatcher = new ChildClassMatcher(className, false);
								break;
							case INTERFACE:
								classMatcher = new InterfaceMatcher(className);
								break;
							case EXACTCLASS:
								classMatcher = new ExactClassMatcher(className);
								break;
							}
							//							if (exactClassMatcher != null) {
							//								matchers.add(exactClassMatcher);
							//								TracerUtils.addClassMatcher(className, exactClassMatcher);
							//							}
							//							if (interfaceMatcher != null) {
							//								matchers.add(interfaceMatcher);
							//								TracerUtils.addClassMatcher(className, interfaceMatcher);
							//							}
							//							if (childClassMatcher != null) {
							//								matchers.add(childClassMatcher);
							//								TracerUtils.addClassMatcher(className, childClassMatcher);
							//							}
							TracerUtils.addClassMatcher(className,classMatcher);
							if (!existingClasses.isEmpty() && classMatcher != null) {
								if(existingClasses.contains(className)) {
									boolean removed = existingClasses.remove(className);
									if (removed) {
										NewRelic.getAgent().getLogger().log(Level.FINE, "Removed class {0} from list of existing, currentExisting: {1}", className, existingClasses);
									} else {
										NewRelic.getAgent().getLogger().log(Level.FINE, "Failed to removed class {0} from list of existing, currentExisting: {1}", className, existingClasses);
									} 
								}
							}
							JSONArray methodArray = (JSONArray) classJson.get("methods");
							Set<MethodMatcher> methodMatchers = new HashSet<>();
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
										
										NewRelic.getAgent().getLogger().log(Level.FINE,"Formed method description for class: {0} : {1}", className, methodDesc);

										
										if(methodDesc != null) {
											MethodMatcher matcher = new DisabledMethodMatcher(methodName, methodDesc);
											methodMatchers.add(matcher);
										} else if(methodName != null && !methodName.isEmpty()) {
											MethodMatcher matcher = new DisabledMethodMatcher(methodName,null);
											methodMatchers.add(matcher);
										}
										ClassMethodConfig config = new ClassMethodConfig();
										config.setClassName(className);
										config.setMethodName(methodName);
										config.setType(type);
										ClassMethodSignature sig = new ClassMethodSignature(className, methodName, methodDesc);
										config.setSig(sig);
										NewRelic.getAgent().getLogger().log(Level.FINE, "Adding ClassMethodConfig: {0}",config);
										//										TracerUtils.addTracerConfig(config);
									}
								}
							}
							if(!methodMatchers.isEmpty()) {
								MethodMatcher multiMethodMatcher = OrMethodMatcher.getMethodMatcher(methodMatchers);
								ConfiguredClassMethodMatcher classMethod = new ConfiguredClassMethodMatcher(classMatcher, multiMethodMatcher);
								TracerUtils.addClassMethodMatcher(className,classMethod);
							}
						}
					}
				}
			}

			Collection<ConfiguredClassMethodMatcher> configured = TracerUtils.getClassMethodMatchers();
			NewRelic.getAgent().getLogger().log(Level.FINE, "The set of Configured Matchers is {0}", configured);
			if(!configured.isEmpty()) {
				DisableTransformer transformer = TracerUtils.getTransformer();

				transformer.resetMatchers();
				
				for(ClassAndMethodMatcher matcher : TracerUtils.getClassMethodMatchers()) {
					ClassMatchVisitorFactory matchVisitor = transformer.addMatcher(matcher);
					classMatchFactories.add(matchVisitor);
				}
		

				if(!existingClasses.isEmpty()) {
					NewRelic.getAgent().getLogger().log(Level.FINE, "There are existing classses that are no longer being traced: {0}", existingClasses);
					TracerUtils.setRemoved(existingClasses);
					for(String classname : existingClasses) {
						ConfiguredClassMethodMatcher removedMatcher = TracerUtils.getConfiguredMatcher(classname);
						ClassMatchVisitorFactory matchVisitor = transformer.addMatcher(removedMatcher);
						
						classMatchFactories.add(matchVisitor);
					}
					
				}


			}

			//			if (!matchers.isEmpty()) {
			//				// DisableClassMatcher disableClassMatcher = DisableClassMatcher.getInstance();
			//				disableClassMatcher.clear();
			//				disableClassMatcher.addAllMatchers(matchers);
			//
			//				OptimizedClassMatcherBuilder builder = OptimizedClassMatcherBuilder.newBuilder();
			//				builder.addClassMethodMatcher(new DisableClassAndMethodMatcher(disableClassMatcher, methodMatcher));
			//				classMatchFactories.add(builder.build());
			//			}
		}
	}

	private void retransformMatchingClasses() {
		try {
			Class<?>[] allLoadedClasses = ServiceFactory.getCoreService().getInstrumentation().getAllLoadedClasses();
			ServiceFactory.getClassTransformerService().retransformMatchingClassesImmediately(allLoadedClasses,
					classMatchFactories);
			NewRelic.getAgent().getLogger().log(Level.INFO,
					"Re-transformed matching classes based on updated configuration");
		} catch (Exception e) {
			NewRelic.getAgent().getLogger().log(Level.SEVERE, e, "Failed to re-transform matching classes");
		}
	}
}