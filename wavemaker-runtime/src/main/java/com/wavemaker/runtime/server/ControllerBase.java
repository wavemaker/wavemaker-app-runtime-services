/*
 *  Copyright (C) 2012-2013 CloudJee, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.runtime.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import com.wavemaker.common.MessageResource;
import com.wavemaker.common.WMException;
import com.wavemaker.common.WMRuntimeException;
import com.wavemaker.common.util.IOUtils;
import com.wavemaker.json.JSONArray;
import com.wavemaker.json.JSONState;
import com.wavemaker.json.type.FieldDefinition;
import com.wavemaker.json.type.GenericFieldDefinition;
import com.wavemaker.json.type.reflect.MapReflectTypeDefinition;
import com.wavemaker.json.type.reflect.ReflectTypeState;
import com.wavemaker.json.type.reflect.ReflectTypeUtils;
import com.wavemaker.runtime.RuntimeAccess;
import com.wavemaker.runtime.WMAppContext;
import com.wavemaker.runtime.interceptor.RequestInitInterceptor;
import com.wavemaker.runtime.server.view.TypedView;
import com.wavemaker.runtime.service.ParsedServiceArguments;
import com.wavemaker.runtime.service.ServiceManager;
import com.wavemaker.runtime.service.ServiceWire;
import com.wavemaker.runtime.service.TypedServiceReturn;
import com.wavemaker.runtime.service.events.ServiceEventNotifier;
import com.wavemaker.runtime.service.events.ServletEventNotifier;
import com.wavemaker.runtime.service.response.RootServiceResponse;

/**
 * @author Matt Small
 */
public abstract class ControllerBase extends AbstractController {

    /** Logger that is available to subclasses */
    protected final Log logger = LogFactory.getLog(getClass());

    protected ServiceResponse serviceResponse;

    private ServiceManager serviceManager;

    private ServiceEventNotifier serviceEventNotifier;

    private ServletEventNotifier servletEventNotifier;

    @SuppressWarnings("deprecation")
    private com.activegrid.runtime.AGRuntime runtime;

    private InternalRuntime internalRuntime;

