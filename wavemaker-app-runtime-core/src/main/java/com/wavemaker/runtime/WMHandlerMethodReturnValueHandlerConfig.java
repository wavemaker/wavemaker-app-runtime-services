package com.wavemaker.runtime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.wavemaker.runtime.security.xss.WMRequestResponseBodyMethodProcessor;

public class WMHandlerMethodReturnValueHandlerConfig implements InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerMapping;

    @Override
    public void afterPropertiesSet() throws Exception {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerMapping.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> customReturnValueHandlers = new ArrayList<>();
        for(HandlerMethodReturnValueHandler handler : returnValueHandlers) {
            if(handler instanceof RequestResponseBodyMethodProcessor) {
                customReturnValueHandlers.add(new WMRequestResponseBodyMethodProcessor(
                        requestMappingHandlerMapping.getMessageConverters()));
            } else {
                customReturnValueHandlers.add(handler);
            }
        }
        requestMappingHandlerMapping.setReturnValueHandlers(customReturnValueHandlers);
    }
}
