/*******************************************************************************
 * Copyright (C) 2022-2023 WaveMaker, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.wavemaker.runtime.data.aop;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.runtime.data.annotations.EntityService;

@Aspect
public class CRUDAspectManager {

    private static final Logger logger = LoggerFactory.getLogger(CRUDAspectManager.class);

    @Autowired
    private List<CRUDMethodInvocationHandler> crudMethodInvocationHandlerList;


    @Around("execution(public * *(..)) && @within(entityService)")
    public Object crudMethodAccessAdvice(
            final ProceedingJoinPoint joinPoint, EntityService entityService) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        String serviceId = entityService.serviceId();
        Class entityClass = entityService.entityClass();
        logger.debug("Inside around for method {}", method.getName());
        Optional<CRUDMethodInvocationHandler> crudMethodInvocationHandlerOptional = findMatchingCRUDMethodInvocationHandler(entityClass, method);
        crudMethodInvocationHandlerOptional.ifPresent(crudMethodInvocationHandler ->  {
            logger.debug("Calling preHandler in handler {}", crudMethodInvocationHandler);
            crudMethodInvocationHandler.preHandle(serviceId, entityClass, method, args);
        });
        logger.debug("Calling target method {}", method.getName());
        Object retVal = joinPoint.proceed(args);
        crudMethodInvocationHandlerOptional.ifPresent(crudMethodInvocationHandler -> {
            logger.debug("Calling postHandler in handler {}", crudMethodInvocationHandler);
            crudMethodInvocationHandler.postHandle(serviceId, entityClass, method, retVal);
        });
        return retVal;
    }

    private Optional<CRUDMethodInvocationHandler> findMatchingCRUDMethodInvocationHandler( Class entityClass, Method method) {
        for (CRUDMethodInvocationHandler crudMethodInvocationHandler : crudMethodInvocationHandlerList) {
            if (crudMethodInvocationHandler.matches(entityClass, method)) {
                return Optional.of(crudMethodInvocationHandler);
            }
        }
        return Optional.empty();
    }
}
