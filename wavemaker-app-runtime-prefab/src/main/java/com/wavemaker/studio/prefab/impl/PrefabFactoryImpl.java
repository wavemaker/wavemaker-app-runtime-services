/**
 * Copyright (C) 2014 WaveMaker, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wavemaker.studio.prefab.impl;

import java.io.File;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wavemaker.studio.common.classloader.WMUrlClassLoader;
import com.wavemaker.studio.prefab.core.Prefab;
import com.wavemaker.studio.prefab.core.PrefabFactory;
import com.wavemaker.studio.prefab.util.PrefabUtils;
import com.wavemaker.studio.prefab.util.Utils;

/**
 * {@link PrefabFactory} for creating {@link Prefab}s.
 *
 * @author Dilip Kumar
 */
@Service
public class PrefabFactoryImpl implements PrefabFactory {
    @Autowired
    private PrefabUtils prefabUtils;

    private URL[] getPrefabFiles(final File prefabDir) {
        return Utils.convertToURLS(prefabUtils.getPrefabConfigDirectory(prefabDir),
                prefabUtils.readJarFilesForPrefab(prefabDir));
    }

    @Override
    public Prefab newPrefab(final File prefabDir)
            throws Exception {
        String prefabName = prefabDir.getName();
        WMUrlClassLoader classLoader = new WMUrlClassLoader(getPrefabFiles(prefabDir), prefabName);
        return new Prefab(prefabName, classLoader);
    }
}