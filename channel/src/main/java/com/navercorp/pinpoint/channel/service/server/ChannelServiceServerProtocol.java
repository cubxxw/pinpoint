/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.channel.service.server;

import java.net.URI;

/**
 * @author youngjin.kim2
 *
 * Server-side protocol for the channel service. It must provide following information.
 * <br>
 * 1. The way to serialize and deserialize the demand and supply objects.
 * 2. Which channel to listen for the demand.
 * 3. Which channel to send the supply.
 *
 * @see com.navercorp.pinpoint.channel.service.ChannelServiceProtocol
 */
public interface ChannelServiceServerProtocol<D, S> {

    D deserializeDemand(byte[] bytes);

    byte[] serializeSupply(S supply);

    URI getDemandSubChannelURI();

    URI getSupplyChannelURI(D demand);

}
