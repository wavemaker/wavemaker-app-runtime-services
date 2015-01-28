package com.wavemaker.runtime.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.wavemaker.runtime.WMAppContext;
import com.wavemaker.studio.common.InvalidInputException;
import com.wavemaker.studio.common.WMRuntimeException;
import com.wavemaker.studio.common.json.JSONUtils;
import net.sf.jmimemagic.*;

/**
 * @author sunilp
 */
public class WMMultipartUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(WMMultipartUtils.class);

    public static final String WM_DATA_JSON = "wm_data_json";

    public static final String BYTE_ARRAY = "byte[]";

    public static final String BLOB = "Blob";

    public static <T> T toObject(MultipartHttpServletRequest multipartHttpServletRequest, Class<T> instance) {
        return toObject(multipartHttpServletRequest, instance, null);
    }

    public static <T> T toObject(MultipartHttpServletRequest multipartHttpServletRequest, Class<T> instance, String serviceId) {
        T t = null;
        try {
            MultipartFile multipartFile = multipartHttpServletRequest.getFile(WM_DATA_JSON);
            if (multipartFile == null) {
                LOGGER.error("Request does not have wm_data_json multipart data");
                throw new InvalidInputException("Invalid Input : wm_data_json part can not be NULL or Empty");
            }
            t = toObject(multipartFile, instance);
            setMultipartsToObject(multipartHttpServletRequest.getFileMap(), t, serviceId);
        } catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchFieldException | NoSuchMethodException | SQLException e) {
            LOGGER.error("Exception while creating a new Instance with information: {}", t);
            throw new WMRuntimeException("Exception while preparing multipart request");
        }
        return t;
    }

    public static <T> T toObject(MultipartFile multipartFile, Class<T> instance) throws IOException {
        return JSONUtils.toObject(multipartFile.getInputStream(), instance);
    }

    private static <T> T setMultipartsToObject(Map<String, MultipartFile> multiparts, T instance, String serviceId) throws IOException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, SQLException {
        Class aClass = instance.getClass();
        for (String part : multiparts.keySet()) {
            if (!part.equals(WM_DATA_JSON)) {
                Field field = aClass.getDeclaredField(part);
                field.setAccessible(true);
                String methodName = "set" + StringUtils.capitalize(field.getName());
                Method method = aClass.getMethod(methodName, (Class<?>) field.getType());
                MultipartFile multipartFile = multiparts.get(part);
                invokeMethod(instance, multipartFile.getInputStream(), method, field, serviceId);
            }
        }
        return instance;
    }

    private static <T> T invokeMethod(T instance, InputStream inputStream, Method method, Field field, String serviceId) throws IOException, IllegalAccessException, InvocationTargetException, SQLException {
        byte[] byteArray = IOUtils.toByteArray(inputStream);
        if (field.getType().isInstance("")) {
            String content = IOUtils.toString(inputStream);
            method.invoke(instance, content);
        } else if (BYTE_ARRAY.equals(field.getType().getSimpleName())) {
            method.invoke(instance, byteArray);
        } else if (BLOB.equals(field.getType().getSimpleName())) {
            SessionFactory sessionFactory = WMAppContext.getInstance().getSpringBean(serviceId + "SessionFactory");
            Session session = sessionFactory.openSession();
            try {
                session = sessionFactory.openSession();
                Blob blob = Hibernate.getLobCreator(session).createBlob(new ByteArrayInputStream(byteArray), new Long(byteArray.length));
                method.invoke(instance, blob);
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        } else {
            LOGGER.error("Casting multipart " + field.getName() + " to " + field.getType().getSimpleName() + " is not supported");
            throw new WMRuntimeException("Casting multipart " + field.getName() + " to " + field.getType().getSimpleName() + " is not supported");
        }
        return instance;
    }

    /**
     * get a match from a stream of data
     *
     * @param data bytes of data
     * @return Guessed content type of given bytes
     * @throws MagicException Failed to Guess!
     */
    public static String guessContentType(byte[] data) throws MagicException {
        return getMagicMatch(data).getMimeType();
    }

    /**
     * get a match from a stream of data
     *
     * @param data bytes of data
     * @return Guessed extension of given bytes
     * @throws MagicException Failed to Guess!
     */
    public static String guessExtension(byte[] data) throws MagicException {
        return getMagicMatch(data).getExtension();
    }

    /**
     * Generate Http response for a field in any Instance
     *
     * @param instance            any Instance
     * @param fieldName           name of the field
     * @param httpServletRequest  to prepare content type
     * @param httpServletResponse to generate response for the given field
     */
    public static <T> void buildHttpResponseForBlob(T instance, String fieldName, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        try {
            String methodName = "get" + StringUtils.capitalize(fieldName);
            Method method = instance.getClass().getMethod(methodName);
            byte[] bytes = null;
            if (BLOB.equals(method.getReturnType().getSimpleName())) {
                Blob blob = (Blob) method.invoke(instance);
                try {
                    bytes = (blob != null) ? IOUtils.toByteArray(blob.getBinaryStream()) : null;
                } catch (SQLException e) {
                    throw new WMRuntimeException("Failed to cast Blob content ", e);
                }
            } else if (BYTE_ARRAY.equals(method.getReturnType().getSimpleName())) {
                bytes = (byte[]) method.invoke(instance);
            }
            if (bytes == null) {
                throw new WMRuntimeException("Data is empty in column " + fieldName);
            }
            httpServletResponse.setContentType(getMatchingContentType(bytes, httpServletRequest));
            httpServletResponse.setHeader("Content-Disposition", "filename=" + fieldName + new Random().nextInt(99) + ";size=" + bytes.length);
            int contentLength = IOUtils.copy(new ByteArrayInputStream(bytes), httpServletResponse.getOutputStream());
            httpServletResponse.setContentLength(contentLength);
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new WMRuntimeException("Failed to prepare response for fieldName" + fieldName, e);
        }

    }

    /**
     * Guess Content type for the given bytes using Magic apis.
     * If any exception using magic api, then getting content type from request.
     *
     * @param bytes              stream of bytes
     * @param httpServletRequest
     * @return content type for given bytes
     */
    private static String getMatchingContentType(byte[] bytes, HttpServletRequest httpServletRequest) {
        String contentType = null;
        try {
            contentType = WMMultipartUtils.guessContentType(bytes);
        } catch (MagicException e) {
            //do nothing
        }
        if (contentType == null) {
            contentType = httpServletRequest.getContentType();
        }
        return contentType;
    }

    private static MagicMatch getMagicMatch(byte[] data) throws MagicException {
        try {
            return Magic.getMagicMatch(data);
        } catch (MagicParseException | MagicMatchNotFoundException | MagicException e) {
            throw new MagicException("Failed to guess magic match for the given bytes", e);
        }
    }
}
