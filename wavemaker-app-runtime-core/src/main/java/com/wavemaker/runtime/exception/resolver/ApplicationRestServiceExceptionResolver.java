/*******************************************************************************
 * Copyright (C) 2024-2025 WaveMaker, Inc.
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
package com.wavemaker.runtime.exception.resolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.GenericJDBCException;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.validator.internal.engine.path.NodeImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.hibernate5.HibernateJdbcException;
import org.springframework.orm.hibernate5.HibernateQueryException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.wavemaker.commons.InvalidInputException;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.MessageResourceHolder;
import com.wavemaker.commons.UnAuthorizedResourceAccessException;
import com.wavemaker.commons.WMCommonException;
import com.wavemaker.commons.WMException;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.core.web.rest.ErrorResponse;
import com.wavemaker.commons.core.web.rest.ErrorResponses;
import com.wavemaker.runtime.data.exception.BlobContentNotFoundException;
import com.wavemaker.runtime.data.exception.EntityNotFoundException;
import com.wavemaker.runtime.data.exception.QueryParameterMismatchException;
import com.wavemaker.runtime.security.xss.handler.XSSSecurityHandler;

/**
 * @author sunilp
 */
public class ApplicationRestServiceExceptionResolver extends AbstractHandlerExceptionResolver {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationRestServiceExceptionResolver.class);
    private static final String INPUT_INVALID_MESSAGE = "The input parameters are not valid.";

    @Override
    protected ModelAndView doResolveException(
        HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

        logger.error("Error occurred while serving the request with url {} and method {}", request.getRequestURI(),request.getMethod(), ex);

        // Validator Errors/ Invalid data validated at controller level
        if (ex instanceof MethodArgumentTypeMismatchException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleMethodArgumentTypeMismatchException((MethodArgumentTypeMismatchException) ex);
        } else if (ex instanceof MethodArgumentNotValidException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleMethodArgumentNotValidException((MethodArgumentNotValidException) ex, response);
        } else if (ex instanceof HttpRequestMethodNotSupportedException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleHttpRequestMethodNotSupportedException((HttpRequestMethodNotSupportedException) ex);
        } else if (ex instanceof HttpMessageNotReadableException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleHttpMessageNotReadableException((HttpMessageNotReadableException) ex, response);
        } else if (ex instanceof ConstraintViolationException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleMethodConstraintViolationException((ConstraintViolationException) ex, response);
        } else if (ex instanceof AuthenticationException) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return handleUnAuthorizedException(ex, response);
        } else if (ex instanceof org.hibernate.exception.ConstraintViolationException) {//Hibernate jdbc exceptions
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleRuntimeException((RuntimeException) ex);
        } else if (ex instanceof DataIntegrityViolationException) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handleRuntimeException((DataIntegrityViolationException) ex);
        } else if (ex instanceof GenericJDBCException) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handleRuntimeException((GenericJDBCException) ex);
        } else if (ex instanceof SQLGrammarException) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handleRuntimeException((SQLGrammarException) ex);
        } else if (ex instanceof QueryParameterMismatchException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleRuntimeException((QueryParameterMismatchException) ex);
        } else if (ex instanceof HibernateQueryException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleHibernateQueryException((HibernateQueryException) ex);
        } else if (ex instanceof EntityNotFoundException || ex instanceof BlobContentNotFoundException) {//WM Runtime Exceptions
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return handleWMExceptions((WMRuntimeException) ex, MessageResource.ENTITY_NOT_FOUND);
        } else if (ex instanceof InvalidInputException) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return handleWMExceptions((WMRuntimeException) ex, MessageResource.INVALID_INPUT, ex.getMessage());
        } else if (ex instanceof UnAuthorizedResourceAccessException) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return handleWMExceptions((WMRuntimeException) ex, null, null);
        } else if (ex instanceof WMRuntimeException) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handleWMExceptions((WMRuntimeException) ex, null, null);
        } else if (ex instanceof WMException) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handleWMExceptions((WMException) ex, null, null);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return handleException(ex, response);
        }
    }

    private ModelAndView handleUnAuthorizedException(Exception ex, HttpServletResponse response) {
        String msg = (ex.getMessage() != null) ? ex.getMessage() : "";
        ErrorResponse errorResponse = getErrorResponse(MessageResource.create("com.wavemaker.unAuthorized"), msg);
        return getModelAndView(errorResponse);
    }

    private ModelAndView handleMethodConstraintViolationException(ConstraintViolationException ex, HttpServletResponse response) {
        List<ErrorResponse> errorResponseList = new ArrayList();
        Set<ConstraintViolation<?>> constraintViolations = ex.getConstraintViolations();
        if (constraintViolations != null) {
            for (ConstraintViolation<?> constraintViolation : constraintViolations) {
                String paramName = "";
                Path propertyPath = constraintViolation.getPropertyPath();
                if (propertyPath != null && propertyPath instanceof PathImpl pathImpl) {
                    NodeImpl leafNode = pathImpl.getLeafNode();
                    if (leafNode != null) {
                        paramName = leafNode.getName();
                    }
                }
                errorResponseList.add(getErrorResponse(MessageResource.INVALID_FIELD_VALUE, paramName,
                    constraintViolation.getMessage()));
            }
        }
        return getModelAndView(errorResponseList);
    }

    private ModelAndView handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return getModelAndView(getErrorResponse(MessageResource.INVALID_INPUT, ex.getMessage()));
    }

    private ModelAndView handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return getModelAndView(
            getErrorResponse(MessageResource.INVALID_INPUT, "The input for " + ex.getName() + " is invalid."));
    }

    private ModelAndView handleHibernateJdbcException(HibernateJdbcException ex) {
        // Not using the root cause for now.
        return getModelAndView(getErrorResponse(MessageResource.INVALID_INPUT, INPUT_INVALID_MESSAGE));
    }

    private ModelAndView handleHibernateQueryException(HibernateQueryException ex) {
        // Not using the root cause for now.
        return getModelAndView(getErrorResponse(MessageResource.INVALID_INPUT, INPUT_INVALID_MESSAGE));
    }

    private ModelAndView handleMethodArgumentNotValidException(
        MethodArgumentNotValidException methodArgumentNotValidException, HttpServletResponse response) {
        BindingResult bindingResult = methodArgumentNotValidException.getBindingResult();
        List<ObjectError> allErrors = bindingResult.getAllErrors();
        if (allErrors != null) {
            List<ErrorResponse> errorResponseList = new ArrayList<>(allErrors.size());
            for (ObjectError objectError : allErrors) {
                if (objectError instanceof FieldError fieldError) {
                    errorResponseList
                        .add(getErrorResponse(MessageResource.INVALID_FIELD_VALUE, fieldError.getField(),
                            fieldError.getDefaultMessage()));
                } else {
                    errorResponseList
                        .add(getErrorResponse(MessageResource.INVALID_OBJECT, objectError.getObjectName(),
                            objectError.getDefaultMessage()));
                }
                objectError.getObjectName();
            }
            return getModelAndView(errorResponseList);
        } else {
            return getModelAndView(Collections.emptyList());
        }
    }

    private ModelAndView handleRuntimeException(RuntimeException ex) {
        String msg = ExceptionUtils.getRootCauseMessage(ex);
        ErrorResponse errorResponse = getErrorResponse(MessageResource.UNEXPECTED_ERROR, getDefaultInternalServerErrorMsg(msg));
        return getModelAndView(errorResponse);
    }

    private ModelAndView handleRuntimeException(DataIntegrityViolationException ex) {
        ErrorResponse errorResponse = getErrorResponse(MessageResource.DATA_INTEGRITY_VIOALATION,
            ex.getMostSpecificCause().getMessage());
        return getModelAndView(errorResponse);
    }

    private ModelAndView handleException(Exception ex, HttpServletResponse response) {
        ErrorResponse errorResponse = getErrorResponse(MessageResource.UNEXPECTED_ERROR, getDefaultInternalServerErrorMsg(ex.getMessage()));
        return getModelAndView(errorResponse);
    }

    private ModelAndView handleWMExceptions(WMCommonException wmCommonException, MessageResource defaultMessageResource, Object... defaultArgs) {
        MessageResourceHolder messageResourceHolder = wmCommonException.getMessageResourceHolder();
        MessageResource messageResource = messageResourceHolder.getMessageResource();
        Object[] args;
        if (messageResource != null) {
            args = messageResourceHolder.getArgs();
        } else if (defaultMessageResource != null) {
            messageResource = defaultMessageResource;
            args = defaultArgs;
        } else {
            messageResource = MessageResource.UNEXPECTED_ERROR;
            String message = getDefaultInternalServerErrorMsg(messageResourceHolder.getMessage());
            args = new Object[]{message};
        }
        ErrorResponse errorResponse = getErrorResponse(messageResource, args);
        errorResponse.setMessage(messageResource.getMessageWithPlaceholders());
        return getModelAndView(errorResponse);
    }

    private ModelAndView handleHttpMessageNotReadableException(
        HttpMessageNotReadableException ex,
        HttpServletResponse response) {
        Throwable exCause = ex.getCause();
        ErrorResponse errorResponse = null;
        if (exCause != null) {
            if (exCause instanceof UnrecognizedPropertyException) {
                errorResponse = getErrorResponse(MessageResource.UNRECOGNIZED_FIELD,
                    ((UnrecognizedPropertyException) exCause).getPropertyName());
            } else if (exCause instanceof JsonMappingException) {
                errorResponse = getErrorResponse(MessageResource.INVALID_JSON, exCause.getMessage());
            }
        }
        if (errorResponse == null) {
            errorResponse = getErrorResponse(MessageResource.MESSAGE_NOT_READABLE);
        }
        return getModelAndView(errorResponse);
    }

    private ErrorResponse getErrorResponse(MessageResource messageResource, Object... args) {
        List<String> parameters = new ArrayList<>();
        XSSSecurityHandler xssSecurityHandler = XSSSecurityHandler.getInstance();
        if (args != null) {
            for (Object arg : args) {
                if (arg != null) {
                    parameters.add(xssSecurityHandler.sanitizeOutgoingData(arg.toString()));
                    continue;
                }
                parameters.add(null);
            }
        }
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setMessageKey(messageResource.getMessageKey());
        errorResponse.setMessage(messageResource.getMessageWithPlaceholders());
        errorResponse.setParameters(parameters);
        return errorResponse;
    }

    private ModelAndView getModelAndView(ErrorResponse errorResponse) {
        List<ErrorResponse> errorResponseList = new ArrayList<>(1);
        errorResponseList.add(errorResponse);
        return getModelAndView(errorResponseList);
    }

    private ModelAndView getModelAndView(List<ErrorResponse> errorResponseList) {
        return getModelAndView(new ErrorResponses(errorResponseList));
    }

    private ModelAndView getModelAndView(ErrorResponses errorResponses) {
        ModelAndView modelAndView = new ModelAndView(new MappingJackson2JsonView());
        modelAndView.addObject("errors", errorResponses);
        return modelAndView;
    }

    private String getDefaultInternalServerErrorMsg(String message) {
        return StringUtils.isNotBlank(message) ? message : "Internal Server Error";
    }
}
