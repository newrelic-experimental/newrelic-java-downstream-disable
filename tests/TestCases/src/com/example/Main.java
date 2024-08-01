package com.example;

import com.newrelic.api.agent.Trace;

public class Main {
    public static void main(String[] args) {
        ExactClass exactClass = new ExactClass();
        InterfaceClass interfaceClass = new InterfaceClass();
        BaseClass baseClass = new BaseClass();

        for (int i = 0; i < 1000; i++) {
            System.out.println("Iteration " + (i + 1));

            testExactClass(exactClass);
            testInterfaceClass(interfaceClass);
            testBaseClass(baseClass);

            
        }
    }
    @Trace(dispatcher=true)
    private static void testExactClass(ExactClass exactClass) {
        System.out.println("Testing ExactClass:");
        exactClass.makeExternalCall();
       
    }
    @Trace(dispatcher=true)
    private static void testInterfaceClass(InterfaceClass interfaceClass) {
        System.out.println("\nTesting InterfaceClass:");
        interfaceClass.makeExternalCall();
        }
      
    @Trace(dispatcher=true)
    private static void testBaseClass(BaseClass baseClass) {
        System.out.println("\nTesting BaseClass:");
        baseClass.makeExternalCall();
     
    }
}