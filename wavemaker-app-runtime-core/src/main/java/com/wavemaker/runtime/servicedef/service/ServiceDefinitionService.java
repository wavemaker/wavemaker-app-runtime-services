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
package com.wavemaker.runtime.servicedef.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.stereotype.Service;

import com.wavemaker.commons.MessageResource;
import com.wavemaker.commons.WMRuntimeException;
import com.wavemaker.commons.auth.oauth2.OAuth2ProviderConfig;
import com.wavemaker.commons.servicedef.model.ServiceDefinition;
import com.wavemaker.commons.util.EncodeUtils;
import com.wavemaker.runtime.auth.oauth2.OAuthProvidersManager;
import com.wavemaker.runtime.commons.WMAppContext;
import com.wavemaker.runtime.commons.util.PropertyPlaceHolderReplacementHelper;
import com.wavemaker.runtime.prefab.config.PrefabsConfig;
import com.wavemaker.runtime.prefab.core.Prefab;
import com.wavemaker.runtime.prefab.core.PrefabInstaller;
import com.wavemaker.runtime.prefab.core.PrefabManager;
import com.wavemaker.runtime.prefab.core.PrefabRegistry;
import com.wavemaker.runtime.prefab.event.PrefabsLoadedEvent;
import com.wavemaker.runtime.security.SecurityService;
import com.wavemaker.runtime.servicedef.helper.ServiceDefinitionHelper;
import com.wavemaker.runtime.servicedef.model.ServiceDefinitionsWrapper;

/**
 * @author <a href="mailto:sunil.pulugula@wavemaker.com">Sunil Kumar</a>
 * @since 1/4/16
 */
@Service
public class ServiceDefinitionService implements ApplicationListener<PrefabsLoadedEvent> {

    public static final String SERVICE_DEF_RESOURCE_POST_FIX = "-service-definitions.json";
    public static final String SERVICE_DEF_LOCATION_PATTERN = "/servicedefs/**" + SERVICE_DEF_RESOURCE_POST_FIX;
    public static final String PREFAB_SERVICE_DEF_LOCATION_PATTERN = "/prefab-servicedefs/**" + SERVICE_DEF_RESOURCE_POST_FIX;

    private ServiceDefinitionHelper serviceDefinitionHelper = new ServiceDefinitionHelper();

    private MultiValuedMap<String, ServiceDefinition> authExpressionVsServiceDefinitions;
    private Map<String, Map<String, ServiceDefinition>> prefabServiceDefinitionsCache = new HashMap<>();
    private Map<String, ServiceDefinition> baseServiceDefinitions;

    private Map<String, Map<String, OAuth2ProviderConfig>> securityDefinitions;
    private Map<String, Map<String, Map<String, OAuth2ProviderConfig>>> prefabSecurityDefinitions = new HashMap<>();

    @Autowired
    private PrefabManager prefabManager;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private PropertyPlaceHolderReplacementHelper propertyPlaceHolderReplacementHelper;

    @Autowired
    private PrefabRegistry prefabRegistry;

    @Autowired
    private OAuthProvidersManager oAuthProvidersManager;

    @Autowired
    private PropertyResolver propertyResolver;

    @Autowired
    private PrefabsConfig prefabsConfig;

    private static final Logger logger = LoggerFactory.getLogger(ServiceDefinitionService.class);

    @Override
    public void onApplicationEvent(final PrefabsLoadedEvent event) {
        loadServiceDefinitions();
        if (!prefabsConfig.isPrefabsLazyLoad()) {
            loadPrefabsServiceDefinitions();
        }
    }

    public ServiceDefinitionsWrapper getServiceDefinitionWrapper() {
        ServiceDefinitionsWrapper serviceDefinitionsWrapper = new ServiceDefinitionsWrapper();
        serviceDefinitionsWrapper.setSecurityDefinitions(securityDefinitions);
        serviceDefinitionsWrapper.setServiceDefs(listServiceDefs());
        return serviceDefinitionsWrapper;
    }

    public Map<String, ServiceDefinition> listServiceDefs() {
        if (securityService.isSecurityEnabled() && securityService.isAuthenticated()) {
            Map<String, ServiceDefinition> serviceDefinitionsMap = new HashMap<>(baseServiceDefinitions);
            putElements(authExpressionVsServiceDefinitions.get("isAuthenticated()"), serviceDefinitionsMap, false);
            String[] userRoles = securityService.getUserRoles();
            for (String role : userRoles) {
                putElements(authExpressionVsServiceDefinitions.get("ROLE_" + role), serviceDefinitionsMap, false);
            }
            return serviceDefinitionsMap;
        } else {
            return baseServiceDefinitions;
        }
    }

