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

package org.apache.zest.runtime.mixin;

import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.identity.StringIdentity;
import org.junit.Test;
import org.apache.zest.api.composite.TransientComposite;
import org.apache.zest.api.entity.EntityComposite;
import org.apache.zest.api.mixin.Mixins;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.test.AbstractZestTest;
import org.apache.zest.test.EntityTestAssembler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Test of declaring types in assembly
 */
public class AssemblyRoleTest
    extends AbstractZestTest
{
    public void assemble( ModuleAssembly module )
        throws AssemblyException
    {
        new EntityTestAssembler().assemble( module );

        module.transients( FooComposite.class ).withTypes( Foo.class );
        module.transients( FooComposite2.class ).withTypes( Foo.class ).withMixins( CustomFooMixin.class );

        module.entities( FooEntity.class ).withTypes( Foo.class );
    }

    @Test
    public void testAssemblyTypesCustomMixin()
    {
        FooComposite2 composite2 = transientBuilderFactory.newTransient( FooComposite2.class );
        assertThat( "Custom mixin has executed", ( (Foo) composite2 ).test( "Foo", 42 ), equalTo( "Foo/42" ) );
    }

    @Test
    public void testAssemblyTypesDefaultMixin()
    {
        FooComposite composite = transientBuilderFactory.newTransient( FooComposite.class );
        assertThat( "Default mixin has executed", ( (Foo) composite ).test( "Foo", 42 ), equalTo( "Foo 42" ) );
    }

    @Test
    public void testAssemblyMixinsEntity()
        throws UnitOfWorkCompletionException
    {
        UnitOfWork uow = unitOfWorkFactory.newUnitOfWork();
        uow.newEntity( FooEntity.class, new StringIdentity( "123" ) );
        uow.complete();

        uow = unitOfWorkFactory.newUnitOfWork();
        Foo foo = uow.get( Foo.class, new StringIdentity( "123" ) );

        try
        {
            assertThat( "Default mixin has executed", foo.test( "Foo", 42 ), equalTo( "Foo 42" ) );
        }
        finally
        {
            uow.discard();
        }
    }

    public interface FooComposite
        extends TransientComposite
    {
    }

    public interface FooComposite2
        extends TransientComposite
    {
    }

    @Mixins( FooMixin.class )
    public interface Foo
    {
        String test( String foo, int bar );
    }

    public static class FooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + " " + bar;
        }
    }

    public static class CustomFooMixin
        implements Foo
    {
        public String test( String foo, int bar )
        {
            return foo + "/" + bar;
        }
    }

    public interface FooEntity
        extends EntityComposite
    {

    }
}