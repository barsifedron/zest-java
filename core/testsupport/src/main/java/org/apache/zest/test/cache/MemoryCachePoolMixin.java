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
package org.apache.zest.test.cache;

import java.util.concurrent.ConcurrentHashMap;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.spi.cache.Cache;

import static org.apache.zest.api.util.Collectors.single;

/**
 * In-Memory CachePool Mixin based on ConcurrentHashMap.
 */
public abstract class MemoryCachePoolMixin
    implements MemoryCachePoolService
{
    private final ConcurrentHashMap<String, MemoryCacheImpl<?>> caches = new ConcurrentHashMap<>();

    @Override
    public <T> Cache<T> fetchCache( String cacheId, Class<T> valueType )
    {
        NullArgumentException.validateNotEmpty( "cacheId", cacheId );
        MemoryCacheImpl<?> cache = caches.get( cacheId );
        if( cache == null )
        {
            cache = createNewCache( cacheId, valueType );
            caches.put( cacheId, cache );
        }
        cache.incRefCount();
        return (Cache<T>) cache;
    }

    private <T> MemoryCacheImpl<T> createNewCache( String cacheId, Class<T> valueType )
    {
        return new MemoryCacheImpl<>( cacheId, new ConcurrentHashMap<String, Object>(), valueType );
    }

    @Override
    public void returnCache( Cache<?> cache )
    {
        MemoryCacheImpl<?> memory = (MemoryCacheImpl<?>) cache;
        memory.decRefCount();
        if( memory.isNotUsed() )
        {
            caches.remove( memory.cacheId() );
        }
    }

    @Override
    public void activateService()
        throws Exception
    {
        caches.clear();
    }

    @Override
    public void passivateService()
        throws Exception
    {
        caches.clear();
    }

    @Override
    public MemoryCacheImpl<?> singleCache()
    {
        return caches.values().stream().collect( single() );
    }
}
