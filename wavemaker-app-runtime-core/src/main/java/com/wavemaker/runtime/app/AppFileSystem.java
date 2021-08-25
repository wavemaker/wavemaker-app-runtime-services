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
package com.wavemaker.runtime.app;

import java.io.InputStream;
import java.util.Set;

import javax.servlet.ServletContext;

import org.springframework.web.context.ServletContextAware;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.ResourceNotFoundException;
import com.wavemaker.commons.io.ClassPathFile;
import com.wavemaker.commons.io.File;

/**
 * Provides a virtual files system for accessing resources of the application.
 *
 * @author Kishore Routhu on 21/6/17 3:12 PM.
 */
public class AppFileSystem implements ServletContextAware {

    private ServletContext context;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.context = servletContext;
    }

    public String getAppContextRoot() {
        return this.context.getRealPath("/");
    }

    public File getClassPathFile(String resourcePath) {
        return new ClassPathFile(context.getClassLoader(), resourcePath);
    }

    public InputStream getWebappResource(String resourcePath) {
        InputStream resourceStream = context.getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            throw new ResourceNotFoundException(MessageResource.create("com.wavemaker.runtime.requested.resource.not.found"), resourcePath);
        }
        return resourceStream;
    }

    public Set<String> getWebappI18nLocaleFileNames() {
        Set<String> resourcePaths = context.getResourcePaths("/resources/i18n");
        if (resourcePaths == null) {
            throw new ResourceNotFoundException(MessageResource.create("com.wavemaker.runtime.requested.resource.not.found"));
        }
        return resourcePaths;
    }
}
