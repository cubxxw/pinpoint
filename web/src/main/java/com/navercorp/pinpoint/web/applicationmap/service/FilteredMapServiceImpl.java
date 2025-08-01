/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.web.applicationmap.service;

import com.navercorp.pinpoint.common.hbase.bo.ColumnGetCount;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMap;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilder;
import com.navercorp.pinpoint.web.applicationmap.ApplicationMapBuilderFactory;
import com.navercorp.pinpoint.web.applicationmap.FilterMapWithScatter;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.DefaultNodeHistogramFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.ResponseHistogramsNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.histogram.datasource.WasNodeHistogramDataSource;
import com.navercorp.pinpoint.web.applicationmap.appender.server.DefaultServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.StatisticsServerGroupListFactory;
import com.navercorp.pinpoint.web.applicationmap.appender.server.datasource.ServerGroupListDataSource;
import com.navercorp.pinpoint.web.applicationmap.map.FilteredMap;
import com.navercorp.pinpoint.web.applicationmap.map.FilteredMapBuilder;
import com.navercorp.pinpoint.web.component.ApplicationFactory;
import com.navercorp.pinpoint.web.dao.ApplicationTraceIndexDao;
import com.navercorp.pinpoint.web.dao.TraceDao;
import com.navercorp.pinpoint.web.filter.Filter;
import com.navercorp.pinpoint.web.scatter.ScatterData;
import com.navercorp.pinpoint.web.security.ServerMapDataFilter;
import com.navercorp.pinpoint.web.service.ServerInstanceDatasourceService;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.LimitedScanResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * @author netspider
 * @author emeroad
 * @author minwoo.jung
 */
