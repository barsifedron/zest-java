/*  Copyright 2008 Edward Yakop.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.struts2;

import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.Module;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import static org.qi4j.library.struts2.Constants.SERVLET_ATTRIBUTE;
import org.qi4j.spi.structure.ApplicationSPI;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * @author edward.yakop@gmail.com
 */
public abstract class Qi4jApplicationBootstrapListener
    implements ServletContextListener
{
    private static final Logger LOG = Logger.getLogger( Qi4jApplicationBootstrapListener.class.getName() );

    private ApplicationSPI application;

    public final void contextInitialized( ServletContextEvent sce )
    {
        LOG.info( "Qi4j Plugin: Initializing" );

        ServletContext context = sce.getServletContext();
        application = createNewApplication( context );

        if( application != null )
        {
            Module module = getQi4jStrutsModule( application );
            context.setAttribute( SERVLET_ATTRIBUTE, module );

            try
            {
                application.activate();
            }
            catch( Exception e )
            {
                throw new IllegalStateException( e );
            }
        }
        else
        {
            throw new IllegalStateException( "None of the assembly creation methods returned a non-null assembler" );
        }
        LOG.info( "... initialized qi4j-struts integration successfully" );
    }

    /**
     * @param anApplication Qi4j application.
     * @return Qi4j struts module.
     */
    protected abstract Module getQi4jStrutsModule( Application anApplication );


    private ApplicationSPI createNewApplication( ServletContext aContext )
    {
        Energy4Java is = new Energy4Java();

        // Try create assembler
        final ApplicationAssembler assembler = createAssembler();
        if( assembler != null )
        {
            try
            {
                return is.newApplication( assembler );
            }
            catch( AssemblyException e )
            {
                throw new IllegalStateException( e );
            }
        }

        return null;
    }

    /**
     * Override this method to create an application assembler.
     *
     * @return An application assembler.
     */
    protected ApplicationAssembler createAssembler()
    {
        return null;
    }

    public final void contextDestroyed( ServletContextEvent sce )
    {
        try
        {
            application.passivate();
        }
        catch( Exception e )
        {
            throw new IllegalStateException( e );
        }
    }
}
