package com.ampaschal.google.agents.agent2;

import com.ampaschal.google.PermissionsManager;
import com.ampaschal.google.TestHelper;
import com.ampaschal.google.agents.agent4.PermissionsAgent;
import com.ampaschal.google.entities.PermissionArgs;
import com.ampaschal.google.entities.TransformProps;
import com.ampaschal.google.enums.ProfileKey;
import com.ampaschal.google.transformers.PermissionsTransformer;
import com.ampaschal.google.utils.Utils;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Map;

public class ExecPermissionsAgent {


    public static void premain(String agentArgs, Instrumentation inst) {

        TestHelper.logTime(ProfileKey.AGENT_CALLED);

        System.out.println("Exec Permissions Agent");

        PermissionArgs permissionArgs = Utils.processAgentArgs(agentArgs);

        PermissionsManager.setup(permissionArgs);

        Map<String, TransformProps> transformPropsMap = PermissionsAgent.getTransformPropMap(false, false, false, true, false);

        inst.addTransformer(new PermissionsTransformer(transformPropsMap, false), true);

        try {
            // We retransform these classes because they are already loaded into the JVM
            inst.retransformClasses(ProcessBuilder.class);
        } catch (UnmodifiableClassException e) {
            throw new RuntimeException(e);
        }

        TestHelper.logTime(ProfileKey.AGENT_EXITING);


    }
}
