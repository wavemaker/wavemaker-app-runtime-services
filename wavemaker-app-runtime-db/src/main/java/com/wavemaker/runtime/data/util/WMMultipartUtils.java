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
package com.wavemaker.runtime.data.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.wavemaker.commons.InvalidInputException;
import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.WMIOUtils;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.commons.WMObjectMapper;
import com.wavemaker.runtime.commons.file.model.DownloadResponse;
import com.wavemaker.runtime.commons.util.MultipartConstants;
import com.wavemaker.runtime.commons.util.WMRandomUtils;

/**
 * @author sunilp
 */
public class WMMultipartUtils {

    public static final String BLOB = "Blob";
    private static final Logger LOGGER = LoggerFactory.getLogger(WMMultipartUtils.class);
    private static final int FILE_NAME_LENGTH = 12;
    private static final String BYTE_ARRAY = "byte[]";

    public static <T> T toObject(MultipartHttpServletRequest multipartHttpServletRequest, Class<T> instance) {
        return toObject(multipartHttpServletRequest, instance, null);
    }

    public static <T> T toObject(
        MultipartHttpServletRequest multipartHttpServletRequest, Class<T> instance, String serviceId) {
        T t = null;
        try {
            MultipartFile multipartFile = multipartHttpServletRequest.getFile(MultipartConstants.WM_DATA_JSON);
            if (multipartFile == null) {
                final String wmJson = multipartHttpServletRequest.getParameter(MultipartConstants.WM_DATA_JSON);
                if (StringUtils.isBlank(wmJson)) {
                    LOGGER.error("Request does not have wm_data_json multipart data");
                    throw new InvalidInputException(MessageResource.create("com.wavemaker.runtime.request.emptyJson"));
                }
                t = toObject(wmJson, instance);
            } else {
                t = toObject(multipartFile, instance);
            }
            setMultipartsToObject(multipartHttpServletRequest.getFileMap(), t, serviceId);
        } catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchFieldException | NoSuchMethodException e) {
            LOGGER.error("Exception while creating a new Instance with information: {}", t);
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.multipart.request.exception"), e);
        }
        return t;
    }

    public static <T> T toObject(MultipartFile multipartFile, Class<T> instance) throws IOException {
        WMObjectMapper objectMapper = WMObjectMapper.getInstance();
        return objectMapper.readValue(multipartFile.getInputStream(), instance);
    }

    public static <T> T toObject(String json, Class<T> instance) throws IOException {
        WMObjectMapper objectMapper = WMObjectMapper.getInstance();
        return objectMapper.readValue(json, instance);
    }

    /**
     * This Api is used to update blob content from old instance to new instance when blob type content is NULL in the
     * new instance
     *
     * @param oldInstance : persisted instance.
     * @param newInstance : changes in the persisted instance.
     *
     * @return returns newInstance with updated blob content
     */
    public static <T> T updateLobsContent(T oldInstance, T newInstance) {
        Field[] fields = newInstance.getClass().getDeclaredFields();
        for (Field field : fields) {
            String type = field.getType().getSimpleName();
            if (BYTE_ARRAY.equals(type) || BLOB.equals(type)) {
                String getMethodName = "get" + StringUtils.capitalize(field.getName());
                try {
                    Method getMethod = newInstance.getClass().getMethod(getMethodName);
                    Object object = getMethod.invoke(newInstance);
                    if (object == null || (object instanceof byte[] && ((byte[]) object).length == 0)) {
                        String setMethodName = "set" + StringUtils.capitalize(field.getName());
                        Method setMethod = newInstance.getClass().getMethod(setMethodName, field.getType());
                        Object oldObject = getMethod.invoke(oldInstance);
                        setMethod.invoke(newInstance, oldObject);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    LOGGER.error("Failed to update blob content", e);
                    throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.blobContent.updated.failed"), e);
                }
            }
        }
        return newInstance;
    }

    private static <T> T setMultipartsToObject(
        Map<String, MultipartFile> multiparts, T instance,
        String serviceId) throws IOException, NoSuchFieldException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class aClass = instance.getClass();
        for (Map.Entry<String, MultipartFile> part : multiparts.entrySet()) {
            if (!part.getKey().equals(MultipartConstants.WM_DATA_JSON)) {
                Field field = aClass.getDeclaredField(part.getKey());
                ReflectionUtils.makeAccessible(field);
                String methodName = "set" + StringUtils.capitalize(field.getName());
                Method method = aClass.getMethod(methodName, (Class<?>) field.getType());
                invokeMethod(instance, part.getValue().getInputStream(), method, field, serviceId);
            }
        }
        return instance;
    }

    private static <T> T invokeMethod(
        T instance, InputStream inputStream, Method method, Field field,
        String serviceId) throws IOException, IllegalAccessException, InvocationTargetException {
        byte[] byteArray = IOUtils.toByteArray(inputStream);
        if (field.getType().isInstance("")) {
            String content = WMIOUtils.toString(inputStream);
            method.invoke(instance, content);
        } else if (Objects.equals(BYTE_ARRAY, field.getType().getSimpleName())) {
            method.invoke(instance, byteArray);
        } else if (Objects.equals(BLOB, field.getType().getSimpleName())) {
            SessionFactory sessionFactory = WMAppContext.getInstance().getSpringBean(serviceId + "SessionFactory");
            try (Session session = sessionFactory.openSession()) {
                Blob blob = Hibernate.getLobCreator(session)
                    .createBlob(new ByteArrayInputStream(byteArray), byteArray.length);
                method.invoke(instance, blob);
            }
        } else {
            LOGGER.error("Casting multipart {} to {} is not supported", field.getName(),
                field.getType().getSimpleName());
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.multipart.casting.exception"), field.getName(), field.getType()
                .getSimpleName());
        }
        return instance;
    }

    public static byte[] toByteArray(MultipartFile file) {
        try {
            return file == null ? null : file.getBytes();
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.file.reading.error"), e);
        }
    }

    /**
     * get a match from a stream of data
     *
     * @param data bytes of data
     *
     * @return Guessed content type of given bytes
     */
    public static String guessContentType(byte[] data) {
        return new Tika().detect(data);
    }

    /**
     * Generate Http response for a field in any Instance
     *
     * @param instance            any Instance
     * @param fieldName           name of the field
     * @param httpServletRequest  to prepare content type
     * @param httpServletResponse to generate response for the given field
     */
    public static <T> void buildHttpResponseForBlob(
        T instance, String fieldName, HttpServletRequest httpServletRequest,
        HttpServletResponse httpServletResponse) {
        try {
            byte[] bytes = getBlobBytes((T) instance, fieldName);
            httpServletResponse.setContentType(getMatchingContentType(bytes, httpServletRequest));
            httpServletResponse.setHeader("Content-Disposition",
                "filename=" + fieldName + new SecureRandom().nextInt(99) + ";size=" + bytes.length);
            int contentLength = IOUtils.copy(new ByteArrayInputStream(bytes), httpServletResponse.getOutputStream());
            httpServletResponse.setContentLength(contentLength);
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.response.construction.failed"), e, fieldName);
        }

    }

    public static <T> DownloadResponse buildDownloadResponseForBlob(
        T instance, String fieldName, HttpServletRequest httpServletRequest, boolean download) {
        DownloadResponse downloadResponse = new DownloadResponse();
        try {
            byte[] bytes = getBlobBytes(instance, fieldName);

            String contentType = null;
            String filename = httpServletRequest.getParameter("filename");
            if (StringUtils.isBlank(filename)) {
                filename = fieldName + System.currentTimeMillis();
            } else {
                contentType = new Tika().detect(filename);
            }

            if (contentType == null) {
                contentType = getMatchingContentType(bytes, httpServletRequest);
            }
            String fileExtension = getFileExtensionFromContentType(contentType);
            if (StringUtils.isNotBlank(fileExtension) && !filename.endsWith(fileExtension)) {
                filename += fileExtension;
            }

            downloadResponse.setContents(new ByteArrayInputStream(bytes));
            downloadResponse.setContentType(contentType);
            downloadResponse.setFileName(filename);
            downloadResponse.setInline(!download);
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.response.construction.failed"), e, fieldName);
        }
        return downloadResponse;
    }

    private static String getFileExtensionFromContentType(String contentType) {
        try {
            MimeType mimeType = TikaConfig.getDefaultConfig().getMimeRepository().forName(contentType);
            return mimeType.getExtension();
        } catch (Exception e) {
            LOGGER.warn("Could not get file extension for file type {}", contentType);
            return "";
        }
    }

    private static MimeType getMimeTypeFromInputStream(InputStream is) {
        String contentType = null;
        try {
            contentType = new Tika().detect(is);
            return TikaConfig.getDefaultConfig().getMimeRepository().forName(contentType);
        } catch (IOException | MimeTypeException e) {
            LOGGER.warn("Could not get file extension for file type {}", contentType);
            return null;
        }
    }

    public static DownloadResponse buildDownloadResponse(
        HttpServletRequest request, InputStream is, boolean download) {
        DownloadResponse downloadResponse = new DownloadResponse();

        downloadResponse.setContents(is);
        downloadResponse.setInline(!download);

        String contentType = null;
        String filename = request.getParameter("filename");
        String extension = "";

        MimeType mimeType = getMimeTypeFromInputStream(is);
        if (mimeType != null) {
            contentType = mimeType.getName();
            extension = mimeType.getExtension();
        } else if (StringUtils.isNotBlank(filename)) {
            contentType = URLConnection.guessContentTypeFromName(filename);
        }

        if (StringUtils.isBlank(filename)) {
            filename = WMRandomUtils.getRandomString(FILE_NAME_LENGTH);
        }

        if (StringUtils.isBlank(contentType)) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        downloadResponse.setContentType(contentType);
        if (filename.endsWith(extension)) {
            downloadResponse.setFileName(filename);
        } else {
            downloadResponse.setFileName(filename + extension);
        }
        return downloadResponse;
    }

    private static <T> byte[] getBlobBytes(
        final T instance,
        final String fieldName) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
        String methodName = "get" + StringUtils.capitalize(fieldName);
        Method method = instance.getClass().getMethod(methodName);
        byte[] bytes = null;
        if (Objects.equals(BLOB, method.getReturnType().getSimpleName())) {
            Blob blob = (Blob) method.invoke(instance);
            try {
                bytes = (blob != null) ? IOUtils.toByteArray(blob.getBinaryStream()) : null;
            } catch (SQLException e) {
                throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.blobContent.cast.failed"), e);
            }
        } else if (Objects.equals(BYTE_ARRAY, method.getReturnType().getSimpleName())) {
            bytes = (byte[]) method.invoke(instance);
        }
        if (bytes == null) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.no.data.in.column"), fieldName);
        }
        return bytes;
    }

    /**
     * Guess Content type for the given bytes using Tika
     * If any exception using Tika api, then getting content type from request.
     *
     * @param bytes stream of bytes
     *
     * @return content type for given bytes
     */
    private static String getMatchingContentType(byte[] bytes, HttpServletRequest httpServletRequest) {
        String contentType = guessContentType(bytes);
        if (contentType == null) {
            contentType = httpServletRequest.getContentType();
        }
        return contentType;
    }

}
