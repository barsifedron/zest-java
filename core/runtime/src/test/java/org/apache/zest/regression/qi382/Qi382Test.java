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
package org.apache.zest.regression.qi382;

import org.apache.zest.api.identity.Identity;
import org.apache.zest.api.identity.StringIdentity;
import org.apache.zest.api.unitofwork.UnitOfWorkFactory;
import org.junit.Test;
import org.apache.zest.api.association.Association;
import org.apache.zest.api.entity.EntityBuilder;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.entity.Lifecycle;
import org.apache.zest.api.entity.LifecycleException;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.value.ValueSerialization;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.entitystore.memory.MemoryEntityStoreService;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.valueserialization.orgjson.OrgJsonValueSerializationService;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

public class Qi382Test extends AbstractZestTest
{

    public static final Identity FERRARI = new StringIdentity( "Ferrari" );
    public static final Identity NICLAS = new StringIdentity( "Niclas" );

    @Override
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        module.addServices( OrgJsonValueSerializationService.class ).taggedWith( ValueSerialization.Formats.JSON );
        module.addServices( MemoryEntityStoreService.class );
        module.entities( Car.class, Person.class );
    }

    @Test
    public void givenCreationOfTwoEntitiesWhenAssigningOneToOtherExpectCompletionToSucceed()
        throws UnitOfWorkCompletionException
    {
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            Car car = unitOfWork.newEntity( Car.class, FERRARI);
            unitOfWork.complete();
        }
        try( UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork() )
        {
            Car car = unitOfWork.get( Car.class, FERRARI);
            assertThat( car, notNullValue() );
            Person p = unitOfWork.get( Person.class, NICLAS);
            assertThat( p, notNullValue() );
            assertThat( p.car().get(), equalTo( car ) );
        }
    }

    @Mixins( Car.CarMixin.class )
    public interface Car extends EntityComposite, Lifecycle
    {

        static class CarMixin implements Lifecycle
        {
            @This
            private Car me;

            @Structure
            private UnitOfWorkFactory uowf;

            @Override
            public void create()
                throws LifecycleException
            {
                UnitOfWork unitOfWork = uowf.currentUnitOfWork();
                EntityBuilder<Person> builder = unitOfWork.newEntityBuilder( Person.class, NICLAS);
                builder.instance().car().set( me );
                builder.newInstance();
            }

            @Override
            public void remove()
                throws LifecycleException
            {

            }
        }
    }

    public interface Person extends EntityComposite
    {
        Association<Car> car();
    }
}
