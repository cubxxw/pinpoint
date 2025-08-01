/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.collector.applicationmap.dao.hbase;

import com.navercorp.pinpoint.collector.applicationmap.Vertex;
import com.navercorp.pinpoint.collector.applicationmap.config.MapLinkProperties;
import com.navercorp.pinpoint.collector.applicationmap.dao.MapOutLinkDao;
import com.navercorp.pinpoint.collector.applicationmap.statistics.BulkWriter;
import com.navercorp.pinpoint.collector.applicationmap.statistics.ColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.InLinkColumnName;
import com.navercorp.pinpoint.collector.applicationmap.statistics.LinkRowKey;
import com.navercorp.pinpoint.collector.applicationmap.statistics.RowKey;
import com.navercorp.pinpoint.common.server.util.ApplicationMapStatisticsUtils;
import com.navercorp.pinpoint.common.timeseries.window.TimeSlot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Objects;

/**
 * Update statistics of caller node
 * 
 * @author netspider
 * @author emeroad
 * @author HyunGil Jeong
 */
@Repository
public class HbaseMapOutLinkDao implements MapOutLinkDao {

    private final Logger logger = LogManager.getLogger(this.getClass());


    private final TimeSlot timeSlot;
    private final BulkWriter bulkWriter;
    private final MapLinkProperties mapLinkProperties;

    public HbaseMapOutLinkDao(MapLinkProperties mapLinkProperties,
                              TimeSlot timeSlot,
                              @Qualifier("outLinkBulkWriter") BulkWriter bulkWriter) {
        this.mapLinkProperties = Objects.requireNonNull(mapLinkProperties, "mapLinkConfiguration");
        this.timeSlot = Objects.requireNonNull(timeSlot, "timeSlot");

        this.bulkWriter = Objects.requireNonNull(bulkWriter, "bulkWrtier");
    }


    @Override
    public void outLink(long requestTime, Vertex outVertex, String outAgentId,
                        Vertex inVertex, String inHost, int elapsed, boolean isError) {
        Objects.requireNonNull(outVertex, "outVertex");
        Objects.requireNonNull(inVertex, "inVertex");


        if (logger.isDebugEnabled()) {
            logger.debug("[OutLink] {}/{} -> {}/{}", outVertex, outAgentId, inVertex, inHost);
        }

        // there may be no endpoint in case of httpclient
        inHost = Objects.toString(inHost, "");

        // make row key. rowkey is me
        final long rowTimeSlot = timeSlot.getTimeSlot(requestTime);
        final RowKey outLinkRowKey = LinkRowKey.of(outVertex, rowTimeSlot);

        final short inSlotNumber = ApplicationMapStatisticsUtils.getSlotNumber(inVertex.serviceType(), elapsed, isError);

        final ColumnName inLink = InLinkColumnName.histogram(outAgentId, inVertex, inHost, inSlotNumber);
        this.bulkWriter.increment(outLinkRowKey, inLink);

        if (mapLinkProperties.isEnableAvg()) {
            final ColumnName sumInLink = InLinkColumnName.sum(outAgentId, inVertex, inHost, outVertex.serviceType());
            this.bulkWriter.increment(outLinkRowKey, sumInLink, elapsed);
        }
        if (mapLinkProperties.isEnableMax()) {
            final ColumnName maxInLink = InLinkColumnName.max(outAgentId, inVertex, inHost, outVertex.serviceType());
            this.bulkWriter.updateMax(outLinkRowKey, maxInLink, elapsed);
        }

    }

    @Override
    public void flushLink() {
        this.bulkWriter.flushLink();
    }

    @Override
    public void flushAvgMax() {
        this.bulkWriter.flushAvgMax();
    }

}