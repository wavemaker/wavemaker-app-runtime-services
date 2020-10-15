/**
 * Copyright (C) 2020 WaveMaker, Inc.
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
package com.wavemaker.runtime.security.provider.saml.metadata;

import java.io.File;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.opensaml.saml2.metadata.provider.AbstractMetadataProvider;
import org.opensaml.saml2.metadata.provider.FileBackedHTTPMetadataProvider;
import org.opensaml.saml2.metadata.provider.FilesystemMetadataProvider;
import org.opensaml.saml2.metadata.provider.MetadataProviderException;
import org.opensaml.xml.parse.ParserPool;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.wavemaker.commons.WMRuntimeInitException;
import com.wavemaker.commons.model.security.saml.MetadataSource;
import com.wavemaker.runtime.security.provider.saml.SAMLConfig;
import com.wavemaker.runtime.security.provider.saml.SAMLConstants;

/**
 * Created by arjuns on 24/3/17.
 */
public class WMMetadataProviderFactory implements FactoryBean<AbstractMetadataProvider> {

    @Autowired
    private SAMLConfig samlConfig;

    @Autowired
    private ParserPool parserPool;

    @Override
    public AbstractMetadataProvider getObject() throws Exception {
        try {
            MetadataSource metadataSource = samlConfig.getMetadataSource();
            checkInput(metadataSource.name(), "MetadataSource invalid.");
            if (MetadataSource.URL == metadataSource) {
                String idpMetadataUrl = samlConfig.getIdpMetadataUrl();
                checkInput(idpMetadataUrl, "Url is invalid.");
                FileBackedHTTPMetadataProvider fileBackedHTTPMetadataProvider = new FileBackedHTTPMetadataProvider(idpMetadataUrl, 15000,
                        getFile(SAMLConstants.SAML_IDP_METADATA_BACKUP_LOCATION).getAbsolutePath());
                fileBackedHTTPMetadataProvider.setParserPool(parserPool);
                return fileBackedHTTPMetadataProvider;
            } else {
                if (MetadataSource.FILE == metadataSource) {
                    String idpMetadataFileLocation = samlConfig.getIdpMetadataFileLocation();
                    checkInput(idpMetadataFileLocation, "File is invalid");
                    FilesystemMetadataProvider filesystemMetadataProvider = new FilesystemMetadataProvider(
                            getFile(idpMetadataFileLocation));
                    filesystemMetadataProvider.setParserPool(parserPool);
                    return filesystemMetadataProvider;
                }
            }
        } catch (MetadataProviderException | URISyntaxException e) {
            throw new WMRuntimeInitException("Failed to create MetadataProvider bean", e.getMessage(), e);
        }

        return null;
    }

    private void checkInput(final String input, String errorMessage) {
        if (StringUtils.isBlank(input)) {
            throw new WMRuntimeInitException("Failed to create MetadataProvider bean. " + errorMessage);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return AbstractMetadataProvider.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    private File getFile(String path) throws URISyntaxException {
        return new File(WMMetadataProviderFactory.class.getResource(path).toURI());
    }
}
