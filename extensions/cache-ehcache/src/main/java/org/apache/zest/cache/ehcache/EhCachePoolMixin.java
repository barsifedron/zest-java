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
package org.apache.zest.cache.ehcache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.zest.api.configuration.Configuration;
import org.apache.zest.api.identity.HasIdentity;
import org.apache.zest.api.injection.scope.This;
import org.apache.zest.api.util.NullArgumentException;
import org.apache.zest.spi.cache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.expiry.Duration;
import org.ehcache.expiry.Expirations;

public abstract class EhCachePoolMixin
    implements EhCachePoolService
{
    private static final long DEFAULT_HEAP_SIZE = 1024 * 1024;
    private final ConcurrentHashMap<String, EhCacheImpl<?>> caches = new ConcurrentHashMap<>();

    @This
    private HasIdentity identity;

    @This
    private Configuration<EhCacheConfiguration> configuration;

    private CacheManager cacheManager;

    @Override
    public void activateService()
            throws Exception
    {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withDefaultDiskStoreThreadPool( cacheManagerThreadPoolName( "disk-store" ) )
                .withDefaultEventListenersThreadPool( cacheManagerThreadPoolName( "event-listeners" ) )
                .withDefaultWriteBehindThreadPool( cacheManagerThreadPoolName( "write-behind" ) )
                .build();
        cacheManager.init();
    }

    @Override
    public void passivateService()
            throws Exception
    {
        cacheManager.close();
        cacheManager = null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Cache<T> fetchCache( String cacheId, Class<T> valueType )
    {
        // Note: Small bug in Ehcache; If the cache name is an empty String it will actually work until
        //       you try to remove the Cache instance from the CacheManager, at which point it is silently
        //       ignored but not removed so there is a follow up problem of too much in the CacheManager.
        NullArgumentException.validateNotEmpty( "cacheId", cacheId );
        EhCacheImpl<?> cache = caches.computeIfAbsent( cacheId, key -> createNewCache( cacheId, valueType ) );
        cache.incRefCount();
        return (Cache<T>) cache;
    }

    private <T> EhCacheImpl<T> createNewCache( String cacheId, Class<T> valueType )
    {
        configuration.refresh();
        EhCacheConfiguration config = configuration.get();

        ResourcePoolsBuilder poolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap( config.heapSize().get(), MemoryUnit.valueOf( config.heapUnit().get() ) );
        if( config.offHeapSize().get() != null )
        {
            poolsBuilder = poolsBuilder
                    .offheap( config.offHeapSize().get(), MemoryUnit.valueOf( config.offHeapUnit().get() ) );
        }
        if( config.diskSize().get() != null )
        {
            poolsBuilder = poolsBuilder
                    .disk( config.diskSize().get(), MemoryUnit.valueOf( config.diskUnit().get() ), config.diskPersistent().get() );
        }

        CacheConfigurationBuilder<String, T> configBuilder = CacheConfigurationBuilder
                .newCacheConfigurationBuilder( String.class, valueType, poolsBuilder );
        if( config.maxObjectSize().get() != null )
        {
            configBuilder = configBuilder
                    .withSizeOfMaxObjectSize( config.maxObjectSize().get(), MemoryUnit.valueOf( config.maxObjectSizeUnit().get() ) );
        }
        if( config.maxObjectGraphDepth().get() != null )
        {
            configBuilder = configBuilder
                    .withSizeOfMaxObjectGraph( config.maxObjectGraphDepth().get() );
        }
        switch( config.expiry().get() ) {
            case "TIME_TO_IDLE":
                configBuilder = configBuilder.withExpiry( Expirations.timeToIdleExpiration( Duration.of(
                    config.expiryLength().get() == null ? -1L : config.expiryLength().get(),
                    TimeUnit.valueOf( config.expiryTimeUnit().get() )
                ) ) );
                break;
            case "TIME_TO_LIVE":
                configBuilder = configBuilder.withExpiry( Expirations.timeToLiveExpiration( Duration.of(
                    config.expiryLength().get() == null ? -1L : config.expiryLength().get(),
                    TimeUnit.valueOf( config.expiryTimeUnit().get() )
                ) ) );
                break;
            case "NONE":
            default:
                configBuilder = configBuilder.withExpiry( Expirations.noExpiration() );
                break;
        }
        CacheConfiguration<String, T> cacheConfig = configBuilder.build();
        org.ehcache.Cache<String,T> cache =  cacheManager.createCache( cacheId, cacheConfig );
        return new EhCacheImpl<>( cacheId, cache, valueType );
    }

    @Override
    public void returnCache( Cache<?> cache )
    {
        EhCacheImpl<?> eh = (EhCacheImpl<?>) cache;
        eh.decRefCount();
        if( eh.isNotUsed() )
        {
            caches.remove( eh.cacheId() );
            cacheManager.removeCache( eh.cacheId() );
        }
    }

    private String cacheManagerThreadPoolName( String name )
    {
        return identity.identity().getClass() + "-" + name;
    }
}
