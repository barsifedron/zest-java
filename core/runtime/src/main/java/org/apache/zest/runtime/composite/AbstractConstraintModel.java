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

package org.apache.zest.runtime.composite;

import java.lang.annotation.Annotation;
import org.apache.zest.api.constraint.ConstraintDescriptor;
import org.apache.zest.functional.Visitable;
import org.apache.zest.functional.Visitor;

/**
 * JAVADOC
 */
public abstract class AbstractConstraintModel
    implements ConstraintDescriptor, Visitable<ConstraintDescriptor>
{
    protected final Annotation annotation;

    public AbstractConstraintModel( Annotation annotation )
    {
        this.annotation = annotation;
    }

    @Override
    public Annotation annotation()
    {
        return annotation;
    }

    public abstract ConstraintInstance<?, ?> newInstance();

    @Override
    public <ThrowableType extends Throwable> boolean accept( Visitor<? super ConstraintDescriptor, ThrowableType> modelVisitor )
        throws ThrowableType
    {
        return modelVisitor.visit( this );
    }
}