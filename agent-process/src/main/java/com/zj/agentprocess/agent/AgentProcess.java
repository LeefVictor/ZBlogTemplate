package com.zj.agentprocess.agent;

import com.zj.agentprocess.transformer.TestTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentProcess {

    public static void premain(
        String agentArgs, Instrumentation inst) throws UnmodifiableClassException {
        TestTransformer transformer = new TestTransformer(agentArgs);
        inst.addTransformer(transformer);
    }

}
