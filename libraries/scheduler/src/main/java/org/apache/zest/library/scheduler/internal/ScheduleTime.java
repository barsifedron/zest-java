/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.apache.zest.library.scheduler.internal;

import org.apache.zest.api.util.NullArgumentException;

public final class ScheduleTime
    implements Comparable<ScheduleTime>
{
    private final String scheduleIdentity;
    private final long nextTime;

    public ScheduleTime( String scheduleIdentity, long nextTime )
    {
        NullArgumentException.validateNotEmpty( "scheduleIdentity", scheduleIdentity );
        this.scheduleIdentity = scheduleIdentity;
        this.nextTime = nextTime;
    }

    public long nextTime()
    {
        return nextTime;
    }

    public String scheduleIdentity()
    {
        return scheduleIdentity;
    }

    @Override
    public int compareTo( ScheduleTime another )
    {
        if( this.scheduleIdentity.equals( another.scheduleIdentity ) )
        {
            return 0;
        }

        if( this.nextTime < another.nextTime )
        {
            return -1;
        }
        return 1;
    }
}
