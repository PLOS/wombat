package org.ambraproject.wombat.config;

import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.springframework.aop.interceptor.PerformanceMonitorInterceptor;

import java.util.ArrayList;
import java.util.List;

public class CustomPerformanceMonitorInterceptor extends PerformanceMonitorInterceptor {

  @Override
  protected Object invokeUnderTrace(MethodInvocation invocation, Log logger) throws Throwable {

    OSCPortOut sender = new OSCPortOut();
    List<Object> args = new ArrayList<>();
    args.add(3);
    args.add(invocation.getMethod().getDeclaringClass() + invocation.getMethod().getName());
    OSCMessage msg = new OSCMessage("/sayhello", args);
    sender.send(msg);
    return super.invokeUnderTrace(invocation, logger);
  }
}
