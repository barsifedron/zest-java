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
package org.apache.zest.index.sql.internal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.apache.zest.api.common.Optional;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.query.grammar.OrderBy;
import org.apache.zest.index.sql.support.api.SQLQuerying;
import org.apache.zest.library.sql.common.SQLUtil;
import org.apache.zest.spi.query.EntityFinder;
import org.apache.zest.spi.query.EntityFinderException;

public class SQLEntityFinder
    implements EntityFinder
{
    @Service
    private SQLQuerying parser;

    @Service
    private DataSource _dataSource;

    /**
     * Helper interface to perform some SQL query. Using this simplifies the structure of some of the methods.
     *
     * @param <ReturnType> The return type of something to be done.
     */
    private interface DoQuery<ReturnType>
    {
        ReturnType doIt( Connection connection )
            throws SQLException;
    }

    @Override
    public long countEntities( Class<?> resultType, @Optional Predicate<Composite> whereClause, Map<String, Object> variables )
        throws EntityFinderException
    {
        final List<Object> values = new ArrayList<>();
        final List<Integer> valueSQLTypes = new ArrayList<>();
        final String query = this.parser.constructQuery( resultType, whereClause, null, null, null, variables, values,
                                                         valueSQLTypes, true );

        return this.performQuery( new DoQuery<Long>()
        {

            @Override
            public Long doIt( Connection connection )
                throws SQLException
            {
                PreparedStatement ps = null;
                ResultSet rs = null;
                try
                {
                    ps = createPS( connection, query, values, valueSQLTypes );
                    rs = ps.executeQuery();
                    rs.next();
                    return rs.getLong( 1 );
                }
                finally
                {
                    SQLUtil.closeQuietly( rs );
                    SQLUtil.closeQuietly( ps );
                }
            }
        } );
    }

    @Override
    public Iterable<EntityReference> findEntities( Class<?> resultType,
                                                   @Optional Predicate<Composite> whereClause,
                                                   @Optional OrderBy[] orderBySegments,
                                                   @Optional final Integer firstResult,
                                                   @Optional final Integer maxResults,
                                                   Map<String, Object> variables )
        throws EntityFinderException
    {
        // TODO what is Zest's policy on negative firstResult and/or maxResults? JDBC has its own way of interpreting
        // these values - does it match with Zest's way?
        Iterable<EntityReference> result;
        if( maxResults == null || maxResults > 0 )
        {
            final List<Object> values = new ArrayList<>();
            final List<Integer> valueSQLTypes = new ArrayList<>();
            final String query = this.parser.constructQuery( resultType, whereClause, orderBySegments, firstResult,
                                                             maxResults, variables, values, valueSQLTypes, false );

            result = this.performQuery( new DoQuery<Iterable<EntityReference>>()
            {
                @Override
                public Iterable<EntityReference> doIt( Connection connection )
                    throws SQLException
                {
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    List<EntityReference> resultList = new ArrayList<>( maxResults == null ? 100 : maxResults );
                    try
                    {
                        // TODO possibility to further optimize by setting fetch size (not too small not too little).
                        Integer rsType = parser.getResultSetType( firstResult, maxResults );
                        ps = createPS( connection, query, values, valueSQLTypes,
                                       rsType, ResultSet.CLOSE_CURSORS_AT_COMMIT );
                        rs = ps.executeQuery();
                        if( firstResult != null
                            && !parser.isFirstResultSettingSupported()
                            && rsType != ResultSet.TYPE_FORWARD_ONLY )
                        {
                            rs.absolute( firstResult );
                        }
                        Integer i = 0;
                        while( rs.next() && ( maxResults == null || i < maxResults ) )
                        {
                            resultList.add( EntityReference.parseEntityReference( rs.getString( 1 ) ) );
                            ++i;
                        }
                    }
                    finally
                    {
                        SQLUtil.closeQuietly( rs );
                        SQLUtil.closeQuietly( ps );
                    }

                    return resultList;
                }

            } );

        }
        else
        {
            result = new ArrayList<>( 0 );
        }

        return result;
    }

    @Override
    public EntityReference findEntity( Class<?> resultType,
                                       @Optional Predicate<Composite> whereClause,
                                       Map<String, Object> variables )
        throws EntityFinderException
    {
        final List<Object> values = new ArrayList<>();
        final List<Integer> valueSQLTypes = new ArrayList<>();
        final String query = this.parser.constructQuery( resultType, whereClause, null, null, null, variables, values,
                                                         valueSQLTypes, false );

        return this.performQuery( new DoQuery<EntityReference>()
        {
            @Override
            public EntityReference doIt( Connection connection )
                throws SQLException
            {
                PreparedStatement ps = null;
                ResultSet rs = null;
                EntityReference result = null;
                try
                {
                    ps = createPS( connection, query, values, valueSQLTypes );
                    ps.setFetchSize( 1 );
                    ps.setMaxRows( 1 );
                    rs = ps.executeQuery();
                    if( rs.next() )
                    {
                        result = EntityReference.parseEntityReference( rs.getString( 1 ) );
                    }
                }
                finally
                {
                    SQLUtil.closeQuietly( rs );
                    SQLUtil.closeQuietly( ps );
                }

                return result;
            }
        } );
    }

    private PreparedStatement createPS( Connection connection, String query,
                                        List<Object> values, List<Integer> valueSQLTypes )
        throws SQLException
    {
        return this.createPS( connection, query, values, valueSQLTypes,
                              ResultSet.TYPE_FORWARD_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT );
    }

    private PreparedStatement createPS( Connection connection, String query,
                                        List<Object> values, List<Integer> valueSQLTypes,
                                        Integer resultSetType, Integer resultSetHoldability )
        throws SQLException
    {
        PreparedStatement ps = connection.prepareStatement( query, resultSetType,
                                                            ResultSet.CONCUR_READ_ONLY, resultSetHoldability );
        if( values.size() != valueSQLTypes.size() )
        {
            throw new InternalError( "There was either too little or too much sql types for values [values="
                                     + values.size() + ", types=" + valueSQLTypes.size() + "]." );
        }

        for( Integer x = 0; x < values.size(); ++x )
        {
            ps.setObject( x + 1, values.get( x ), valueSQLTypes.get( x ) );
        }

        return ps;
    }

    // Helper method to perform SQL queries and handle things if/when something happens
    private <ReturnType> ReturnType performQuery( DoQuery<ReturnType> doQuery )
        throws EntityFinderException
    {
        ReturnType result = null;
        Connection connection = null;
        try
        {
            connection = this._dataSource.getConnection();
            connection.setReadOnly( true );

            result = doQuery.doIt( connection );

        }
        catch( SQLException sqle )
        {
            throw new EntityFinderException( sqle );
        }
        finally
        {
            SQLUtil.closeQuietly( connection );
        }

        return result;
    }

}
