package com.quorum.gauge;

import com.quorum.gauge.common.Context;
import com.quorum.gauge.common.QuorumNetworkProperty;
import com.quorum.gauge.core.AbstractSpecImplementation;
import com.quorum.gauge.ext.ObjectResponse;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.Response;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Service
public class PluginSecurity extends AbstractSpecImplementation {

    private static final Logger logger = LoggerFactory.getLogger(PluginSecurity.class);

    @Step("Configure the authorization server to grant `<clientId>` access to scopes `<scopes>` in `<nodes>`")
    public void configure(String clientId, String scopes, String nodes) {
        List<String> nodeList = Arrays.stream(nodes.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        List<String> scopeList = Arrays.stream(scopes.split(","))
                .map(StringUtils::trim)
                .collect(Collectors.toList());
        assertThat(oAuth2Service.updateOrInsert(clientId, scopeList, nodeList).blockingFirst())
                .as("Configuring must be successful").isEqualTo(true);
    }

    @Step("`<clientId>` is responded with <policy> when trying to: <table>")
    public void invokeMultiple(String clientId, String policy, Table table) {
        boolean expectAuthorized = "success".equalsIgnoreCase(policy);
        String token = mustHaveValue(DataStoreFactory.getScenarioDataStore(), clientId, String.class);
        Context.storeAccessToken(token);
        Map<String, QuorumNetworkProperty.Node> nodeMap = networkProperty.getNodesAsString();
        table.getTableRows().stream()
                .map(r -> new ApiCall(r.getCell("callApi"), r.getCell("targetNode")))
                .onClose(Context::removeAccessToken)
                .forEach(a -> {
                    rpcService.call(nodeMap.get(a.node), a.name, Collections.emptyList(), ObjectResponse.class)
                            .blockingForEach(res -> {
                                String description = policy + ": " + a.name + "@" + a.node;
                                if (expectAuthorized) {
                                    assertThat(Optional.ofNullable(res.getError()).orElse(new Response.Error()).getMessage())
                                        .as(description).isNullOrEmpty();
                                }
                                assertThat(res.hasError())
                                        .as(description).isNotEqualTo(expectAuthorized);
                                if (res.hasError()) {
                                    assertThat(res.getError().getMessage())
                                            .as(description).endsWith(policy);
                                }
                            });
                });
    }

    @Step("`<clientId>` requests access token for scope(s) `<scopes>` and audience(s) `<nodes>` from the authorization server")
    public void requestAccessToken(String clientId, String scopes, String nodes) {
        oAuth2Service.requestAccessToken(
            clientId,
            Arrays.stream(nodes.split(",")).map(this::cleanData).collect(Collectors.toList()),
            Arrays.stream(scopes.split(",")).map(this::cleanData).collect(Collectors.toList())
        )
            .doOnTerminate(Context::removeAccessToken)
            .blockingForEach(t -> {
                DataStoreFactory.getScenarioDataStore().put(clientId, t);
            });
    }

    private String cleanData(String d) {
        return Stream.of(d).map(StringUtils::trim)
                .map(s -> StringUtils.removeStart(s, "`"))
                .map(s -> StringUtils.removeEnd(s, "`"))
                .map(StringUtils::trim).collect(Collectors.joining());
    }

    private static class ApiCall {
        public String name;
        public String node;

        public ApiCall(String name, String node) {
            List<String> v = Stream.of(name, node)
                    .map(StringUtils::trim)
                    .map(s -> StringUtils.removeStart(s, "`"))
                    .map(s -> StringUtils.removeEnd(s, "`"))
                    .map(StringUtils::trim)
                    .collect(Collectors.toList());
            this.name = v.get(0);
            this.node = v.get(1);
        }
    }
}
