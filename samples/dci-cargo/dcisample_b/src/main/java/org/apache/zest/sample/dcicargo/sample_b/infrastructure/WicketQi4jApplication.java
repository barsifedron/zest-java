/*
 * Copyright 2011 Marc Grue.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zest.sample.dcicargo.sample_b.infrastructure;

import javax.servlet.http.HttpServletRequest;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.zest.api.Qi4j;
import org.apache.zest.api.injection.scope.Service;
import org.apache.zest.api.injection.scope.Structure;
import org.apache.zest.api.structure.Application;
import org.apache.zest.api.structure.Module;
import org.apache.zest.api.unitofwork.UnitOfWork;
import org.apache.zest.api.unitofwork.UnitOfWorkCompletionException;
import org.apache.zest.api.usecase.UsecaseBuilder;
import org.apache.zest.bootstrap.ApplicationAssembler;
import org.apache.zest.bootstrap.Energy4Java;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.conversion.EntityToDTOService;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.dci.Context;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.model.Queries;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.model.ReadOnlyModel;
import org.apache.zest.sample.dcicargo.sample_b.infrastructure.wicket.page.BaseWebPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WicketQi4jApplication
 *
 * Base Wicket Web Application containing the Zest application.
 */
public class WicketQi4jApplication
    extends WebApplication
{
    public Logger logger = LoggerFactory.getLogger( WicketQi4jApplication.class );

    protected Application qi4jApp;
    protected Module qi4jModule;

    @Structure
    protected Module module;

    @Structure
    protected Qi4j qi4j;

    @Service
    protected EntityToDTOService valueConverter;

    /**
     * Zest Assembler
     *
     * To let the custom application class (DCISampleApplication_x) focus on starting up the
     * Wicket environment, I made a convention of having Zest Assembler files in an 'assembly'
     * folder beside the custom application class.
     *
     * There's always only one application file, but we could split the assemblage into several
     * files ie. one for each layer. In that case, the Assembler file would be distributing to
     * the individual LayerXAssembler classes.
     *
     * If you like, you can also override this method in the custom application class and simply
     * return an instance of YourAssembler:
     * <pre><code>
     * &#64;Override protected ApplicationAssembler getAssembler() {
     *     return new YourAssemblerInAnyPath();
     * }
     * </code></pre>
     */
    protected ApplicationAssembler getAssembler()
        throws Exception
    {
        String appPath = getClass().getCanonicalName();
        String expectedPathFromApplication = ".assembly.Assembler";
        String assemblerPath = appPath.substring( 0, appPath.lastIndexOf( "." ) ) + expectedPathFromApplication;
        try
        {
            return (ApplicationAssembler) Class.forName( assemblerPath ).newInstance();
        }
        catch( ClassNotFoundException e )
        {
            throw new Exception( "Couldn't find Zest assembler in path '" + assemblerPath + "'" );
        }
    }

    protected String defaultLayerName()
    {
        return "BOOTSTRAP";
    }

    protected String defaultModuleName()
    {
        return "BOOTSTRAP-Bootstrap";
    }

    // Override this to bootstrap the wicket application
    protected void wicketInit()
    {
    }

    @Override
    protected void init()
    {
        startQi4j();
        handleUnitOfWork();

        Context.prepareContextBaseClass( module );
        BaseWebPage.prepareBaseWebPageClass( module );
        ReadOnlyModel.prepareModelBaseClass( qi4jModule, qi4j, valueConverter );
        Queries.prepareQueriesBaseClass( module, module );

        wicketInit();
    }

    private void startQi4j()
    {
        try
        {
            logger.info( "Starting Zest application" );
            Energy4Java qi4j = new Energy4Java();
            qi4jApp = qi4j.newApplication( getAssembler() );
            qi4jApp.activate();
            qi4jModule = qi4jApp.findModule( defaultLayerName(), defaultModuleName() );

            // Zest injects @Structure and @Service elements into this application instance
            qi4jModule.injectTo( this );

            logger.info( "Started Zest application" );
        }
        catch( Exception e )
        {
            logger.error( "Could not start Zest application." );
            e.printStackTrace();
            System.exit( 100 );
        }
    }

    private void handleUnitOfWork()
    {
        getRequestCycleListeners().add( new AbstractRequestCycleListener()
        {
            @Override
            public void onBeginRequest( final RequestCycle requestCycle )
            {
                super.onBeginRequest( requestCycle );

                logger.debug( "================================" );
                logger.debug( "REQUEST start" );
                logger.debug( requestCycle.getRequest().toString() );
                logger.debug( requestCycle.getRequest().getRequestParameters().toString() );

                UnitOfWork uow = module.newUnitOfWork( UsecaseBuilder.newUsecase( "REQUEST" ) );
                logger.debug( "  ### NEW " + uow + "   ### MODULE: " + qi4jModule );
            }

            @Override
            public void onEndRequest( final RequestCycle requestCycle )
            {
                UnitOfWork uow = module.currentUnitOfWork();
                if( uow != null )
                {
                    try
                    {
                        if( "POST".equals( ( (HttpServletRequest) requestCycle.getRequest()
                            .getContainerRequest() ).getMethod() ) )
                        {
                            // "Save"
                            logger.debug( "  ### COMPLETE " + uow + "   ### MODULE: " + qi4jModule );
                            uow.complete();
                        }
                        else
                        {
                            // GET requests
                            logger.debug( "  ### DISCARD " + uow + "   ### MODULE: " + qi4jModule );
                            uow.discard();
                        }
                    }
                    catch( UnitOfWorkCompletionException e )
                    {
                        logger.error( "  ### DISCARD " + uow + "   ### MODULE: " + qi4jModule );
                        uow.discard();
                        e.printStackTrace();
                    }
                }
                logger.debug( "REQUEST end" );
                logger.debug( "------------------------------------" );
            }
        } );
    }

    // Since Zest can only add concrete classes in the assembly, we need to implement a (dummy) getHomePage()
    // method here. Override in wicket application class with a real returned page class.
    @Override
    public Class<? extends Page> getHomePage()
    {
        return null;
    }

    @Override
    protected void onDestroy()
    {
        if( qi4jApp == null )
        {
            return;
        }

        try
        {
            logger.info( "Passivating Zest application" );
            qi4jApp.passivate();
        }
        catch( Exception e )
        {
            e.printStackTrace();
        }
    }

    public String appVersion()
    {
        return qi4jApp.version();
    }
}