package com.zj.agentprocess.transformer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

public class TestTransformer implements ClassFileTransformer {

    private List<String> scanPackageName;

    public TestTransformer(String agentArgs) {
        this.scanPackageName = Arrays.asList(agentArgs.split(","));
    }

    private final String source_create = ""
        + "   "
        + "        reqChain = new ReqChain();"
        + "        chainRequestObj = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();"
        + "        reqChain.setMethodName(\"%s\");"
        + "        reqChain.setTime_start(System.currentTimeMillis());"
        + "        chainRequestObj.setAttribute(ReqChain.ATTRIBUTE_NAME, reqChain);"
        + "   ";

    private final String source_current = ""
        + "   "
        + "        chainRequestObj = ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();"
        + "        reqChain = ReqChain.getCurrentChain(\"%s\", chainRequestObj);"
        + "   ";

    private final String source_finish = ""
        + "   if (reqChain != null) {"
        + "        reqChain.finish();"
        + "   }";
    private final String source_print = ""
        + "   if (reqChain != null) { "
        + "        System.out.println(reqChain.print());"
        + "   }";

    @Override
    public byte[] transform(ClassLoader loader,
        String className,
        Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain,
        byte[] classfileBuffer) throws IllegalClassFormatException {

        ClassPool pool = ClassPool.getDefault();
        pool.importPackage("com.zj.agentcore.entity");
        CtClass cc = null;
        try {
            cc = pool.makeClass(new ByteArrayInputStream( classfileBuffer));
        } catch (IOException e) {
            e.printStackTrace();
        }

        CtMethod[] methods = cc.getDeclaredMethods();
        try {
            for (CtMethod m : methods) {
                if (m.getName().equals("main")) {
                    continue;
                }
                for (String s : scanPackageName) {
                    if (m.getLongName().contains(s)) {
                        //直接在insert里面声明的局部变量只能在此此insert的源码中使用， 之后在调用insert的话是不能再用之前的变量的， 因为每次的insert都是已编译并写如字节码， 每次编译后字节码的变量名是有变化的
                        //如果要使用， 就需要这样去声明一个局部变量
                        m.addLocalVariable("reqChain", pool.get("com.zj.agentcore.entity.ReqChain"));
                        m.addLocalVariable("chainRequestObj", pool.get("javax.servlet.http.HttpServletRequest"));
                        if (m.getAnnotation(RequestMapping.class) != null || m.getAnnotation(GetMapping.class) != null || m.getAnnotation(
                            PostMapping.class) != null) {
                            m.insertBefore(String.format(source_create, m.getLongName()));
                            m.insertAfter(source_finish+source_print);
                        } else {
                            m.insertBefore(String.format(source_current, m.getLongName()));
                            m.insertAfter(source_finish);
                        }
                    }
                }
            }
            cc.writeFile("D:/temp");
            return cc.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
            return classfileBuffer;
        }
    }
}
