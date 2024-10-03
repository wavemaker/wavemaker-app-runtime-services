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
package com.wavemaker.runtime.security.provider.saml.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.config.Lookup;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.util.SSLUtils;
import com.wavemaker.commons.util.WMIOUtils;

/**
 * Created by arjuns on 15/12/16.
 */
public class FileDownload {

    private boolean secure;

    public FileDownload() {
        this(true);
    }

    public FileDownload(boolean secure) {
        this.secure = secure;
    }

    public File download(String url, File file) {
        HttpGet httpGet = new HttpGet(url);
        File downloadedFile = null;
        CloseableHttpClient closeableHttpClient = null;
        try {
            if (!secure) {
                Lookup<TlsSocketStrategy> tlsSocketStrategy = name -> new DefaultClientTlsStrategy(SSLUtils.getAllTrustedCertificateSSLContext());
                BasicHttpClientConnectionManager connectionManager = BasicHttpClientConnectionManager.create(tlsSocketStrategy);
                closeableHttpClient = HttpClients.custom().setConnectionManager(connectionManager).build();
            } else {
                closeableHttpClient = HttpClients.custom().build();
            }
            downloadedFile = closeableHttpClient.execute(httpGet, new FileResponseHandler(file));
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.failed.to.download.file.from.url"), e, url);
        } finally {
            WMIOUtils.closeSilently(closeableHttpClient);
        }
        return downloadedFile;
    }

    public static class FileResponseHandler implements HttpClientResponseHandler<File> {

        private File file;

        public FileResponseHandler(File file) {
            this.file = file;
        }

        @Override
        public File handleResponse(ClassicHttpResponse response) throws IOException {
            InputStream content = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(content, file);
            return file;
        }
    }
}
