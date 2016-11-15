package com.wavemaker.runtime.data.transform;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.transform.ResultTransformer;

/**
 * @author <a href="mailto:dilip.gundu@wavemaker.com">Dilip Kumar</a>
 * @since 15/11/16
 */
public class Transformers {

    private final Map<Class<?>, ResultTransformer> transformerMap;

    private Transformers() {
        transformerMap = new HashMap<>();

        transformerMap.put(Object.class, AliasToEntityLinkedHashMapTransformer.INSTANCE);
        transformerMap.put(Map.class, AliasToEntityLinkedHashMapTransformer.INSTANCE);
    }

    private static class TransformersHolder {
        private static final Transformers INSTANCE = new Transformers();
    }

    public ResultTransformer get(Class<?> type) {
        if (!transformerMap.containsKey(type)) {
            synchronized (transformerMap) {
                if (!transformerMap.containsKey(type)) {
                    transformerMap.put(type, new AliasToMappedClassResultTransformer(type));
                }
            }
        }
        return transformerMap.get(type);
    }

    public static ResultTransformer aliasToMappedClass(Class<?> type) {
        return TransformersHolder.INSTANCE.get(type);
    }


}
