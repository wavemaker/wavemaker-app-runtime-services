package com.wavemaker.runtime.connector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 29/4/20
 */
public class ConnectorAPIInvoker {

    public static final String WMCONNECTOR_SPRING_XML = "wmconnector-spring.xml";

    public ApplicationContext connectorApplicationContext;

    public ConnectorAPIInvoker() {
        connectorApplicationContext = new ClassPathXmlApplicationContext(new String[]{WMCONNECTOR_SPRING_XML});
    }

    public Object invokeMethod(Method method, Object[] args) {
        try {
            Class<?> aClass = Class.forName(method.getDeclaringClass().getName());
            Object bean = connectorApplicationContext.getBean(aClass);
            Method implMethod = aClass.getMethod(method.getName(), method.getParameterTypes());
            return implMethod.invoke(bean, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load connector impl bean", e);
        }
    }

}
