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
 */
package org.apache.zest.index.elasticsearch.assembly;

import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.elasticsearch.ElasticSearchConfiguration;
import org.apache.zest.index.elasticsearch.client.ESClientIndexQueryService;
import org.apache.zest.index.elasticsearch.internal.AbstractElasticSearchAssembler;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;
import org.elasticsearch.client.Client;

public class ESClientIndexQueryAssembler
    extends AbstractElasticSearchAssembler<ESClientIndexQueryAssembler>
{
    private final Client client;

    public ESClientIndexQueryAssembler( final Client client )
    {
        this.client = client;
    }

    @Override
    public void assemble( final ModuleAssembly module ) throws AssemblyException
    {
        module.services( ESClientIndexQueryService.class )
              .identifiedBy( identity() )
              .setMetaInfo( client )
              .visibleIn( visibility() )
              .instantiateOnStartup();

        module.services( OrgJsonValueSerializationService.class )
              .taggedWith( ValueSerialization.Formats.JSON );

        if( hasConfig() )
        {
            configModule().entities( ElasticSearchConfiguration.class )
                          .visibleIn( configVisibility() );
        }
    }
}
