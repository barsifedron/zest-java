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
package org.apache.zest.index.elasticsearch;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.index.elasticsearch.assembly.ESClientIndexQueryAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationAssembler;
import org.apache.zest.library.fileconfig.FileConfigurationOverride;
import org.apache.zest.test.EntityTestAssembler;
import org.apache.zest.test.indexing.AbstractComplexQueryTest;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import static org.apache.zest.test.util.Assume.assumeNoIbmJdk;

@Ignore( "ElasticSearch Index/Query do not support Complex Queries, ie. queries by 'example values'" )
public class ElasticSearchComplexQueryTest
    extends AbstractComplexQueryTest
{
    @ClassRule
    public static final TemporaryFolder ELASTIC_SEARCH_DIR = new TemporaryFolder();

    @ClassRule
    public static final ESEmbeddedRule ELASTIC_SEARCH = new ESEmbeddedRule( ELASTIC_SEARCH_DIR );

    @Rule
    public final TestName testName = new TestName();

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    @BeforeClass
    public static void beforeClass_IBMJDK()
    {
        assumeNoIbmJdk();
    }

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        super.assemble( module );

        // Config module
        ModuleAssembly config = module.layer().module( "config" );
        new EntityTestAssembler().assemble( config );

        // Index/Query
        new ESClientIndexQueryAssembler( ELASTIC_SEARCH.client() )
            .withConfig( config, Visibility.layer )
            .assemble( module );
        ElasticSearchConfiguration esConfig = config.forMixin( ElasticSearchConfiguration.class ).declareDefaults();
        esConfig.index().set( ELASTIC_SEARCH.indexName( ElasticSearchQueryTest.class.getName(),
                                                        testName.getMethodName() ) );
        esConfig.indexNonAggregatedAssociations().set( Boolean.TRUE );

        // FileConfig
        new FileConfigurationAssembler()
            .withOverride( new FileConfigurationOverride().withConventionalRoot( tmpDir.getRoot() ) )
            .assemble( module );
    }
}
