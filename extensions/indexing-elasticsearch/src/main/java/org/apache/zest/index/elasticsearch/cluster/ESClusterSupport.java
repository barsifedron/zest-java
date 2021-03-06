/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */
package org.apache.zest.index.elasticsearch.cluster;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.index.elasticsearch.ElasticSearchClusterConfiguration;
import org.apache.zest.index.elasticsearch.internal.AbstractElasticSearchSupport;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetSocketAddress;

public class ESClusterSupport
    extends AbstractElasticSearchSupport
{
    @This
    private Configuration<ElasticSearchClusterConfiguration> configuration;

    @Override
    protected void activateElasticSearch()
        throws Exception
    {
        configuration.refresh();
        ElasticSearchClusterConfiguration config = configuration.get();

        String clusterName = config.clusterName().get() == null ? DEFAULT_CLUSTER_NAME : config.clusterName().get();
        index = config.index().get() == null ? DEFAULT_INDEX_NAME : config.index().get();
        indexNonAggregatedAssociations = config.indexNonAggregatedAssociations().get();

        String[] nodes = config.nodes().get() == null
                         ? new String[] { "localhost:9300" }
                         : config.nodes().get().split( "," );
        boolean clusterSniff = config.clusterSniff().get();
        boolean ignoreClusterName = config.ignoreClusterName().get();
        String pingTimeout = config.pingTimeout().get() == null ? "5s" : config.pingTimeout().get();
        String samplerInterval = config.samplerInterval().get() == null ? "5s" : config.samplerInterval().get();

        Settings settings = Settings.builder()
                                    .put( "cluster.name", clusterName )
                                    .put( "client.transport.sniff", clusterSniff )
                                    .put( "client.transport.ignore_cluster_name", ignoreClusterName )
                                    .put( "client.transport.ping_timeout", pingTimeout )
                                    .put( "client.transport.nodes_sampler_interval", samplerInterval )
                                    .build();
        TransportClient transportClient = new PreBuiltTransportClient( settings );
        for( String node : nodes )
        {
            String[] split = node.split( ":" );
            String host = split[ 0 ];
            int port = Integer.valueOf( split[ 1 ] );
            InetSocketAddress socketAddress = new InetSocketAddress( host, port );
            transportClient.addTransportAddress( new InetSocketTransportAddress( socketAddress ) );
        }

        client = transportClient;
    }
}
