package com.zj.agentcore.entity;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import lombok.Data;

@Data
public class ReqChain {

    public static final String ATTRIBUTE_NAME = "com-zj-agent-req-per-chain";

    private String methodName;
    private long time_start;
    private long time_end;

    private volatile int status;
    private List<ReqChain> usages = new ArrayList<>(); //整个方法体所调用的方法类
    private volatile int currentUsageIndex = 0;//usages中当前处理方法的下标

    public ReqChain() {
    }

    public static ReqChain getCurrentChain(String methodName, HttpServletRequest request){
        //获取当前请求中的对象
        ReqChain chain = (ReqChain) request.getAttribute(ATTRIBUTE_NAME);

        ReqChain parent = chain;
        for (int i = chain.getUsages().size() - 1; i >=0 ; i--) {
            ReqChain temp = chain.getUsages().get(i);
            if (temp.getStatus() == 0) {
                parent = temp;
                break;
            }
        }
        //因为是每次都在方法调用时新建， 所以不需要担心单例与否问题
        ReqChain reqChain = new ReqChain();
        reqChain.setMethodName(methodName);
        reqChain.setTime_start(System.currentTimeMillis());
        parent.getUsages().add(reqChain);
        parent.currentUsageIndex = parent.usages.size() - 1;

        return parent.getUsages().get(parent.currentUsageIndex);
    }

    public void finish(){
        time_end = System.currentTimeMillis();
        status = 1;
    }

    public String print(){
        return print(0);
    }

    public String print(int deep){
        List<String> spaceSeparator = new ArrayList<>();
        for (int i = 0; i < deep; i++) {
            spaceSeparator.add("    ");
        }
        StringBuilder builder = new StringBuilder((spaceSeparator.isEmpty() ? "" : String.join( "", spaceSeparator) + "|_") + methodName);
        builder.append("  [耗时"+ (time_end - time_start) + "毫秒]\n");

        int currentDeep = deep + 1;
        spaceSeparator.add("    ");
        for (ReqChain usage : usages) {
            builder.append(String.join( "", spaceSeparator)).append(usage.print(currentDeep));
        }
        return builder.toString();
    }
}
