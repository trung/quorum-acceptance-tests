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
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.thoughtworks.gauge.GaugeConstant;
import com.thoughtworks.gauge.StepValue;
import com.thoughtworks.gauge.connection.GaugeConnection;
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

public class Plugin {

    private static final Logger logger = LoggerFactory.getLogger(Plugin.class);

    public enum Language {python}

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

    public static void startRunner(Language language) {
        int newPort = startServer();
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
            processBuilder.environment().put("GAUGE_INTERNAL_PORT", String.valueOf(newPort));
            Process runner = processBuilder
                    .start();
            if (runner.waitFor() != 0) {
                throw new RuntimeException("command run failed");
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to execute command " + cmd, e);
        }
    }

    private static int startServer() {
        // need to start a socket server to accept the initial request from the runner
        try {
            ServerSocket server = new ServerSocket(0);
            logger.debug("Server started on {}", server.getLocalPort());
            new Thread(() -> {
                Socket socket = null;
                try {
                    socket = server.accept();
                    // validate if runner has the corresponding implementation
                    String stepText = "This is a test to call python passing string <s> and integer <i>";
                    Spec.ProtoStepValue protoStepValue = getProtoStepValue(stepText);
                    Messages.Message msg = Messages.Message.newBuilder()
                            .setMessageType(Messages.Message.MessageType.StepValidateRequest)
                            .setMessageId(1)
                            .setStepValidateRequest(Messages.StepValidateRequest.newBuilder()
                                    .setStepText(stepText)
                                    .setNumberOfParameters(protoStepValue.getParametersCount())
                                    .setStepValue(protoStepValue)
                                    .build())
                            .build();
                    socket.getOutputStream().write(toData(msg.toByteArray()));
                    socket.getOutputStream().flush();

                    InputStream inputStream = socket.getInputStream();

//                    while (socket.isConnected()) {
//                        logger.debug("Parsing data ...");
                    MessageLength messageLength = getMessageLength(inputStream);
                    Messages.Message response = Messages.Message.parseFrom(toBytes(messageLength));
                    logger.debug("Received: type={}, valid={}", response.getMessageType(), response.getStepValidateResponse().getIsValid());
                    logger.debug("          errorMessage={}, suggestion={}", response.getStepValidateResponse().getErrorMessage(), response.getStepValidateResponse().getSuggestion());
//                    }

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

    private static Spec.ProtoStepValue getProtoStepValue(String stepText) {
        GaugeConnection conn = new GaugeConnection(Integer.valueOf(System.getenv(GaugeConstant.GAUGE_API_PORT)));
        StepValue sv = conn.getStepValue(stepText);
        return Spec.ProtoStepValue.newBuilder()
                .addAllParameters(sv.getParameters())
                .setParameterizedStepValue(sv.getStepAnnotationText())
                .build();
    }

    private static byte[] toBytes(MessageLength messageLength) throws IOException {
        long messageSize = messageLength.getLength();
        CodedInputStream stream = messageLength.getRemainingStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < messageSize; i++) {
            outputStream.write(stream.readRawByte());
        }

        return outputStream.toByteArray();
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
        return new MessageLength(size, codedInputStream);
    }
}
