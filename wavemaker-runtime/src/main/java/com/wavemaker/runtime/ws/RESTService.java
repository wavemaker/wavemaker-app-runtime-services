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

package com.wavemaker.runtime.ws;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;

import com.wavemaker.runtime.ws.HTTPBindingSupport.HTTPRequestMethod;
import com.wavemaker.runtime.ws.util.Constants;

/**
 * REST service caller. This class provides interface to call a REST style Web service. This class can be extended and
 * customized to support patrner specific web service requirements.
 * 
 * @author Frankie Fu
 */
public class RESTService {

    protected String serviceId;

    protected QName serviceQName;

    protected String parameterizedURI;

    protected BindingProperties bindingProperties;

    protected HTTPRequestMethod httpRequestMethod;

    /**
     * Constructs a REST style Web service.
     * 
     * @param serviceId The service ID.
     * @param serviceQName The Qualified name of the service in the WSDL service description.
     * @param parameterizedURI The parameterized URI used to call the REST service.
     */
    public RESTService(String serviceId, QName serviceQName, String parameterizedURI) {
        this(serviceId, serviceQName, parameterizedURI, null);
    }

    /**
     * Constucts a REST style Web service.
     * 
     * @param serviceId The service ID.
     * @param serviceQName The Qualified name of the service in the WSDL service description.
     * @param parameterizedURI The parameterized URI used to call the REST service.
     * @param bindingProperties The optional properties for the HTTP binding. For example, this could contain the HTTP
     *        Basic Auth username and password. This param could be null.
     * 
     */
    public RESTService(String serviceId, QName serviceQName, String parameterizedURI, BindingProperties bindingProperties) {
        this.serviceId = serviceId;
        this.serviceQName = serviceQName;
        this.parameterizedURI = parameterizedURI;
        this.bindingProperties = bindingProperties;
    }

    public <T extends Object> T invoke(Map<String, Object> inputs, Class<T> responseType) {
        return invoke(inputs, null, null, null, responseType, null);
    }

    public <T extends Object> T invoke(Map<String, Object> urlParams, Class<T> responseType, Map<String, Object> headerParams) {
        return invoke(urlParams, null, null, null, responseType, headerParams);
    }

    public <T extends Object> T invoke(Map<String, Object> urlParams, String method, String contentType, String endpoint, Class<T> responseType,
        Map<String, Object> headerParams) {
        return invoke(urlParams, method, contentType, endpoint, responseType, null, headerParams);
    }

    /**
     * invoke a REST style web service
     * 
     * @param urlParams the map containing all input parameters <parameter name, value>
     * @param method the http request method (<tt>GET</tt>, <tt>POST</tt>, <tt>PUT</tt>, <tt>DELETE</tt> and etc.)
     * @param contentType the content type of the request body
     * @param endpoint the service endpoint address
     * @param responseType the Class object of the output class
     * @param partnerName the name of the partner
     * @param headerParams the map containing all input parameters to pass in header
     * @return the object of <i>responseType</i>
     */
    public <T extends Object> T invoke(Map<String, Object> urlParams, String method, String contentType, String endpoint, Class<T> responseType,
        String partnerName, Map<String, Object> headerParams) {
        String endpointAddress = null;

        if (endpoint != null) {
            endpointAddress = endpoint;
        } else {
            endpointAddress = this.parameterizedURI;
        }

        Object postData = "";
        if (method != null && method.equals(Constants.HTTP_METHOD_POST)) {
            this.httpRequestMethod = HTTPRequestMethod.POST;
            if (urlParams.size() == 1) {
                for (Object o : urlParams.values()) {
                    postData = o;
                }
            } else if (urlParams.size() > 1) {
                if (contentType.equalsIgnoreCase(Constants.MIME_TYPE_FORM)) {
                    postData = createFormData(urlParams);
                } else {
                    throw new WebServiceInvocationException("REST service call with HTTP POST should not have more than 1 input.");
                }
            }
        } else {
            this.httpRequestMethod = HTTPRequestMethod.GET;
            endpointAddress = parameterize(endpointAddress, urlParams);
        }

        try {
            return HTTPBindingSupport.getResponseObject(this.serviceQName, this.serviceQName, endpointAddress, this.httpRequestMethod, contentType,
                postData, responseType, this.bindingProperties, partnerName, headerParams);
        } catch (WebServiceException e) {
            throw new WebServiceInvocationException(e);
        }
    }

    private String createFormData(Map<String, Object> urlParams) {
        StringBuffer sb = new StringBuffer();
        Set<Entry<String, Object>> entries = urlParams.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            sb.append(entry.getKey());
            sb.append("=");
            if (entry.getValue() != null) {
                sb.append((String) entry.getValue());
            }
            sb.append("&");
        }

        return sb.toString();
    }

    private static String parameterize(String parameterizedURI, Map<String, Object> urlParams) {
        StringBuilder endpointAddress = new StringBuilder(parameterizedURI);
        for (Entry<String, Object> entry : urlParams.entrySet()) {
            String param = entry.getKey();
            int index = endpointAddress.indexOf("{" + param + "}");
            if (index > -1) {
                try {
                    String v = entry.getValue() != null ? entry.getValue().toString() : "";
                    v = URLEncoder.encode(v, "UTF-8");
                    // java.net.URLEncoder.encode() encodes space " " as "+"
                    // instead of "%20".
                    v = v.replaceAll("\\+", "%20");
                    // http://jira.wavemaker.com/browse/WM-3897
                    v = v.replaceAll("%2F", "/");
                    endpointAddress.replace(index, index + param.length() + 2, v);
                } catch (UnsupportedEncodingException e) {
                    throw new WebServiceInvocationException(e);
                }
            }
        }
        return endpointAddress.toString();
    }

    public String getServiceId() {
        return this.serviceId;
    }

    public void setHttpRequestMethod(String method) {
        this.httpRequestMethod = HTTPRequestMethod.valueOf(method);
    }

    /**
     * Returns the binding properties.
     * 
     * @return The bindingProperties.
     */
    public BindingProperties getBindingProperties() {
        return this.bindingProperties;
    }

    /**
     * Sets the binding properties.
     * 
     * @param bindingProperties The bindingProperties to set.
     */
    public void setBindingProperties(BindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

}
