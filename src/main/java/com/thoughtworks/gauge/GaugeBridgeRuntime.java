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

package com.thoughtworks.gauge;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.quorum.gauge.bridge.Common;
import com.quorum.gauge.bridge.ExtendedStepsScanner;
import com.quorum.gauge.bridge.LanguageRunner;
import com.thoughtworks.gauge.connection.GaugeConnection;
import com.thoughtworks.gauge.scan.ClasspathScanner;
import gauge.messages.Messages;
import gauge.messages.Spec;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

/**
 * Similar to {@link com.thoughtworks.gauge.GaugeRuntime}, only applied to additional language runners
 *
 * @see com.thoughtworks.gauge.GaugeRuntime
 */
public class GaugeBridgeRuntime {
    private static final Logger logger = LoggerFactory.getLogger(GaugeBridgeRuntime.class);
    private GaugeConnection connection;

    public GaugeBridgeRuntime() {
        this.connection = new GaugeConnection(readEnvVar(GaugeConstant.GAUGE_API_PORT));
    }

    public boolean validate() {
        logger.info("Scanning proxy steps");
        ClasspathScanner classpathScanner = new ClasspathScanner();
        ExtendedStepsScanner stepsScanner = new ExtendedStepsScanner();
        classpathScanner.scan(stepsScanner);
        for (LanguageRunner lr : stepsScanner.getLanguageRunners()) {
            logger.info("Validation proxy steps against language {}", lr);
            if (!validateSteps(lr, stepsScanner.getStepNames(lr))) {
                return false;
            }
        }
        return true;
    }

    private boolean validateSteps(LanguageRunner lr, List<String> stepNames) {
        if (stepNames.size() == 0) {
            return true;
        }
        int port = startServer(lr, (socket) -> {
            int msgId = 1;
            for (String step : stepNames) {
                StepValue sv = connection.getStepValue(step);
                Spec.ProtoStepValue protoStepValue = Spec.ProtoStepValue.newBuilder()
                        .addAllParameters(sv.getParameters())
                        .setParameterizedStepValue(sv.getStepAnnotationText())
                        .setStepValue(sv.getStepText())
                        .build();
                Messages.Message msg = Messages.Message.newBuilder()
                        .setMessageType(Messages.Message.MessageType.StepValidateRequest)
                        .setMessageId(msgId++)
                        .setStepValidateRequest(Messages.StepValidateRequest.newBuilder()
                                .setStepText(protoStepValue.getStepValue())
                                .setStepValue(protoStepValue)
                                .build())
                        .build();
                socket.getOutputStream().write(toData(msg.toByteArray()));
                socket.getOutputStream().flush();
            }

            InputStream inputStream = socket.getInputStream();
            while (socket.isConnected()) {
                MessageLength messageLength = getMessageLength(inputStream);
                Messages.Message response = Messages.Message.parseFrom(toBytes(messageLength));
                if (response.getMessageType() != Messages.Message.MessageType.StepValidateResponse) {
                    throw new RuntimeException(String.format("invalid response. expected %s but got %s", Messages.Message.MessageType.StepValidateResponse, response.getMessageType()));
                }
                Messages.StepValidateResponse stepValidateResponse = response.getStepValidateResponse();
                if (!stepValidateResponse.getIsValid()) {
                    logger.error("Msg: {}, Suggestion: {}", stepValidateResponse.getErrorMessage(), stepValidateResponse.getSuggestion());
                    return false;
                }
            }
            return true;
        });
        new Thread(() -> startRunner(lr, port)).start();
        return true;
    }

    private int readEnvVar(String env) {
        String port = System.getenv(env);
        if (port == null || port.equalsIgnoreCase("")) {
            throw new RuntimeException(env + " not set");
        }
        return Integer.parseInt(port);
    }

    interface ValidateFunc {
        boolean validate(Socket socket) throws IOException;
    }

    static class RunnerInfo {
        public String id;
        public String name;
        public String version;
        public String description;
        public Map<String, List<String>> run;
        public Map<String, List<String>> init;
        public Map<String, String> gaugeVersionSupport;
        public String lspLangId;
    }

    public static RunnerInfo getRunnerInfo(LanguageRunner language) {
        String pluginJsonPath = Common.getLanguageJSONFilePath(language.name());
        try {
            return new ObjectMapper().readValue(new File(pluginJsonPath), RunnerInfo.class);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read descriptor for language " + language, e);
        }
    }

    private void startRunner(LanguageRunner language, int internalPort) {
        RunnerInfo info = getRunnerInfo(language);
        List<String> cmd = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            cmd = info.run.get("windows");
        } else if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_MAC_OSX) {
            cmd = info.run.get("darwin");
        } else if (SystemUtils.IS_OS_LINUX) {
            cmd = info.run.get("linux");
        }
        if (cmd == null) {
            throw new RuntimeException("No command found for the OS");
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder()
                    .command(cmd)
                    .directory(new File(Common.getLanguageJSONFilePath(language.name())).getParentFile())
                    .inheritIO();
            processBuilder.environment().put("GAUGE_INTERNAL_PORT", String.valueOf(internalPort));
            Process runner = processBuilder
                    .start();
            if (runner.waitFor() != 0) {
                throw new RuntimeException("command run failed");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute command " + cmd, e);
        }
    }

    private int startServer(LanguageRunner lr, ValidateFunc func) {
        // need to start a socket server to accept the initial request from the runner
        try {
            ServerSocket server = new ServerSocket(0);
            logger.debug("Internal Server for language {} started on {}", lr, server.getLocalPort());
            new Thread(() -> {
                Socket socket = null;
                try {
                    socket = server.accept();
                    func.validate(socket);
                } catch (Exception e) {
                    throw new RuntimeException("reading socket error", e);
                } finally {
                    try {
                        if (socket != null) {
                            socket.close();
                        }
                    } catch (IOException e) {
                        logger.warn("closing socket error: {}", e.getMessage());
                    }
                }
            }).start();
            return server.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("starting socket server error", e);
        }
    }

    private static byte[] toData(byte[] bytes) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CodedOutputStream cos = CodedOutputStream.newInstance(stream);
        cos.writeUInt64NoTag(bytes.length);
        cos.flush();
        stream.write(bytes);
        stream.close();

        return stream.toByteArray();
    }

    private static byte[] toBytes(MessageLength messageLength) throws IOException {
        long messageSize = messageLength.getLength();
        CodedInputStream stream = messageLength.getRemainingStream();
        return stream.readRawBytes((int) messageSize);
    }

    static class MessageLength {
        private long length;
        private CodedInputStream remainingStream;

        MessageLength(long length, CodedInputStream remainingStream) {
            this.length = length;
            this.remainingStream = remainingStream;
        }

        public long getLength() {
            return length;
        }

        public CodedInputStream getRemainingStream() {
            return remainingStream;
        }
    }

    private static MessageLength getMessageLength(InputStream is) throws IOException {
        CodedInputStream codedInputStream = CodedInputStream.newInstance(is);
        long size = codedInputStream.readRawVarint64();
        logger.debug("Message length: {}", size);
        return new MessageLength(size, codedInputStream);
    }
}