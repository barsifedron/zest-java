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
package org.apache.zest.api.service;

import org.apache.zest.api.activation.Activators;

/**
 * Convenience interface for simple Service Activation.
 *
 * Let your ServiceComposite extends ServiceActivation and implement it in one of its Mixins.
 * A corresponding Activator is automatically registered.
 */
@Activators( ServiceActivation.ServiceActivator.class )
public interface ServiceActivation
{

    /**
     * Called after ServiceComposite Activation.
     */
    void activateService()
        throws Exception;

    /**
     * Called before ServiceComposite Passivation.
     */
    void passivateService()
        throws Exception;

    /**
     * Service Activator.
     */
    class ServiceActivator
        extends ServiceActivatorAdapter<ServiceActivation>
    {

        @Override
        public void afterActivation( ServiceReference<ServiceActivation> activated )
            throws Exception
        {
            activated.get().activateService();
        }

        @Override
        public void beforePassivation( ServiceReference<ServiceActivation> passivating )
            throws Exception
        {
            passivating.get().passivateService();
        }

    }

}
