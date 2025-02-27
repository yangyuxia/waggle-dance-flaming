/**
 * Copyright (C) 2016-2023 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.bdp.waggledance.server;

import org.apache.hadoop.hive.metastore.security.HadoopThriftAuthBridge;
import org.apache.thrift.TProcessor;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class TProcessorFactorySaslDecorator extends TProcessorFactory {

  private final HadoopThriftAuthBridge.Server saslServer;
  private final TProcessorFactory tProcessorFactory;

  TProcessorFactorySaslDecorator(TProcessorFactory tProcessorFactory, HadoopThriftAuthBridge.Server saslServer) throws TTransportException {
    super(null);
    this.tProcessorFactory = tProcessorFactory;
    this.saslServer = saslServer;
  }

  @Override
  public TProcessor getProcessor(TTransport transport) {
    try {
      TProcessor tProcessor = tProcessorFactory.getProcessor(transport);
      return saslServer.wrapProcessor(tProcessor);
    } catch (RuntimeException e) {
      throw new RuntimeException("Error creating SASL wrapped TProcessor", e);
    }
  }

}
