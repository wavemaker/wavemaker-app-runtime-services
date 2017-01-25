/**
 * Copyright © 2013 - 2016 WaveMaker, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.runtime.ws;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.activation.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;



import com.sun.xml.ws.developer.JAXWSProperties;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.wavemaker.commons.CommonConstants;
import com.wavemaker.commons.util.JAXBUtils;

import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.xml.XMLSerializer;

/**
 * This class provides helper methods for HTTP binding.
 *
 * @author Frankie Fu
 */
public class HTTPBindingSupport {

    private static final String RESPONSE = "Response";

    public enum HTTPRequestMethod {
        GET, POST, PUT, DELETE
    }

    private static Logger logger = LoggerFactory.getLogger(HTTPBindingSupport.class);

    public static <T extends Object> T getResponseObject(QName serviceQName, QName portQName, String endpointAddress, HTTPRequestMethod method,
                                                         String contentType, Object postData, Class<T> responseType, BindingProperties bindingProperties) throws WebServiceException, MalformedURLException {
        return getResponseObject(serviceQName, portQName, endpointAddress, method, contentType, postData, responseType, bindingProperties, null, null);
    }

    public static <T extends Object> T getResponseObject(QName serviceQName, QName portQName, String endpointAddress, HTTPRequestMethod method,
                                                         String contentType, Object postData, Class<T> responseType, BindingProperties bindingProperties, String partnerName,
                                                         Map<String, Object> headerParams) throws WebServiceException, MalformedURLException {

        String msg = postData == null ? null : postData instanceof String ? (String) postData : convertToXMLString(postData);
        URL serviceUrl = new URL(endpointAddress);
        String serviceName = constructServiceName(serviceUrl) + RESPONSE;
        DataSource postSource = null;
        byte[] bytes = null;
        if (method == HTTPRequestMethod.POST) {
            postSource = createDataSource(contentType, msg);
        }
        DataSource response = getResponse(serviceQName, portQName, endpointAddress, method, postSource, bindingProperties, DataSource.class,
                headerParams);
        String responseContentType = getContentType(response.getContentType());
        InputStream is = null;
        try {
            String responseString = HTTPBindingSupport.convertStreamToString(response.getInputStream());
            if (MediaType.APPLICATION_JSON.toString().equals(responseContentType)) {
                responseString = convertJSONToXML(responseString, serviceName);
            }
            is = new BufferedInputStream(IOUtils.toInputStream(responseString, CommonConstants.UTF8));
            bytes = IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (JSONException e) {
            throw new WebServiceException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                throw new WebServiceException(e);
            }
        }

        return processServiceResponse(bytes, responseType);
    }

