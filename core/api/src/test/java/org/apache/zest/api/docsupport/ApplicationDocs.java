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
package org.apache.zest.api.docsupport;

import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.ApplicationDescriptor;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.ApplicationAssembly;
import org.apache.zest.bootstrap.ApplicationAssemblyFactory;
import org.apache.zest.bootstrap.Assembler;
import org.apache.zest.bootstrap.AssemblyException;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.bootstrap.LayerAssembly;
import org.apache.zest.bootstrap.ModuleAssembly;
import org.apache.zest.bootstrap.SingletonAssembler;

public class ApplicationDocs
{
    public static void someMethod( String[] args )
        throws Exception
    {
        {
// START SNIPPET: application1
            SingletonAssembler zest = new SingletonAssembler()
            {
                public void assemble( ModuleAssembly assembly )
                    throws AssemblyException
                {
                    assembly.values( MyStuffValueComposite.class );
                }
            };
// END SNIPPET: application1
        }
        {
            Assembler customerListEditAssembler = new DummyAssembler();
            Assembler customerEditAssembler = new DummyAssembler();
            Assembler customerSearchAssembler = new DummyAssembler();
            Assembler accountsListEditAssembler = new DummyAssembler();
            Assembler accountsEditAssembler = new DummyAssembler();
            Assembler accountsSearchAssembler = new DummyAssembler();
            Assembler customerDomainAssembler = new DummyAssembler();
            Assembler accountsDomainAssembler = new DummyAssembler();
// START SNIPPET: application2
            final Assembler[][][] assemblers =
                {
                    { // web layer
                      { // Customer Module
                        customerListEditAssembler,
                        customerEditAssembler,
                        customerSearchAssembler
                      },
                      { // Accounts Module
                        accountsListEditAssembler,
                        accountsEditAssembler,
                        accountsSearchAssembler
                      }
                    },
                    { // domain layer
                      { // Customer Module
                        customerDomainAssembler,
                      },
                      { // Accounts Module
                        accountsDomainAssembler,
                      }
                    }
                };
            Energy4Java zest = new Energy4Java();
            Application app = zest.newApplication( new ApplicationAssembler()
            {

                @Override
                public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                    throws AssemblyException
                {
                    return applicationFactory.newApplicationAssembly( assemblers );
                }
            } );
            app.activate();
// END SNIPPET: application2
        }
    }

    public interface MyStuffValueComposite
    {
    }

    private static class DummyAssembler implements Assembler
    {
        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {

        }
    }

    // START SNIPPET: application3
    private static Energy4Java zest;

    public static void main( String[] args )
        throws Exception
    {
        zest = new Energy4Java();
        ApplicationDescriptor model = zest.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory applicationFactory )
                throws AssemblyException
            {
                return createAssembly( applicationFactory );
            }
        } );
        Application application = model.newInstance( zest.spi() );
    }

    private static ApplicationAssembly createAssembly( ApplicationAssemblyFactory factory )
        throws AssemblyException
    {
        String applicationName = "Example Application";
        ApplicationAssembly app = factory.newApplicationAssembly();
        app.setName( applicationName );
        LayerAssembly webLayer = createWebLayer( app );
        LayerAssembly domainLayer = createDomainLayer( app );
        LayerAssembly infraLayer = createInfrastructureLayer( app );
        webLayer.uses( domainLayer );
        webLayer.uses( infraLayer );  // Accesses the WebService
        domainLayer.uses( infraLayer ); // For persistence
        return app;
    }

    private static LayerAssembly createWebLayer(
        ApplicationAssembly application
    )
    {
        LayerAssembly layer = application.layer( "Web Layer" );
        createCustomerWebModule( layer );
        return layer;
    }

    private static LayerAssembly createDomainLayer(
        ApplicationAssembly application
    )
    {
        LayerAssembly layer = application.layer( "Domain Layer" );
        createCustomerDomainModule( layer );
        // :
        // :
        return layer;
    }

    private static LayerAssembly createInfrastructureLayer(
        ApplicationAssembly application
    )
        throws AssemblyException
    {
        LayerAssembly layer = application.layer( "Infrastructure Layer" );
        createWebServiceModule( layer );
        createPersistenceModule( layer );
        return layer;
    }

    private static void createCustomerWebModule( LayerAssembly layer )
    {
        ModuleAssembly assembly = layer.module( "Customer Web Module" );
        assembly.transients( CustomerViewComposite.class );
        assembly.transients( CustomerEditComposite.class );
        assembly.transients( CustomerListViewComposite.class );
        assembly.transients( CustomerSearchComposite.class );
    }

    private static void createCustomerDomainModule( LayerAssembly layer )
    {
        ModuleAssembly assembly = layer.module( "Customer Domain Module" );
        assembly.entities( CustomerEntity.class );
        assembly.entities( CountryEntity.class );
        assembly.transients( AddressComposite.class );
    }

    private static void createWebServiceModule( LayerAssembly layer )
        throws AssemblyException
    {
        ModuleAssembly assembly = layer.module( "Web Service Module" );
        // Someone has created an assembler for a Jetty Web Service.
        JettyAssembler jetty = new JettyAssembler( 8080 );
        jetty.assemble( assembly );
    }

    private static void createPersistenceModule( LayerAssembly layer )
        throws AssemblyException
    {
        ModuleAssembly assembly = layer.module( "Persistence Module" );
        // Someone has created an assembler for the Neo EntityStore
        NeoAssembler neo = new NeoAssembler( "./neostore" );
        neo.assemble( assembly );
    }
// START SNIPPET: application3

    public static class CustomerViewComposite
    {

    }
    public static class CustomerEditComposite
    {

    }
    public static class CustomerListViewComposite
    {

    }
    public static class CustomerSearchComposite
    {

    }


    public static class CustomerEntity
    {

    }
    public static class CountryEntity
    {

    }
    public static class AddressComposite
    {

    }

    public static class JettyAssembler
        implements Assembler
    {

        public JettyAssembler( int port )
        {
        }

        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
        }
    }
    public static class NeoAssembler
        implements Assembler
    {

        public NeoAssembler( String s )
        {
        }

        @Override
        public void assemble( ModuleAssembly module )
            throws AssemblyException
        {
        }
    }
}

