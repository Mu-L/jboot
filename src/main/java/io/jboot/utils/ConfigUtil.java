/**
 * Copyright (c) 2015-2022, Michael Yang 杨福海 (fuhai999@gmail.com).
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
package io.jboot.utils;


import io.jboot.app.config.JbootConfigKit;
import io.jboot.app.config.JbootConfigManager;
import io.jboot.app.config.annotation.ConfigModel;
import org.apache.commons.lang3.StringUtils;

import java.util.*;


/**
 * @author michael yang (fuhai999@gmail.com)
 * @Date: 2020/3/19
 */
public class ConfigUtil {

    public static <T> Map<String, T> getConfigModels(Class<T> tClass) {
        ConfigModel configModel = tClass.getAnnotation(ConfigModel.class);
        if (configModel == null) {
            throw new IllegalStateException("The Class \"" + tClass.getName() + "\" is not have @ConfigModel annotation");
        }
        return getConfigModels(tClass, configModel.prefix());
    }



    public static <T> Map<String, T> getConfigModels(Class<T> tClass, String prefix) {
        Map<String, T> objMap = new HashMap<>();

        boolean initDefault = false;

        Set<String> objNames = new HashSet<>();

        String objPrefix = prefix + ".";
        int pointCount = StringUtils.countMatches(prefix, '.');

        Properties prop = JbootConfigManager.me().getProperties();

        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            if (entry.getKey() == null || JbootConfigKit.isBlank(entry.getKey().toString())) {
                continue;
            }

            String key = entry.getKey().toString().trim();

            //配置来源于 Docker 的环境变量配置
            if (key.contains("_") && Character.isUpperCase(key.charAt(0))) {
                key = key.toLowerCase().replace('_', '.');
            }

            //初始化默认的配置
            if (!initDefault && key.startsWith(prefix) && StringUtils.countMatches(key, '.') == pointCount + 1) {
                initDefault = true;
                T defaultObj = JbootConfigManager.me().get(tClass, prefix, null);
                objMap.put("default", defaultObj);

            }

            if (key.startsWith(objPrefix) && entry.getValue() != null) {
                String[] keySplits = key.split("\\.");
                if (keySplits.length == pointCount + 3) {
                    objNames.add(keySplits[pointCount + 1]);
                }
            }
        }

        for (String name : objNames) {
            T obj = JbootConfigManager.me().get(tClass, objPrefix + name, null);
            objMap.put(name, obj);
        }

        return objMap;
    }
}
