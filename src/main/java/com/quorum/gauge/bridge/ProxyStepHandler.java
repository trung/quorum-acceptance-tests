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

import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.StepValue;
import gauge.messages.Messages;
import gauge.messages.Spec;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ProxyStepHandler {
    private static final Logger logger = LoggerFactory.getLogger(ProxyStepHandler.class);

    @Autowired
    GaugeBridgeRuntime runtime;

    @Around("@annotation(proxyStep) && @annotation(step)")
    public Object handle(ProceedingJoinPoint joinPoint, ProxyStep proxyStep, Step step) throws Throwable {
        for (String stepText : step.value()) {
            StepValue sv = runtime.getStepValue(stepText);
            String actualStepText = String.format(sv.getStepText().replaceAll("\\{\\}", "\"%s\""), joinPoint.getArgs());
            logger.debug("Handling\nactualStepText: {}\nparsedStepText: {}\nparameters: {}", actualStepText, sv.getStepText(), joinPoint.getArgs());
            Messages.ExecuteStepRequest.Builder requestBuilder = Messages.ExecuteStepRequest.newBuilder()
                    .setScenarioFailing(false)
                    .setActualStepText(actualStepText)
                    .setParsedStepText(sv.getStepText());
            for (Object paramValue : joinPoint.getArgs()) {
                requestBuilder.addParameters(Spec.Parameter.newBuilder()
                        .setValue(String.valueOf(paramValue))
                        .build());
            }
            Messages.Message msg = runtime.newMessageBuilder()
                    .setMessageType(Messages.Message.MessageType.ExecuteStep)
                    .setExecuteStepRequest(requestBuilder.build())
                    .build();

            Spec.ProtoExecutionResult result = runtime.executeAndGetStatus(proxyStep.value(), msg);
            logger.debug("Result: {}", result);
            if (result.getFailed()) {
                throw new RuntimeException(result.getErrorMessage());
            }
        }
        return joinPoint.proceed();
    }
}
