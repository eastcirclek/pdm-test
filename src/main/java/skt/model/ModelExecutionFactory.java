package skt.model;

import skt.model.execution.impl.JavaAPIExecution;
import skt.model.execution.impl.JepExecution;
import skt.model.execution.impl.ServingAPIExecution;
import skt.util.TestVariables;

public class ModelExecutionFactory {
    private static JavaAPIExecution javaAPIExecutionObject = null;
    private static JepExecution jepExecutionObject = null;
    private static ServingAPIExecution servingAPIExecutionObject = null;

    public static ModelExecutionService getModel(TestVariables.ExecutionMode executionMode) {

        switch (executionMode) {
            case JAVAAPI:
                if (javaAPIExecutionObject == null) {
                    javaAPIExecutionObject = new JavaAPIExecution();
                }
                return javaAPIExecutionObject;
            case JEP:
                if (jepExecutionObject == null) {
                    jepExecutionObject = new JepExecution();
                }
                return jepExecutionObject;
            case SERVINGAPI:
                if (servingAPIExecutionObject == null) {
                    servingAPIExecutionObject = new ServingAPIExecution();
                }
                return servingAPIExecutionObject;
        }
        System.out.println("Execution Mode is unavailable");
        return null;
    }
}