@Service
public class FilteredMapServiceImpl implements FilteredMapService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TraceDao traceDao;

    private final ApplicationTraceIndexDao applicationTraceIndexDao;

    private final ServiceTypeRegistryService registry;

    private final ApplicationFactory applicationFactory;

    private final ServerInstanceDatasourceService serverInstanceDatasourceService;

    private final ServerMapDataFilter serverMapDataFilter;

    private final ApplicationMapBuilderFactory applicationMapBuilderFactory;

    private static final Object V = new Object();

    @Value("${web.servermap.build.timeout:600000}")
    private long buildTimeoutMillis;

    public FilteredMapServiceImpl(TraceDao traceDao,
                                  ApplicationTraceIndexDao applicationTraceIndexDao,
                                  ServiceTypeRegistryService registry,
                                  ApplicationFactory applicationFactory,
                                  ServerInstanceDatasourceService serverInstanceDatasourceService,
                                  Optional<ServerMapDataFilter> serverMapDataFilter,
                                  ApplicationMapBuilderFactory applicationMapBuilderFactory) {
        this.traceDao = Objects.requireNonNull(traceDao, "traceDao");
        this.applicationTraceIndexDao = Objects.requireNonNull(applicationTraceIndexDao, "applicationTraceIndexDao");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.applicationFactory = Objects.requireNonNull(applicationFactory, "applicationFactory");
        this.serverInstanceDatasourceService = Objects.requireNonNull(serverInstanceDatasourceService, "serverInstanceDatasourceService");
        this.serverMapDataFilter = Objects.requireNonNull(serverMapDataFilter, "serverMapDataFilter").orElse(null);
        this.applicationMapBuilderFactory = Objects.requireNonNull(applicationMapBuilderFactory, "applicationMapBuilderFactory");
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit) {
        return selectTraceIdsFromApplicationTraceIndex(applicationName, range, limit, true);
    }

    @Override
    public LimitedScanResult<List<TransactionId>> selectTraceIdsFromApplicationTraceIndex(String applicationName, Range range, int limit, boolean backwardDirection) {
        Objects.requireNonNull(applicationName, "applicationName");
        Objects.requireNonNull(range, "range");

        if (logger.isTraceEnabled()) {
            logger.trace("scan(selectTraceIdsFromApplicationTraceIndex) {}, {}", applicationName, range);
        }

        return this.applicationTraceIndexDao.scanTraceIndex(applicationName, range, limit, backwardDirection);
    }

    private List<List<SpanBo>> filterList2(List<List<SpanBo>> transactionList, Filter<List<SpanBo>> filter) {
        final List<List<SpanBo>> filteredResult = new ArrayList<>();
        for (List<SpanBo> transaction : transactionList) {
            if (filter.include(transaction)) {
                filteredResult.add(transaction);
            }
        }
        return filteredResult;
    }

    public ApplicationMap selectApplicationMap(FilteredMapServiceOption option) {
        final List<List<SpanBo>> filterList = selectFilteredSpan(option.getTransactionIdList(), option.getFilter(), option.getColumnGetCount());
        TimeWindow timeWindow = newTimeWindow(filterList);
        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, timeWindow);
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        return createMap(timeWindow, option.isUseStatisticsAgentState(), filteredMap);
    }

    private TimeWindow newTimeWindow(List<List<SpanBo>> allSpanList) {
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;

        if (CollectionUtils.isEmpty(allSpanList)) {
            return new TimeWindow(Range.between(min, max));
        }

        final List<SpanBo> spanBoList = allSpanList.get(0);
        for (SpanBo spanBo : spanBoList) {
            min = Math.min(min, spanBo.getStartTime());
            max = Math.max(max, spanBo.getStartTime() + spanBo.getElapsed());
        }

        final long startTime = min;
        final long endTime = max;
        final Range between = Range.between(startTime, endTime);
        return new TimeWindow(between);
    }

    public FilterMapWithScatter selectApplicationMapWithScatterData(FilteredMapServiceOption option) {
        StopWatch watch = new StopWatch();
        watch.start();

        final List<List<SpanBo>> filterList = selectFilteredSpan(option.getTransactionIdList(), option.getFilter(), option.getColumnGetCount());
        Range scanRange = option.getRange();
        TimeWindow timeWindow = new TimeWindow(scanRange);

        FilteredMapBuilder filteredMapBuilder = new FilteredMapBuilder(applicationFactory, registry, timeWindow);
        filteredMapBuilder.serverMapDataFilter(serverMapDataFilter);
        filteredMapBuilder.addTransactions(filterList);
        FilteredMap filteredMap = filteredMapBuilder.build();

        ApplicationMap map = createMap(timeWindow, option.isUseStatisticsAgentState(), filteredMap);

        Map<Application, ScatterData> applicationScatterData = filteredMap
                .getApplicationScatterData(
                        scanRange.getFrom(), scanRange.getTo(),
                        option.getXGroupUnit(), option.getYGroupUnit()
                );

        watch.stop();
        logger.debug("Select filtered application map elapsed. {}ms", watch.getTotalTimeMillis());

        return new FilterMapWithScatter(map, applicationScatterData);
    }


    private List<List<SpanBo>> selectFilteredSpan(List<TransactionId> transactionIdList, Filter<List<SpanBo>> filter, ColumnGetCount columnGetCount) {
        // filters out recursive calls by looking at each objects
        // do not filter here if we change to a tree-based collision check in the future.
        final List<TransactionId> recursiveFilterList = recursiveCallFilter(transactionIdList);

        // FIXME might be better to simply traverse the List<Span> and create a process chain for execution
        final List<List<SpanBo>> originalList = this.traceDao.selectAllSpans(recursiveFilterList, columnGetCount);

        return filterList2(originalList, filter);
    }

    private ApplicationMap createMap(TimeWindow timeWindow, boolean isUseStatisticsAgentState, FilteredMap filteredMap) {
        final ApplicationMapBuilder applicationMapBuilder = applicationMapBuilderFactory.createApplicationMapBuilder(timeWindow);
        final WasNodeHistogramDataSource wasNodeHistogramDataSource = new ResponseHistogramsNodeHistogramDataSource(filteredMap.getResponseHistograms());
        applicationMapBuilder.includeNodeHistogram(new DefaultNodeHistogramFactory(wasNodeHistogramDataSource));
        ServerGroupListDataSource serverGroupListDataSource = serverInstanceDatasourceService.getServerGroupListDataSource();
        if (isUseStatisticsAgentState) {
            applicationMapBuilder.includeServerInfo(new StatisticsServerGroupListFactory(serverGroupListDataSource));
        } else {
            applicationMapBuilder.includeServerInfo(new DefaultServerGroupListFactory(serverGroupListDataSource));
        }
        ApplicationMap map = applicationMapBuilder.build(filteredMap.getLinkDataDuplexMap(), buildTimeoutMillis);
        if (serverMapDataFilter != null) {
            map = serverMapDataFilter.dataFiltering(map);
        }

        return map;
    }

    private List<TransactionId> recursiveCallFilter(List<TransactionId> transactionIdList) {
        Objects.requireNonNull(transactionIdList, "transactionIdList");

        List<TransactionId> crashKey = new ArrayList<>();
        Map<TransactionId, Object> filterMap = new LinkedHashMap<>(transactionIdList.size());
        for (TransactionId transactionId : transactionIdList) {
            Object old = filterMap.put(transactionId, V);
            if (old != null) {
                crashKey.add(transactionId);
            }
        }
        if (!crashKey.isEmpty()) {
            Set<TransactionId> filteredTransactionId = filterMap.keySet();
            logger.info("transactionId crash found. original:{} filter:{} crashKey:{}", transactionIdList.size(), filteredTransactionId.size(), crashKey);
            return new ArrayList<>(filteredTransactionId);
        }
        return transactionIdList;
    }
}
