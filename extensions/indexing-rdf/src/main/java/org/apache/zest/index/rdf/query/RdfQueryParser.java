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

package org.apache.zest.index.rdf.query;

import java.util.Map;
import java.util.function.Predicate;
import org.apache.zest.api.composite.Composite;
import org.apache.zest.api.query.grammar.OrderBy;

public interface RdfQueryParser
{
    String constructQuery( Class<?> resultType,
                           Predicate<Composite> whereClause,
                           OrderBy[] orderBySegments,
                           Integer firstResult,
                           Integer maxResults,
                           Map<String, Object> variables
    );
}
