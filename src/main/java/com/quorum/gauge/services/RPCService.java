package com.quorum.gauge.services;

import com.quorum.gauge.common.QuorumNetworkProperty;
import io.reactivex.Observable;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.Response;

import java.util.List;

/**
 * Generic service that can invoke JSON RPC APIs
 */
@Service
public class RPCService extends AbstractService {
    public <S, T extends Response> Observable<T> call(QuorumNetworkProperty.Node node, String method, List<S> params, Class<T> responseClass) {
        Request<S, T> request = new Request<>(
                method,
                params,
                connectionFactory().getWeb3jService(node),
                responseClass
        );
        return request.flowable().toObservable();
    }
}
