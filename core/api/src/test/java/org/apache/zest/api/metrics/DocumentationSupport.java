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

package org.apache.zest.api.metrics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.zest.api.injection.scope.Service;

public class DocumentationSupport
{
    // START SNIPPET: common
    @Service
    private MetricsProvider provider;
    // END SNIPPET: common

    public void forDocumentationOnly()
    {
        // START SNIPPET: gauge
        final BlockingQueue queue = new LinkedBlockingQueue( 20 );
        // END SNIPPET: gauge
        // START SNIPPET: gauge
        MetricsGaugeFactory gaugeFactory = provider.createFactory( MetricsGaugeFactory.class );
        MetricsGauge<Integer> gauge = gaugeFactory.registerGauge( "Sample Gauge", new MetricsGauge<Integer>()
        {
            @Override
            public Integer value()
            {
                return queue.size();
            }
        } );
        // END SNIPPET: gauge

        // START SNIPPET: counter
        MetricsCounterFactory counterFactory = provider.createFactory( MetricsCounterFactory.class );
        MetricsCounter counter = counterFactory.createCounter( "Sample Counter" );
        // END SNIPPET: counter

        // START SNIPPET: histogram
        MetricsHistogramFactory histoFactory = provider.createFactory( MetricsHistogramFactory.class );
        MetricsHistogram histogram = histoFactory.createHistogram( "Sample Histogram" );
        // END SNIPPET: histogram

        // START SNIPPET: meter
        MetricsMeterFactory meterFactory = provider.createFactory( MetricsMeterFactory.class );
        MetricsMeter meter = meterFactory.createMeter( "Sample Meter" );
        // END SNIPPET: meter

        // START SNIPPET: timer
        MetricsTimerFactory timerFactory = provider.createFactory( MetricsTimerFactory.class );
        MetricsTimer timer = timerFactory.createTimer( "Sample Timer" );
        // END SNIPPET: timer

        // START SNIPPET: healthcheck
        MetricsHealthCheckFactory healthFactory = provider.createFactory( MetricsHealthCheckFactory.class );
        MetricsHealthCheck healthCheck = healthFactory.registerHealthCheck(
            "Sample Healthcheck",
            new MetricsHealthCheck()
            {
                @Override
                public Result check()
                    throws Exception
                {
                    ServiceStatus status = pingMyService();
                    return new Result( status.isOk(), status.getErrorMessage(), status.getException() );
                }
            } );
        // END SNIPPET: healthcheck

    }

    private ServiceStatus pingMyService()
    {
        return new ServiceStatus();
    }

    private static class ServiceStatus
    {
        String errorMessage;
        Exception exception;

        public String getErrorMessage()
        {
            return errorMessage;
        }

        public Exception getException()
        {
            return exception;
        }

        public boolean isOk()
        {
            return errorMessage.equals( "OK" );
        }
    }
}