    private static <T extends Object> T processServiceResponse(byte[] bytes, Class<T> responseType) throws WebServiceException {
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            if (responseType == Void.class) {
                return null;
            } else if (responseType == String.class) {
                String responseString = HTTPBindingSupport.convertStreamToString(is);
                return responseType.cast(responseString);
            } else {
                Object object = JAXBUtils.unMarshall(JAXBContext.newInstance(responseType), is);
                return responseType.cast(object);
            }
        } catch (IOException e) {
            throw new WebServiceException(e);
        } catch (JAXBException e) {
            throw new WebServiceException(e);
        }
    }

    public static String convertToXMLString(Object o) {
        try {
            JAXBContext context = JAXBContext.newInstance(o.getClass());
            Marshaller marshaller = context.createMarshaller();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            marshaller.marshal(o, os);
            return new String(os.toByteArray());
        } catch (JAXBException e) {
            throw new WebServiceInvocationException(e);
        }
    }

    public static DataSource createDataSource(String contentType, String msg) throws WebServiceException {
        ByteArrayInputStream is = null;
        if (msg != null) {
            try {
                StringBuffer sb = new StringBuffer(msg);
                is = new ByteArrayInputStream(sb.toString().getBytes(CommonConstants.UTF8));
            } catch (UnsupportedEncodingException e) {
                throw new WebServiceException(e);
            }
        }
        return XMLMessage.createDataSource(contentType, is);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Object> T getResponse(QName serviceQName, QName portQName, String endpointAddress, HTTPRequestMethod method,
                                                    T postSource, BindingProperties bindingProperties, Class<T> type, Map<String, Object> headerParams) throws WebServiceException {

        Service service = Service.create(serviceQName);
        URI endpointURI;
        try {
            if (bindingProperties != null) {
                // if BindingProperties had endpointAddress defined, then use
                // it instead of the endpointAddress passed in from arguments.
                String endAddress = bindingProperties.getEndpointAddress();
                if (endAddress != null) {
                    endpointAddress = endAddress;
                }
            }
            endpointURI = new URI(endpointAddress);
        } catch (URISyntaxException e) {
            throw new WebServiceException(e);
        }

        String endpointPath = null;
        String endpointQueryString = null;
        if (endpointURI != null) {
            endpointPath = endpointURI.getRawPath();
            endpointQueryString = endpointURI.getRawQuery();
        }

        service.addPort(portQName, HTTPBinding.HTTP_BINDING, endpointAddress);

        Dispatch<T> d = service.createDispatch(portQName, type, Service.Mode.MESSAGE);

        Map<String, Object> requestContext = d.getRequestContext();
        requestContext.put(MessageContext.HTTP_REQUEST_METHOD, method.toString());
        requestContext.put(MessageContext.QUERY_STRING, endpointQueryString);
        requestContext.put(MessageContext.PATH_INFO, endpointPath);

        Map<String, List<String>> reqHeaders = null;
        if (bindingProperties != null) {
            String httpBasicAuthUsername = bindingProperties.getHttpBasicAuthUsername();
            if (httpBasicAuthUsername != null) {
                requestContext.put(BindingProvider.USERNAME_PROPERTY, httpBasicAuthUsername);
                String httpBasicAuthPassword = bindingProperties.getHttpBasicAuthPassword();
                requestContext.put(BindingProvider.PASSWORD_PROPERTY, httpBasicAuthPassword);
            }

            int connectionTimeout = bindingProperties.getConnectionTimeout();
            requestContext.put(JAXWSProperties.CONNECT_TIMEOUT, Integer.valueOf(connectionTimeout));

            int requestTimeout = bindingProperties.getRequestTimeout();
            requestContext.put(JAXWSProperties.REQUEST_TIMEOUT, Integer.valueOf(requestTimeout));

            Map<String, List<String>> httpHeaders = bindingProperties.getHttpHeaders();
            if (httpHeaders != null && !httpHeaders.isEmpty()) {
                reqHeaders = (Map<String, List<String>>) requestContext.get(MessageContext.HTTP_REQUEST_HEADERS);
                if (reqHeaders == null) {
                    reqHeaders = new HashMap<String, List<String>>();
                    requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
                }
                for (Entry<String, List<String>> entry : httpHeaders.entrySet()) {
                    reqHeaders.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // Parameters to pass in http header
        if (headerParams != null && headerParams.size() > 0) {
            if (null == reqHeaders) {
                reqHeaders = new HashMap<String, List<String>>();
            }
            Set<Entry<String, Object>> entries = headerParams.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                List<String> valList = new ArrayList<String>();
                valList.add((String) entry.getValue());
                reqHeaders.put(entry.getKey(), valList);
                requestContext.put(MessageContext.HTTP_REQUEST_HEADERS, reqHeaders);
            }
        }

        logger.info("Invoking HTTP {} request with URL: {}", method, endpointAddress);
        try {
            T result = d.invoke(postSource);
            return result;
        } catch (Exception e) {
            throw new WebServiceException(e);
        }

    }

    public static String getResponseString(QName serviceQName, QName portQName, String endpointAddress, HTTPRequestMethod method,
                                           DataSource postSource, BindingProperties bindingProperties) throws WebServiceException {
        return getResponseString(serviceQName, portQName, endpointAddress,
                method, postSource, bindingProperties, null);
    }

    public static DataSource getDataSource(QName serviceQName, QName portQName, String endpointAddress, HTTPRequestMethod method,
                                           DataSource postSource, BindingProperties bindingProperties, Map<String, Object> headerParams) throws WebServiceException {
        return getResponse(serviceQName, portQName, endpointAddress, method, postSource, bindingProperties, DataSource.class, headerParams);
    }

    public static String getResponseString(QName serviceQName, QName portQName, String endpointAddress, HTTPRequestMethod method,
                                           DataSource postSource, BindingProperties bindingProperties, Map<String, Object> headerParams) throws WebServiceException {

        DataSource response = getResponse(serviceQName, portQName, endpointAddress, method, postSource, bindingProperties, DataSource.class, headerParams);
        try {
            InputStream inputStream = response.getInputStream();
            return convertStreamToString(inputStream);
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            is.close();
        }
        return sb.toString();
    }

    public static String convertJSONToXML(String json, String rootNode) throws JSONException {
        XMLSerializer serializer = new XMLSerializer();
        serializer.setTypeHintsEnabled(false);
        serializer.setRootName(rootNode);
        JSON jsonString = JSONSerializer.toJSON(json);
        return serializer.write(jsonString);
    }

    public static String constructServiceName(URL url) {
        return getServiceName(url.getHost());
    }

    public static String constructServiceName(URI uri) {
        return getServiceName(uri.getHost());
    }

    private static String getServiceName(String host) {
        int i = host.indexOf('.');
        if (i > -1) {
            String s1 = host.substring(i + 1, host.length());
            int j = s1.indexOf('.');
            String s2 = null;
            if (j > -1) {
                s2 = s1.substring(0, j);
            } else {
                s2 = host.substring(0, i);
            }
            return s2;
        }
        return host;
    }

    public static String getContentType(String type) {
        if (type != null && !type.isEmpty()) {
            int index = type.indexOf(";");
            if (index != -1) {
                return type.substring(0, index);
            }
        }
        return type;
    }
}
