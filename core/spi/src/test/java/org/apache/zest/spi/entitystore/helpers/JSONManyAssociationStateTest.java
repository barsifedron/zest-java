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
package org.apache.zest.spi.entitystore.helpers;

import java.util.ArrayList;
import java.util.List;
import org.apache.zest.api.entity.EntityReference;
import org.apache.zest.api.time.SystemTime;
import org.apache.zest.spi.entity.EntityStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

public class JSONManyAssociationStateTest
{

    @Test
    public void givenEmptyJSONManyAssociationStateWhenAddingTwoRefsAtZeroIndexExpectCorrectOrder()
        throws JSONException
    {
        // Fake JSONManyAssociationState
        JSONObject state = new JSONObject();
        state.put( JSONKeys.PROPERTIES, new JSONObject() );
        state.put( JSONKeys.ASSOCIATIONS, new JSONObject() );
        state.put( JSONKeys.MANY_ASSOCIATIONS, new JSONObject() );
        state.put( JSONKeys.NAMED_ASSOCIATIONS, new JSONObject() );
        JSONEntityState entityState = new JSONEntityState( null,
                                                           null,
                                                           "0",
                                                           SystemTime.now(),
                                                           EntityReference.parseEntityReference( "123" ),
                                                           EntityStatus.NEW,
                                                           null,
                                                           state );
        JSONManyAssociationState jsonState = new JSONManyAssociationState( entityState, new JSONArray() );

        jsonState.add( 0, EntityReference.parseEntityReference( "first" ) );
        jsonState.add( 0, EntityReference.parseEntityReference( "second" ) );

        assertThat( jsonState.count(), equalTo( 2 ) );
    }

    @Test
    public void givenJSONManyAssociationStateWhenChangingReferencesExpectCorrectBehavior()
        throws JSONException
    {
        // Fake JSONManyAssociationState
        JSONObject state = new JSONObject();
        state.put( JSONKeys.PROPERTIES, new JSONObject() );
        state.put( JSONKeys.ASSOCIATIONS, new JSONObject() );
        state.put( JSONKeys.MANY_ASSOCIATIONS, new JSONObject() );
        state.put( JSONKeys.NAMED_ASSOCIATIONS, new JSONObject() );
        JSONEntityState entityState = new JSONEntityState( null,
                                                           null,
                                                           "0",
                                                           SystemTime.now(),
                                                           EntityReference.parseEntityReference( "123" ),
                                                           EntityStatus.NEW,
                                                           null,
                                                           state );
        JSONManyAssociationState jsonState = new JSONManyAssociationState( entityState, new JSONArray() );

        assertThat( jsonState.contains( EntityReference.parseEntityReference( "NOT_PRESENT" ) ), is( false ) );

        jsonState.add( 0, EntityReference.parseEntityReference( "0" ) );
        jsonState.add( 1, EntityReference.parseEntityReference( "1" ) );
        jsonState.add( 2, EntityReference.parseEntityReference( "2" ) );

        assertThat( jsonState.contains( EntityReference.parseEntityReference( "1" ) ), is( true ) );

        assertThat( jsonState.get( 0 ).identity().toString(), equalTo( "0" ) );
        assertThat( jsonState.get( 1 ).identity().toString(), equalTo( "1" ) );
        assertThat( jsonState.get( 2 ).identity().toString(), equalTo( "2" ) );

        assertThat( jsonState.count(), equalTo( 3 ) );

        jsonState.remove( EntityReference.parseEntityReference( "1" ) );

        assertThat( jsonState.count(), equalTo( 2 ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "1" ) ), is( false ) );
        assertThat( jsonState.get( 0 ).identity().toString(), equalTo( "0" ) );
        assertThat( jsonState.get( 1 ).identity().toString(), equalTo( "2" ) );

        jsonState.add( 2, EntityReference.parseEntityReference( "1" ) );

        assertThat( jsonState.count(), equalTo( 3 ) );

        jsonState.add( 0, EntityReference.parseEntityReference( "A" ) );
        jsonState.add( 0, EntityReference.parseEntityReference( "B" ) );
        jsonState.add( 0, EntityReference.parseEntityReference( "C" ) );

        assertThat( jsonState.count(), equalTo( 6 ) );

        assertThat( jsonState.get( 0 ).identity().toString(), equalTo( "C" ) );
        assertThat( jsonState.get( 1 ).identity().toString(), equalTo( "B" ) );
        assertThat( jsonState.get( 2 ).identity().toString(), equalTo( "A" ) );

        assertThat( jsonState.contains( EntityReference.parseEntityReference( "C" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "B" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "A" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "0" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "2" ) ), is( true ) );
        assertThat( jsonState.contains( EntityReference.parseEntityReference( "1" ) ), is( true ) );

        List<String> refList = new ArrayList<>();
        for( EntityReference ref : jsonState )
        {
            refList.add( ref.identity().toString() );
        }
        assertThat( refList.isEmpty(), is( false ) );
        assertArrayEquals( new String[]
        {
            "C", "B", "A", "0", "2", "1"
        }, refList.toArray() );
    }
}
