package skt.model.execution.impl;

import jep.Interpreter;
import jep.MainInterpreter;
import jep.SharedInterpreter;
import skt.util.TestVariables;

public class SharedJepInterpreter {
    private static Interpreter interpeter = null;

    public static Interpreter getSharedJepInterpreter() {
        if (interpeter == null) {
            try {
                MainInterpreter.setJepLibraryPath(TestVariables.jepLibraryPath);
                interpeter = new SharedInterpreter();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(1);
            }
        }
        return interpeter;
    }
}