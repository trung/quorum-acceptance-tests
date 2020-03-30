package com.quorum.gauge;

import com.quorum.gauge.common.QuorumNetworkProperty;
import com.quorum.gauge.core.AbstractSpecImplementation;
import com.quorum.gauge.ext.StringResonse;
import com.thoughtworks.gauge.Step;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.Response;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@Service
public class PluginHelloWorld extends AbstractSpecImplementation {

    @Step("Calling `plugin@helloworld_greeting` API in <node> with single parameter <name> must return <expected>")
    public void greeting(QuorumNetworkProperty.Node node, String name, String expected) {
        StringResonse res = rpcService.call(node, "plugin@helloworld_greeting", Collections.singletonList(name), StringResonse.class).blockingFirst();

        assertThat(res.hasError()).as("Call must be successful but got " + Optional.ofNullable(res.getError()).orElse(new Response.Error()).getMessage()).isFalse();
        assertThat(res.getResult()).isEqualTo(expected);
    }
}
