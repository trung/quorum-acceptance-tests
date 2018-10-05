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

package com.quorum.gauge.core;

import com.quorum.gauge.services.AccountService;
import com.quorum.gauge.services.ContractService;
import com.quorum.gauge.services.TransactionService;
import com.thoughtworks.gauge.datastore.DataStoreFactory;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;

public abstract class AbstractSpecImplementation {

    @Autowired
    protected ContractService contractService;

    @Autowired
    protected TransactionService transactionService;

    @Autowired
    protected AccountService accountService;

    @Autowired
    protected OkHttpClient okHttpClient;

    protected BigInteger currentBlockNumber() {
        return (BigInteger) DataStoreFactory.getScenarioDataStore().get("blocknumber");
    }
}