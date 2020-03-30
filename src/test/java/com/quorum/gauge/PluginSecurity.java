package com.quorum.gauge;

import com.quorum.gauge.core.AbstractSpecImplementation;
import com.thoughtworks.gauge.Step;
import com.thoughtworks.gauge.Table;
import org.springframework.stereotype.Service;

@Service
public class PluginSecurity extends AbstractSpecImplementation {

    @Step("Configure the authorization server to grant `<clientId>` access to scopes `<scopes>` in `<nodes>`")
    public void configure(String clientId, String scopes, String nodes) {

    }

    @Step("`<clientId>` is <policy> to invoke: <table>")
    public void invokeMultiple(String clientId, String policy, Table table) {

    }
}
