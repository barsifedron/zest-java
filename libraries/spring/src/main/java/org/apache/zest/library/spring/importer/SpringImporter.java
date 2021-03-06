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

package org.apache.zest.library.spring.importer;

import org.apache.zest.api.service.ImportedServiceDescriptor;
import org.apache.zest.api.service.ServiceImporter;
import org.apache.zest.api.service.ServiceImporterException;
import org.springframework.context.ApplicationContext;

/**
 * Import a service from Spring. This assumes that the service has been imported
 * into the assembly using the SpringImporterAssembler, so that the ApplicationContext
 * is available as service meta-info.
 */
public class SpringImporter
    implements ServiceImporter
{
    /**
     * Import a service from Spring by looking it up in the ApplicationContext.
     *
     * @param serviceDescriptor The service descriptor.
     * @return a service instance
     * @throws ServiceImporterException if unable to import the service
     */
    @Override
    public Object importService( ImportedServiceDescriptor serviceDescriptor ) throws ServiceImporterException
    {
        try
        {
            return serviceDescriptor.
                metaInfo( ApplicationContext.class ).
                getBean( serviceDescriptor.identity().toString(), serviceDescriptor.type() );
        }
        catch( Throwable e )
        {
            throw new ServiceImporterException( "Could not import Spring service with id " + serviceDescriptor.identity(), e );
        }
    }

    @Override
    public boolean isAvailable( Object instance )
    {
        return true;
    }
}
