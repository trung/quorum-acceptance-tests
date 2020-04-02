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

package com.quorum.gauge.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.quorum.gauge.common.config.WalletData;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "quorum")
public class QuorumNetworkProperty {
    private Map<QuorumNode, Node> nodes = new HashMap<>();
    private Map<String, WalletData> wallets = new HashMap<>();
    private OAuth2ServerProperty oauth2Server;
    private SocksProxy socksProxy;
    private String bootEndpoint;

    public SocksProxy getSocksProxy() {
        return socksProxy;
    }

    public void setSocksProxy(SocksProxy socksProxy) {
        this.socksProxy = socksProxy;
    }

    public Map<QuorumNode, Node> getNodes() {
        return nodes;
    }

    public void setNodes(Map<QuorumNode, Node> nodes) {
        this.nodes = nodes;
    }

    public String getBootEndpoint() {
        return bootEndpoint;
    }

    public void setBootEndpoint(String bootEndpoint) {
        this.bootEndpoint = bootEndpoint;
    }

    public Map<String, WalletData> getWallets() {
        return wallets;
    }

    public void setWallets(final Map<String, WalletData> wallets) {
        this.wallets = wallets;
    }

    public Map<String, Node> getNodesAsString() {
        Map<String, Node> converted = new HashMap<>();
        for (Map.Entry<QuorumNode, Node> quorumNodeNodeEntry : nodes.entrySet()) {
            converted.put(quorumNodeNodeEntry.getKey().name(), quorumNodeNodeEntry.getValue());
        }
        return converted;
    }

    public OAuth2ServerProperty getOauth2Server() {
        return oauth2Server;
    }

    public void setOauth2Server(OAuth2ServerProperty oauth2Server) {
        this.oauth2Server = oauth2Server;
    }

    public static class SocksProxy {
        private String host;
        private int port;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }

    public static class Node {
        @JsonProperty("privacy-address")
        private String privacyAddress;
        private Map<String, String> privacyAddressAliases = new LinkedHashMap<>();
        private String url;
        @JsonProperty("third-party-url")
        private String thirdPartyUrl;
        @JsonProperty("validator-address")
        private String validatorAddress;
        @JsonProperty("enode-address")
        private String enode;

        public String getPrivacyAddress() {
            return privacyAddress;
        }

        public void setPrivacyAddress(String privacyAddress) {
            this.privacyAddress = privacyAddress;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getValidatorAddress() {
            return validatorAddress;
        }

        public void setValidatorAddress(String validatorAddress) {
            this.validatorAddress = validatorAddress;
        }

        @Override
        public String toString() {
            final String template = "Node[url: %s, privacy-address: %s, validator-address: %s, enode: %s]";
            return String.format(template, url, privacyAddress, validatorAddress, enode);
        }

        public String getEnode() {
            return enode;
        }

        public void setEnode(String enode) {
            this.enode = enode;
        }

        public String getThirdPartyUrl() {
            return thirdPartyUrl;
        }

        public void setThirdPartyUrl(String thirdPartyUrl) {
            this.thirdPartyUrl = thirdPartyUrl;
        }

        public Map<String, String> getPrivacyAddressAliases() {
            return privacyAddressAliases;
        }

        public void setPrivacyAddressAliases(Map<String, String> privacyAddressAliases) {
            this.privacyAddressAliases = privacyAddressAliases;
        }
    }

    public static class OAuth2ServerProperty {
        private String clientEndpoint;
        private String adminEndpoint;

        public OAuth2ServerProperty() {
        }

        public String getClientEndpoint() {
            return clientEndpoint;
        }

        public void setClientEndpoint(String clientEndpoint) {
            this.clientEndpoint = clientEndpoint;
        }

        public String getAdminEndpoint() {
            return adminEndpoint;
        }

        public void setAdminEndpoint(String adminEndpoint) {
            this.adminEndpoint = adminEndpoint;
        }
    }
}
