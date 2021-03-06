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
package org.apache.zest.runtime.activation;

import org.apache.zest.api.activation.Activator;
import org.apache.zest.api.activation.ActivatorDescriptor;
import org.apache.zest.api.common.ConstructionException;
import org.apache.zest.functional.HierarchicalVisitor;
import org.apache.zest.functional.VisitableHierarchy;
import org.apache.zest.runtime.composite.ConstructorsModel;
import org.apache.zest.runtime.injection.InjectedFieldsModel;
import org.apache.zest.runtime.injection.InjectedMethodsModel;
import org.apache.zest.runtime.injection.InjectionContext;

/**
 * Model for a single Activator.
 *
 * @param <ActivateeType> Type of the activation target
 */
public class ActivatorModel<ActivateeType>
    implements ActivatorDescriptor, VisitableHierarchy<Object, Object>
{
    private final Class<? extends Activator<ActivateeType>> activatorType;
    private final ConstructorsModel constructorsModel;
    private final InjectedFieldsModel injectedFieldsModel;
    private final InjectedMethodsModel injectedMethodsModel;

    public ActivatorModel( Class<? extends Activator<ActivateeType>> activatorType )
    {
        this.activatorType = activatorType;
        this.constructorsModel = new ConstructorsModel( activatorType );
        this.injectedFieldsModel = new InjectedFieldsModel( activatorType );
        this.injectedMethodsModel = new InjectedMethodsModel( activatorType );
    }

    @Override
    public <ThrowableType extends Throwable> boolean accept( HierarchicalVisitor<? super Object, ? super Object, ThrowableType> visitor )
        throws ThrowableType
    {
        if( visitor.visitEnter( this ) )
        {
            if( constructorsModel.accept( visitor ) )
            {
                if( injectedFieldsModel.accept( visitor ) )
                {
                    injectedMethodsModel.accept( visitor );
                }
            }
        }
        return visitor.visitLeave( this );
    }

    public Activator<ActivateeType> newInstance()
    {
        try
        {
            return activatorType.newInstance();
        }
        catch( InstantiationException | IllegalAccessException ex )
        {
            throw new ConstructionException( "Could not instantiate " + activatorType.getName(), ex );
        }
    }

    @SuppressWarnings( "unchecked" )
    public Activator<ActivateeType> newInstance( InjectionContext injectionContext )
    {
        try
        {
            Activator<ActivateeType> instance = (Activator<ActivateeType>) constructorsModel.newInstance( injectionContext );
            injectionContext = new InjectionContext( injectionContext.module(), injectionContext.uses(), instance );
            inject( injectionContext, instance );
            return instance;
        }
        catch( Exception ex )
        {
            throw new ConstructionException( "Could not instantiate " + activatorType.getName(), ex );
        }
    }

    public void inject( InjectionContext injectionContext, Activator<ActivateeType> instance )
    {
        injectedFieldsModel.inject( injectionContext, instance );
        injectedMethodsModel.inject( injectionContext, instance );
    }

    @Override
    public String toString()
    {
        return activatorType.getName();
    }

}
