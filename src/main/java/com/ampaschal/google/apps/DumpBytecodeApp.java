package com.ampaschal.google.apps;

import java.io.IOException;

public class DumpBytecodeApp {

    public static void main(String[] args) {

        DumpBytecodeInnerClass innerClass = new DumpBytecodeInnerClass();
        try {
            innerClass.dumpBytecode();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
