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

package com.quorum.gauge;

import com.quorum.gauge.bridge.LanguageRunner;
import com.quorum.gauge.bridge.ProxyStep;
import com.quorum.gauge.core.AbstractSpecImplementation;
import com.thoughtworks.gauge.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SampleGaugePython extends AbstractSpecImplementation {
    private static final Logger logger = LoggerFactory.getLogger(SampleGaugePython.class);

    @ProxyStep(LanguageRunner.python)
    @Step("This is a test to call python passing string <s> and integer <i>")
    public void testCallPython(String s, int i) {

    }
}
