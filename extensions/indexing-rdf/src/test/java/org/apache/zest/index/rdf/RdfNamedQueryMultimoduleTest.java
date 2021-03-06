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

package org.apache.zest.index.rdf;

import org.apache.zest.api.common.Visibility;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.unitofwork.DefaultUnitOfWorkAssembler;
import org.apache.zest.index.rdf.assembly.RdfMemoryStoreAssembler;
import org.apache.zest.test.EntityTestAssembler;

public class RdfNamedQueryMultimoduleTest
    extends RdfNamedQueryTest
{
    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        LayerAssembly layer = module.layer();
        assembleEntities( module, Visibility.module );
        assembleValues( module, Visibility.module );

        ModuleAssembly storeModule = layer.module( "store" );
        new DefaultUnitOfWorkAssembler().assemble( storeModule );
        new EntityTestAssembler().visibleIn( Visibility.layer ).assemble( storeModule );
        assembleValues( storeModule, Visibility.module );

        ModuleAssembly indexModule = layer.module( "index" );
        new DefaultUnitOfWorkAssembler().assemble( indexModule );
        new RdfMemoryStoreAssembler( Visibility.layer, Visibility.module ).assemble( indexModule );
    }

}
