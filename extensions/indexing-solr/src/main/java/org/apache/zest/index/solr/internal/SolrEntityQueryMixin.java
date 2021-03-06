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

package org.apache.zest.index.solr.internal;

import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.query.grammar.OrderBy;
import org.apache.zest.api.query.grammar.QuerySpecification;
import org.apache.zest.functional.Iterables;
import org.apache.zest.index.solr.EmbeddedSolrService;
import org.apache.zest.index.solr.SolrSearch;
import org.apache.zest.spi.query.EntityFinder;
import org.apache.zest.spi.query.EntityFinderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JAVADOC
 */
public class SolrEntityQueryMixin
        implements EntityFinder, SolrSearch
{
    @Service
    private EmbeddedSolrService solr;

    private Logger logger = LoggerFactory.getLogger( SolrEntityQueryMixin.class );

    @Override
    public Iterable<EntityReference> findEntities( Class<?> resultType, @Optional Predicate<Composite> whereClause, @Optional OrderBy[] orderBySegments, @Optional Integer firstResult, @Optional Integer maxResults, Map<String, Object> variables ) throws EntityFinderException
    {
        try
        {
            QuerySpecification expr = (QuerySpecification) whereClause;

            SolrServer server = solr.solrServer();

            NamedList<Object> list = new NamedList<Object>();

            list.add( "q", expr.query() );
            list.add( "rows", maxResults != 0 ? maxResults : 10000 );
            list.add( "start", firstResult );

            if( orderBySegments != null && orderBySegments.length > 0 )
            {
                for( OrderBy orderBySegment : orderBySegments )
                {
                    String propName = ((Member)orderBySegment.property().accessor()).getName() + "_for_sort";
                    String order = orderBySegment.order() == OrderBy.Order.ASCENDING ? "asc" : "desc";
                    list.add( "sort", propName + " " + order );

                }
            }

            SolrParams solrParams = SolrParams.toSolrParams( list );
            logger.debug( "Search:" + list.toString() );

            QueryResponse query = server.query( solrParams );

            SolrDocumentList results = query.getResults();

            List<EntityReference> references = new ArrayList<EntityReference>( results.size() );
            for( SolrDocument result : results )
            {
                references.add( EntityReference.parseEntityReference( result.getFirstValue( "id" ).toString() ) );
            }
            return references;

        } catch( SolrServerException e )
        {
            throw new EntityFinderException( e );
        }
    }

    @Override
    public EntityReference findEntity( Class<?> resultType, @Optional Predicate<Composite> whereClause, Map<String, Object> variables ) throws EntityFinderException
    {
        Iterator<EntityReference> iter = findEntities( resultType, whereClause, null, 0, 1, variables ).iterator();

        if( iter.hasNext() )
            return iter.next();
        else
            return null;
    }

    @Override
    public long countEntities( Class<?> resultType, @Optional Predicate<Composite> whereClause, Map<String, Object> variables ) throws EntityFinderException
    {
        return Iterables.count( findEntities( resultType, whereClause, null, 0, 1, variables ) );
    }

    @Override
    public SolrDocumentList search( String queryString ) throws SolrServerException
    {
        SolrServer server = solr.solrServer();

        NamedList<Object> list = new NamedList<Object>();

        list.add( "q", queryString );

        QueryResponse query = server.query( SolrParams.toSolrParams( list ) );
        return query.getResults();
    }
}