    private RuntimeAccess runtimeAccess;

    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) {

        if (request == null) {
            throw new WMRuntimeException(MessageResource.SERVER_NOREQUEST);
        } else if (response == null) {
            throw new WMRuntimeException(MessageResource.SERVER_NORESPONSE);
        }

        ModelAndView ret;
        try {
            this.runtimeAccess.setStartTime(System.currentTimeMillis());
            // add logging
            StringBuilder logEntry = new StringBuilder();
            HttpSession session = request.getSession(false);
            if (session != null) {
                logEntry.append("session " + session.getId() + ", ");
            }
            logEntry.append("thread " + Thread.currentThread().getId());
            NDC.push(logEntry.toString());

            // default responses to the DEFAULT_ENCODING
            response.setCharacterEncoding(ServerConstants.DEFAULT_ENCODING);

            getServletEventNotifier().executeStartRequest();
            initializeRuntime(request, response);

            // execute the request
            ret = executeRequest(request, response);

            getServletEventNotifier().executeEndRequest();
        } catch (Throwable t) {
            this.logger.error(t.getMessage(), t);
            String message = t.getMessage();
            if (!StringUtils.hasLength(message)) {
                message = t.toString();
            }
            if (this.serviceResponse != null && !this.serviceResponse.isPollingRequest() && this.serviceResponse.getConnectionTimeout() > 0
                && System.currentTimeMillis() - this.runtimeAccess.getStartTime() > (this.serviceResponse.getConnectionTimeout() - 3) * 1000) {
                this.serviceResponse.addError(t);
            }
            return handleError(message, t);
        } finally {
            RuntimeAccess.setRuntimeBean(null);
            NDC.pop();
            NDC.remove();
        }

        return ret;
    }

    /**
     * Actually handle the request; control is passed to this from handleRequestInternal.
     * 
     * @param request The current request.
     * @param response The current response.
     * @return An appropriate ModelAndView.
     * @throws IOException
     * @throws WMException
     */
    protected abstract ModelAndView executeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, WMException;

    /**
     * Handle an error, either by returning a ModelAndView, or by throwing an WMRuntimeException.
     * 
     * @param message The error message.
     * @param t The throwable that cause the exception.
     * @return
     */
    protected abstract ModelAndView handleError(String message, Throwable t);

    /**
     * Get a view appropriate for this controller.
     * 
     * @return The view.
     */
    protected abstract View getView();

    /**
     * Return the ModelAndView object, with the key set appropriately for the type of resultObject.
     * 
     * Current behaviour:
     * <ul>
     * <li>If resultObject is an instance of {@link RootServiceResponse}, set resultObject in the Model with a key of
     * {@link ServerConstants#ROOT_MODEL_OBJECT_KEY}.</li>
     * <li>Otherwise, set resultObject into the Model with a key of {@link ServerConstants#RESULTS_PART}.</li>
     * </ul>
     * 
     * @param view The current view.
     * @param typedServiceReturn The result of the method invocation.
     * @return A new ModelAndView object, set up properly depending on the type of resultObject.
     */
    protected ModelAndView getModelAndView(TypedView view, TypedServiceReturn typedServiceReturn) {

        ModelAndView ret;

        FieldDefinition fd;
        if (typedServiceReturn.getReturnType() != null) {
            fd = typedServiceReturn.getReturnType();
        } else {
            fd = new GenericFieldDefinition();
        }

        Object resultObject = typedServiceReturn.getReturnValue();
        if (resultObject != null && resultObject instanceof RootServiceResponse) {
            view.setRootType(fd);
            ret = new ModelAndView(view, ServerConstants.ROOT_MODEL_OBJECT_KEY, resultObject);
        } else {
            MapReflectTypeDefinition mrtd = new MapReflectTypeDefinition();
            mrtd.setKeyFieldDefinition(ReflectTypeUtils.getFieldDefinition(String.class, new ReflectTypeState(), false, null));
            mrtd.setValueFieldDefinition(fd);

            view.setRootType(new GenericFieldDefinition(mrtd));

            ret = new ModelAndView(view, ServerConstants.RESULTS_PART, resultObject);
        }

        return ret;
    }

    protected TypedServiceReturn invokeMethod(ServiceWire sw, String method, JSONArray jsonArgs, Map<String, Object[]> mapParams) throws WMException {
        return invokeMethod(sw, method, jsonArgs, mapParams, null);
    }

    protected TypedServiceReturn invokeMethod(ServiceWire sw, String method, JSONArray jsonArgs, Map<String, Object[]> mapParams,
        ServiceResponse serviceResponse) throws WMException {

        if (jsonArgs != null && mapParams != null) {
            throw new WMRuntimeException(MessageResource.BOTH_ARGUMENT_TYPES, jsonArgs, mapParams);
        } else if (sw == null) {
            throw new NullArgumentException("sw");
        }

        sw.getServiceType().setup(sw, this.internalRuntime, this.runtimeAccess);

        JSONState jsonState = getInternalRuntime().getJSONState();

        ParsedServiceArguments args;
        if (mapParams != null) {
            args = sw.getServiceType().parseServiceArgs(sw, method, mapParams, jsonState);
        } else {
            args = sw.getServiceType().parseServiceArgs(sw, method, jsonArgs, jsonState);
        }

        getInternalRuntime().setDeserializedProperties(args.getGettersCalled());

        return ServerUtils.invokeMethodWithEvents(getServiceEventNotifier(), sw, method, args, jsonState, false, serviceResponse);
    }

    @SuppressWarnings("deprecation")
    private void initializeRuntime(HttpServletRequest request, HttpServletResponse response) {
        RequestInitInterceptor.preHandle(request, response, runtimeAccess, internalRuntime, runtime);
    }

    protected void handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String appContextRoot = WMAppContext.getInstance().getAppContextRoot();
        String requestURI = request.getRequestURI();
        Resource resource = new FileSystemResource(appContextRoot);
        resource = resource.createRelative(requestURI);

        InputStream content = resource.getInputStream();
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(requestURI);
        response.setContentType(contentType);
        try {
            IOUtils.copy(content, response.getOutputStream());
        } finally {
            content.close();
        }
    }

    public void setServiceManager(ServiceManager spm) {
        this.serviceManager = spm;
    }

    public ServiceManager getServiceManager() {
        return this.serviceManager;
    }

    public ServletEventNotifier getServletEventNotifier() {
        return this.servletEventNotifier;
    }

    public void setServletEventNotifier(ServletEventNotifier servletEventNotifier) {
        this.servletEventNotifier = servletEventNotifier;
    }

    public ServiceEventNotifier getServiceEventNotifier() {
        return this.serviceEventNotifier;
    }

    public void setServiceEventNotifier(ServiceEventNotifier serviceEventNotifier) {
        this.serviceEventNotifier = serviceEventNotifier;
    }

    @SuppressWarnings("deprecation")
    public com.activegrid.runtime.AGRuntime getRuntime() {
        return this.runtime;
    }

    @SuppressWarnings("deprecation")
    public void setRuntime(com.activegrid.runtime.AGRuntime runtime) {
        this.runtime = runtime;
    }

    public InternalRuntime getInternalRuntime() {
        return this.internalRuntime;
    }

    public void setInternalRuntime(InternalRuntime internalRuntime) {
        this.internalRuntime = internalRuntime;
    }

    public RuntimeAccess getRuntimeAccess() {
        return this.runtimeAccess;
    }

    public void setRuntimeAccess(RuntimeAccess runtimeAccess) {
        this.runtimeAccess = runtimeAccess;
    }

    public void setServiceResponse(ServiceResponse serviceResponse) {
        this.serviceResponse = serviceResponse;
    }
}
