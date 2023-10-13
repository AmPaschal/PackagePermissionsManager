package com.ampaschal.google.agents.agent3;

import com.ampaschal.google.PermissionsManager;
import com.ampaschal.google.TestHelper;
import com.ampaschal.google.enums.ProfileKey;
import com.ampaschal.google.transformers.PermissionsTransformer;

import java.io.FileInputStream;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;

public class NoTransformationAgent {
    public static void premain(String agentArgs, Instrumentation inst) {

        TestHelper.logTime(ProfileKey.AGENT_CALLED);

        System.out.println("No transformation Agent");

        boolean monitorMode;
        boolean enforceMode;
        long duration;
        String[] args = agentArgs.split(",");
        monitorMode = args[0].contains("m");
        enforceMode = args[0].contains("e");
        duration = Long.parseLong(agentArgs.replaceAll("-?[^\\d]", ""));
        PermissionsManager.setup(monitorMode, enforceMode, duration, args[1]);
        TestHelper.logTime(ProfileKey.AGENT_EXITING);

    }
}
