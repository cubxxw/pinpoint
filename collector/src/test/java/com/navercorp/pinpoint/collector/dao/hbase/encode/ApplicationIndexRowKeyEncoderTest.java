/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.dao.hbase.encode;

import com.navercorp.pinpoint.common.PinpointConstants;
import com.navercorp.pinpoint.common.hbase.wd.ByteHasher;
import com.navercorp.pinpoint.common.hbase.wd.ByteSaltKey;
import com.navercorp.pinpoint.common.hbase.wd.OneByteSimpleHash;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributor;
import com.navercorp.pinpoint.common.hbase.wd.RowKeyDistributorByHashPrefix;
import com.navercorp.pinpoint.common.server.bo.serializer.agent.ApplicationNameRowKeyEncoder;
import com.navercorp.pinpoint.common.server.scatter.FuzzyRowKeyFactory;
import com.navercorp.pinpoint.common.server.scatter.OneByteFuzzyRowKeyFactory;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationIndexRowKeyEncoderTest {

    private ApplicationIndexRowKeyEncoder encoder;
    private FuzzyRowKeyFactory<Byte> fuzzyRowKeyFactory = new OneByteFuzzyRowKeyFactory();

    @BeforeEach
    void beforeEach() {
        ApplicationNameRowKeyEncoder rowKeyEncoder = new ApplicationNameRowKeyEncoder();
        RowKeyDistributor rowKeyDistributor = applicationTraceIndexDistributor();
        this.encoder = new ApplicationIndexRowKeyEncoder(rowKeyEncoder, rowKeyDistributor);
    }

    private RowKeyDistributor applicationTraceIndexDistributor() {
        ByteHasher hasher = new OneByteSimpleHash(32);
        return new RowKeyDistributorByHashPrefix(hasher);
    }

    @Test
    void encodeRowKey() {
        int elapsedTime = 1000 * 10;

        byte[] rowKey = encoder.encodeRowKey(ByteSaltKey.SALT.size(), "agent", elapsedTime, 2000L);
        rowKey = Arrays.copyOfRange(rowKey, 1, rowKey.length);


        int fuzzySize = PinpointConstants.APPLICATION_NAME_MAX_LEN + Bytes.SIZEOF_LONG + 1;
        assertThat(rowKey).hasSize(fuzzySize);

        byte fuzzyKey = fuzzyRowKeyFactory.getKey(elapsedTime);

        Assertions.assertEquals(fuzzyKey, rowKey[rowKey.length - 1]);
    }
}