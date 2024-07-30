package com.newrelic.instrumentation.kronos;

import com.newrelic.agent.config.ConfigFileHelper;
import com.newrelic.agent.deps.org.json.simple.JSONArray;
import com.newrelic.agent.deps.org.json.simple.JSONObject;
import com.newrelic.agent.deps.org.json.simple.parser.JSONParser;
import com.newrelic.agent.deps.org.json.simple.parser.ParseException;
import com.newrelic.agent.instrumentation.classmatchers.ChildClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.ExactClassMatcher;
import com.newrelic.agent.instrumentation.classmatchers.InterfaceMatcher;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.api.agent.NewRelic;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class KronosConfigListener implements Runnable {
  private static final String CLASSNAME = "classname";
  
  private static final String CLASSTYPE = "classType";
  
  private static final String EXACTCLASS = "exactclass";
  
  private static final String BASECLASS = "baseclass";
  
  private static final String INTERFACE = "interface";
  
  private static File configFile = null;
  
  private static long lastModified = System.currentTimeMillis();
  
  static {
    String configFileName = "kronos.json";
    File agentDir = ConfigFileHelper.getNewRelicDirectory();
    configFile = new File(agentDir, configFileName);
  }
  
  protected static void initialize() throws IOException, ParseException {
    JSONObject json = new JSONObject();
    JSONParser parser = new JSONParser();
    FileReader reader = new FileReader(configFile);
    json = (JSONObject)parser.parse(reader);
    process(json);
  }
  
  public void run() {
    if (configFile != null && 
      configFile.lastModified() > lastModified)
      try {
        JSONObject json = new JSONObject();
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(configFile);
        json = (JSONObject)parser.parse(reader);
        process(json);
      } catch (FileNotFoundException e) {
        NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to find kronos.json in the agent directory");
      } catch (IOException e) {
        NewRelic.getAgent().getLogger().log(Level.FINE, e, "Failed to open kronos.json in the agent directory");
      } catch (ParseException e) {
        NewRelic.getAgent().getLogger().log(Level.FINE, (Throwable)e, "Failed to parse the contents of kronos.json in the agent directory");
      }  
  }
  
  protected static void process(JSONObject json) {
    if (json != null) {
      JSONArray jArray = (JSONArray)json.get("toTrace");
      KronosMethodMatcher methodMatcher = KronosMethodMatcher.getInstance();
      if (!methodMatcher.isEmpty())
        methodMatcher.clear(); 
      List<ClassMatcher> classMatchers = new ArrayList<>();
      if (jArray != null && !jArray.isEmpty())
        for (Object obj : jArray) {
          if (obj instanceof JSONObject) {
            JSONObject classJson = (JSONObject)obj;
            String className = (String)classJson.get(CLASSNAME);
            TraceType type = TraceType.EXACTCLASS;
            if (className != null && !className.isEmpty()) {
              ChildClassMatcher childClassMatcher = null;
              InterfaceMatcher interfaceMatcher = null;
              ExactClassMatcher exactClassMatcher = null;
              String classType = (String)classJson.get(CLASSTYPE);
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
                classMatchers.add(exactClassMatcher); 
              if(interfaceMatcher != null) {
            	  classMatchers.add(interfaceMatcher);
              }
              if(childClassMatcher != null) {
            	  classMatchers.add(childClassMatcher);
              }
              JSONArray methodArray = (JSONArray)classJson.get("methods");
              if (methodArray != null && !methodArray.isEmpty())
                for (Object obj2 : methodArray) {
                  if (obj2 instanceof JSONObject) {
                    JSONObject methodObj = (JSONObject)obj2;
                    String methodName = (String)methodObj.get("methodName");
                    String returnType = (String)methodObj.get("returnType");
                    JSONArray argsArray = (JSONArray)methodObj.get("args");
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
                    ClassMethodSignature sig = new ClassMethodSignature(className, methodName, methodDesc);
                    config.setSig(sig);
                    Object positionObj = methodObj.get("collectionPosition");
                    if (positionObj != null && positionObj instanceof Number) {
                      Number number = (Number)positionObj;
                      config.setCollectionMarker(number.intValue());
                    } 
                    NewRelic.getAgent().getLogger().log(Level.FINE, "Adding ClassMethodConfig: {0}", config);
                    TracerUtils.addTracerConfig(config);
                  } 
                }  
            } 
          } 
        }  
      if (!classMatchers.isEmpty()) {
        KronosClassMatcher matcher = KronosClassMatcher.getInstance();
        if (!matcher.isEmpty())
          matcher.clear(); 
        matcher.addAllMatchers(classMatchers);
      } 
    } 
  }
}
