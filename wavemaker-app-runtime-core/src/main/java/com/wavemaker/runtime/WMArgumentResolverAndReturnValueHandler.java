package com.wavemaker.runtime;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;

import com.wavemaker.runtime.security.xss.WMRequestResponseBodyMethodProcessor;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

public class WMArgumentResolverAndReturnValueHandler implements InitializingBean {

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerMapping;

    @Override
    public void afterPropertiesSet() throws Exception {
        WMRequestResponseBodyMethodProcessor wmRequestResponseBodyMethodProcessor = new WMRequestResponseBodyMethodProcessor(requestMappingHandlerMapping.getMessageConverters());
        if (XSSSecurityHandler.getInstance().isInputSanitizationEnabled()) {
            enableInputEncoding(wmRequestResponseBodyMethodProcessor);
        }
        if (XSSSecurityHandler.getInstance().isOutputSanitizationEnabled()) {
            enableOutputEncoding(wmRequestResponseBodyMethodProcessor);
        }
    }

    public void enableInputEncoding(WMRequestResponseBodyMethodProcessor wmRequestResponseBodyMethodProcessor) {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerMapping.getArgumentResolvers();
        List<HandlerMethodArgumentResolver> customArgumentResolvers = new ArrayList<>();
        for (HandlerMethodArgumentResolver handler : argumentResolvers) {
            if (handler instanceof RequestResponseBodyMethodProcessor) {
                customArgumentResolvers.add(wmRequestResponseBodyMethodProcessor);
            } else {
                customArgumentResolvers.add(handler);
            }
        }
        requestMappingHandlerMapping.setArgumentResolvers(customArgumentResolvers);
    }

    public void enableOutputEncoding(WMRequestResponseBodyMethodProcessor wmRequestResponseBodyMethodProcessor) {
        List<HandlerMethodReturnValueHandler> returnValueHandlers = requestMappingHandlerMapping.getReturnValueHandlers();
        List<HandlerMethodReturnValueHandler> customReturnValueHandlers = new ArrayList<>();
        for (HandlerMethodReturnValueHandler handler : returnValueHandlers) {
            if (handler instanceof RequestResponseBodyMethodProcessor) {
                customReturnValueHandlers.add(wmRequestResponseBodyMethodProcessor);
            } else {
                customReturnValueHandlers.add(handler);
            }
        }
        requestMappingHandlerMapping.setReturnValueHandlers(customReturnValueHandlers);
    }
}
