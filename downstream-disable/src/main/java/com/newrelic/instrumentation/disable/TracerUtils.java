package com.newrelic.instrumentation.disable;

import com.newrelic.agent.Transaction;
import com.newrelic.agent.TransactionActivity;
import com.newrelic.agent.config.AgentConfig;
import com.newrelic.agent.config.AgentConfigListener;
import com.newrelic.agent.config.ConfigService;
import com.newrelic.agent.service.ServiceFactory;
import com.newrelic.agent.tracers.ClassMethodSignature;
import com.newrelic.agent.tracers.Tracer;
import com.newrelic.api.agent.NewRelic;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class TracerUtils implements AgentConfigListener {
  private static final ConcurrentHashMap<ClassMethodSignature, ClassMethodConfig> mappings = new ConcurrentHashMap<>();
  
  private static int THRESHOLD = 250;
  
  private static final String THRESHOLD_SETIING = "jdbc_threshold.threshold";
  
  static {
    NewRelic.getAgent().getLogger().log(Level.INFO, "Initializing TracerUtils");
    ConfigService configService = ServiceFactory.getConfigService();
    configService.addIAgentConfigListener(new TracerUtils());
    Object threshold = configService.getLocalAgentConfig().getValue("jdbc_threshold.threshold");
    if (threshold != null) {
      if (threshold instanceof Number) {
        THRESHOLD = ((Number)threshold).intValue();
      } else {
        String str = threshold.toString();
        char c = str.charAt(0);
        if (Character.isDigit(c))
          try {
            int tmp = Integer.parseInt(str);
            THRESHOLD = tmp;
          } catch (NumberFormatException numberFormatException) {} 
      } 
      NewRelic.getAgent().getLogger().log(Level.INFO, "Set Threshold Limit to {0}", Integer.valueOf(THRESHOLD));
    } 
  }
  
  public static ClassMethodConfig getClassMethodConfig(ClassMethodSignature sig) {
    return mappings.get(sig);
  }
  
  public static void addTracerConfig(ClassMethodConfig config) {
    ClassMethodSignature sig = config.getSig();
    if (!mappings.containsKey(sig))
      mappings.put(sig, config); 
  }
  
  public static Tracer getTracer(int size, ClassMethodSignature sig, Object obj) {
    Tracer tracer;
    Transaction txn = Transaction.getTransaction();
    if (txn == null)
      return null; 
    if (size > THRESHOLD) {
      tracer = new DisableCustomTracer(txn.getTransactionActivity(), sig, "Disable");
    } else {
      tracer = null;
    } 
    if (tracer != null) {
      tracer.addCustomAttribute("NumberOfIds", Integer.valueOf(size));
      TransactionActivity txa = txn.getTransactionActivity();
      Tracer parent = txa.getLastTracer();
      tracer.setParentTracer(parent);
      txa.tracerStarted(tracer);
    } 
    return tracer;
  }
  
  public void configChanged(String appName, AgentConfig agentConfig) {
    Object threshold = agentConfig.getValue("jdbc_threshold.threshold");
    if (threshold != null) {
      if (threshold instanceof Number) {
        THRESHOLD = ((Number)threshold).intValue();
      } else {
        String str = threshold.toString();
        char c = str.charAt(0);
        if (Character.isDigit(c))
          try {
            int tmp = Integer.parseInt(str);
            THRESHOLD = tmp;
          } catch (NumberFormatException numberFormatException) {} 
      } 
      NewRelic.getAgent().getLogger().log(Level.INFO, "Set Threshold Limit to {0}", Integer.valueOf(THRESHOLD));
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
}
