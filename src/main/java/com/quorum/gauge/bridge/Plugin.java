/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.quorum.gauge.bridge;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Plugin {
    enum Language {python}

    public static class RunnerInfo {
        public String id;
        public String name;
        public String version;
        public String description;
        public Map<String, List<String>> run;
        public Map<String, List<String>> init;
        public Map<String, String> gaugeVersionSupport;
        public String lspLangId;
    }

    public static RunnerInfo getRunnerInfo(Language language) {
        String pluginJsonPath = Common.getLanguageJSONFilePath(language.name());
        try {
            return new ObjectMapper().readValue(new File(pluginJsonPath), RunnerInfo.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read descriptor for language " + language, e);
        }
    }
}
