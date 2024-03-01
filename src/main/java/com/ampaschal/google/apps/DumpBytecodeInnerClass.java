package com.ampaschal.google.apps;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.ampaschal.google.enums.ResourceOp;
import com.ampaschal.google.enums.ResourceType;

public class DumpBytecodeInnerClass {
    public void dumpBytecode() throws IOException {

            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            String callerName = stackTrace[1].getClassName(); // 2 represents the index of the class calling the class's
                                                              // constructor

            // This check avoids a StackOverFLow error resulting from an infinite loop where
            // loadClass method tries calling FileInputStream again
            if (!callerName.startsWith("jdk.internal.loader")
                    && !callerName.startsWith("sun.misc.URLClassPath$FileLoader")) {
                try {
                    Class<?> permManagerClass = ClassLoader.getSystemClassLoader()
                            .loadClass("com.ampaschal.google.PermissionsManager");
                    Method logMethod = permManagerClass.getMethod("mockTest", int.class, int.class, Object.class);
                    logMethod.invoke(null, ResourceType.THREAD.getId(), ResourceOp.STOP.getId(), this);
                } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
                    System.out.println("Exception thrown in instrumentation");
                    System.out.println(e);
                } catch (InvocationTargetException e) {
                    Throwable cause = e.getCause();

                    if (cause instanceof IOException) {
                        throw (IOException) cause;
                    }
                }
            }
        }
}
