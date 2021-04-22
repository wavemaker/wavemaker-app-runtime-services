package com.wavemaker.runtime.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.core.web.rest.ErrorResponse;
import com.wavemaker.commons.core.web.rest.ErrorResponses;
import com.wavemaker.runtime.WMObjectMapper;

import static com.wavemaker.runtime.security.SecurityConstants.APPLICATION_JSON;
import static com.wavemaker.runtime.security.SecurityConstants.X_WM_LOGIN_ERROR_MESSAGE;

public class WMAuthenticationDefaultFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        if (!response.isCommitted()) {
            String exceptionMessage = exception.getMessage();
            Throwable rootCause = ExceptionUtils.getRootCause(exception.getCause());
            StringBuilder msg = new StringBuilder("Authentication Failed");
            if (rootCause instanceof WMRuntimeException) {
                msg.append(":").append(rootCause.getMessage());
            }
            response.setHeader(X_WM_LOGIN_ERROR_MESSAGE, msg.toString());
            Map<String, Object> errorMap = new HashMap(1);
            ErrorResponse errorResponse = new ErrorResponse();
            MessageResource messageResource = MessageResource.create("com.wavemaker.runtime.security.authentication.failed");
            errorResponse.setMessageKey(messageResource.getMessageKey());
            errorResponse.setMessage(messageResource.getMessageWithPlaceholders());
            errorResponse.setParameters(Arrays.asList(exceptionMessage));
            List<ErrorResponse> errorResponseList = new ArrayList<>(1);
            errorResponseList.add(errorResponse);
            errorMap.put("errors", new ErrorResponses(errorResponseList));
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(APPLICATION_JSON);
            response.getWriter().write(WMObjectMapper.getInstance().writeValueAsString(errorMap));
        }
    }
}