    public ServiceDefinitionsWrapper getServiceDefinitionWrapperForPrefab(String prefabName) {
        ServiceDefinitionsWrapper serviceDefinitionsWrapper = new ServiceDefinitionsWrapper();
        Map<String, ServiceDefinition> serviceDefinitionMap = listPrefabServiceDefinitions(prefabName);
        serviceDefinitionsWrapper.setServiceDefs(serviceDefinitionMap);
        serviceDefinitionsWrapper.setSecurityDefinitions(prefabSecurityDefinitions.get(prefabName));
        return serviceDefinitionsWrapper;
    }

    public Map<String, ServiceDefinition> listPrefabServiceDefinitions(final String prefabName) {
        Map<String, ServiceDefinition> serviceDefinitionMap = prefabServiceDefinitionsCache.get(prefabName);
        if (prefabsConfig.isPrefabsLazyLoad() && serviceDefinitionMap == null) {
            Prefab prefab = prefabManager.getPrefab(prefabName);
            if (prefab != null && !prefab.isInstalled()) {
                synchronized (prefab) {
                    WMAppContext.getInstance().getSpringBean(PrefabInstaller.class).installPrefab(prefab);
                    runInPrefabClassLoader(prefab, () -> loadPrefabServiceDefsAndSecurityDefinitions(prefab, prefabServiceDefinitionsCache, prefabSecurityDefinitions));
                    serviceDefinitionMap = prefabServiceDefinitionsCache.get(prefabName);
                }
            }
        }
        if (serviceDefinitionMap == null) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.invalid.prefab.name"), prefabName);
        }
        return serviceDefinitionMap;
    }

    private void loadServiceDefinitions() {
        Map<String, ServiceDefinition> serviceDefinitionsCache = new HashMap<>();
        Resource[] resources = getServiceDefResources(false);
        if (resources != null) {
            for (Resource resource : resources) {
                serviceDefinitionsCache.putAll(getServiceDefinition(resource, propertyResolver));
            }
        } else {
            logger.warn("Service def resources does not exist for this project");
        }
        this.authExpressionVsServiceDefinitions = constructAuthVsServiceDefinitions(serviceDefinitionsCache);
        this.baseServiceDefinitions = new HashMap<>();
        putElements(authExpressionVsServiceDefinitions.get("permitAll"), baseServiceDefinitions, false);
        putElements(authExpressionVsServiceDefinitions.get("isAuthenticated()"), baseServiceDefinitions, true);
        Set<String> authExpressions = authExpressionVsServiceDefinitions.keySet();
        authExpressions.stream().filter(s -> s.startsWith("ROLE_")).forEach(s ->
                putElements(authExpressionVsServiceDefinitions.get(s), baseServiceDefinitions, true));
        securityDefinitions = oAuthProvidersManager.getOAuth2ProviderWithImplicitFlow();
    }

    //TODO find a better way to fix it, read intercept urls from json instead of from spring xml
    private MultiValuedMap<String, ServiceDefinition> constructAuthVsServiceDefinitions(Map<String, ServiceDefinition> serviceDefinitions) {
        MultiValuedMap<String, ServiceDefinition> authExpressionVsServiceDefinitions = new ArrayListValuedHashMap<>();
        if (securityService.isSecurityEnabled()) {
            FilterSecurityInterceptor filterSecurityInterceptor = WMAppContext.getInstance().getSpringBean(FilterSecurityInterceptor.class);
            FilterInvocationSecurityMetadataSource securityMetadataSource = filterSecurityInterceptor.getSecurityMetadataSource();
            for (ServiceDefinition serviceDefinition : serviceDefinitions.values()) {
                String path = serviceDefinition.getWmServiceOperationInfo().getRelativePath();
                String method = serviceDefinition.getWmServiceOperationInfo().getHttpMethod();
                method = StringUtils.upperCase(method);
                Collection<ConfigAttribute> attributes = securityMetadataSource.getAttributes(new FilterInvocation(null, "/services", path, null,
                        method));
                List<ConfigAttribute> configAttributeList;
                if (attributes instanceof List) {
                    configAttributeList = (List) attributes;
                } else {
                    configAttributeList = new ArrayList<>(attributes);
                }
                if (configAttributeList.size() == 1) {
                    ConfigAttribute configAttribute = configAttributeList.get(0);
                    if (configAttribute != null) {
                        String attribute = configAttribute.toString().trim();
                        if (attribute.startsWith("hasAnyRole(")) {
                            String rolesString = attribute.substring("hasAnyRole(".length(), attribute.length() - 1);
                            String[] roles = rolesString.split(",");
                            for (String role : roles) {
                                role = role.trim();
                                role = role.substring(1, role.length() - 1);
                                authExpressionVsServiceDefinitions.put(EncodeUtils.decode(role), serviceDefinition);
                            }
                        } else {
                            authExpressionVsServiceDefinitions.put(attribute, serviceDefinition);
                        }
                    }
                }
            }
        } else {
            for (ServiceDefinition serviceDefinition : serviceDefinitions.values()) {
                authExpressionVsServiceDefinitions.put("permitAll", serviceDefinition);
            }
        }
        return authExpressionVsServiceDefinitions;
    }

    private void loadPrefabsServiceDefinitions() {
        final Map<String, Map<String, ServiceDefinition>> prefabServiceDefinitionsCache = new HashMap<>();
        final Map<String, Map<String, Map<String, OAuth2ProviderConfig>>> prefabSecurityDefinitions = new HashMap<>();
        for (final Prefab prefab : prefabManager.getPrefabs()) {
            runInPrefabClassLoader(prefab, () -> loadPrefabServiceDefsAndSecurityDefinitions(prefab, prefabServiceDefinitionsCache, prefabSecurityDefinitions));
        }
        this.prefabServiceDefinitionsCache = prefabServiceDefinitionsCache;
        this.prefabSecurityDefinitions = prefabSecurityDefinitions;
    }

    private synchronized void loadPrefabServiceDefsAndSecurityDefinitions(final Prefab prefab, Map<String, Map<String, ServiceDefinition>> prefabServiceDefinitionsCache,
                                                                          Map<String, Map<String, Map<String, OAuth2ProviderConfig>>> prefabSecurityDefinitions) {
        if (prefabServiceDefinitionsCache.get(prefab.getName()) == null) {
            prefabServiceDefinitionsCache.put(prefab.getName(), new HashMap<>());
        }
        Resource[] resources = getServiceDefResources(true);
        if (resources != null) {
            ConfigurableApplicationContext prefabContext = prefabRegistry.getPrefabContext(prefab.getName());
            for (Resource resource : resources) {
                prefabServiceDefinitionsCache.get(prefab.getName()).putAll(getServiceDefinition(resource, prefabContext.getEnvironment()));
            }
            prefabSecurityDefinitions.put(prefab.getName(), oAuthProvidersManager.getOAuth2ProviderWithImplicitFlow());
        } else {
            logger.warn("Service def resources does not exist for this project");
        }
    }

    private Map<String, ServiceDefinition> getServiceDefinition(Resource resource, PropertyResolver propertyResolver) {
        try {
            Reader reader = propertyPlaceHolderReplacementHelper.getPropertyReplaceReader(resource.getInputStream(),
                    propertyResolver);
            return serviceDefinitionHelper.build(reader);
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.service.definition.generation.failure"), e, resource.getFilename());
        }
    }


    private void runInPrefabClassLoader(final Prefab prefab, Runnable runnable) {
        ClassLoader classLoader = prefab.getClassLoader();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            runnable.run();
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private Resource[] getServiceDefResources(boolean isPrefab) {
        try {
            PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
            if (isPrefab) {
                return patternResolver.getResources(PREFAB_SERVICE_DEF_LOCATION_PATTERN);
            }
            return patternResolver.getResources(SERVICE_DEF_LOCATION_PATTERN);
        } catch (FileNotFoundException e) {
            //do nothing
            return new Resource[0];
        } catch (IOException e) {
            throw new WMRuntimeException(MessageResource.create("com.wavemaker.runtime.service.definition.files.not.found"), e);
        }
    }

    private void putElements(Collection<ServiceDefinition> serviceDefinitions, Map<String, ServiceDefinition> serviceDefinitionsMap, boolean valueLess) {
        if (serviceDefinitions != null) {
            for (ServiceDefinition serviceDefinition : serviceDefinitions) {
                if (valueLess) {
                    serviceDefinitionsMap.put(serviceDefinition.getId(), buildValueLessServiceDef(serviceDefinition));
                } else {
                    serviceDefinitionsMap.put(serviceDefinition.getId(), serviceDefinition);
                }
            }
        }
    }

    private ServiceDefinition buildValueLessServiceDef(ServiceDefinition serviceDefinition) {
        return ServiceDefinition.getNewInstance()
                .addId(serviceDefinition.getId())
                .addController(serviceDefinition.getController())
                .addType(serviceDefinition.getType())
                .addCrudOperationId(serviceDefinition.getCrudOperationId())
                .addOperationType(serviceDefinition.getOperationType())
                .addService(serviceDefinition.getService())
                .addWmServiceOperationInfo(null);
    }
}