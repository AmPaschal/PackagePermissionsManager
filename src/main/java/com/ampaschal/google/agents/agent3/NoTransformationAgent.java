package com.ampaschal.google.agents.agent3;

import java.lang.instrument.Instrumentation;

import com.ampaschal.google.PermissionsManager;
import com.ampaschal.google.TestHelper;
import com.ampaschal.google.entities.PermissionArgs;
import com.ampaschal.google.enums.ProfileKey;
import com.ampaschal.google.utils.Utils;

public class NoTransformationAgent {
    public static void premain(String agentArgs, Instrumentation inst) {

        TestHelper.logTime(ProfileKey.AGENT_CALLED);

        System.out.println("No transformation Agent");

        PermissionArgs permissionArgs = Utils.processAgentArgs(agentArgs);

        PermissionsManager.setup(permissionArgs);
        TestHelper.logTime(ProfileKey.AGENT_EXITING);

    }
}
