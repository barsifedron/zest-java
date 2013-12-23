package org.qi4j.bootstrap.builder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.qi4j.api.activation.ActivationEvent;
import org.qi4j.api.activation.ActivationEventListener;
import org.qi4j.api.activation.ActivationEventListenerRegistration;
import org.qi4j.api.activation.ActivationException;
import org.qi4j.api.structure.Application;
import org.qi4j.api.structure.ApplicationDescriptor;
import org.qi4j.bootstrap.ApplicationAssembler;
import org.qi4j.bootstrap.ApplicationAssembly;
import org.qi4j.bootstrap.ApplicationAssemblyFactory;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.Energy4Java;
import org.qi4j.bootstrap.LayerAssembly;

import static org.qi4j.api.activation.ActivationEvent.EventType.ACTIVATED;
import static org.qi4j.api.activation.ActivationEvent.EventType.ACTIVATING;

public class ApplicationBuilder
{
    private final String applicationName;
    private final Map<String, LayerDeclaration> layers = new HashMap<>();

    public ApplicationBuilder( String applicationName )
    {
        this.applicationName = applicationName;
    }

    public Application newApplication()
        throws AssemblyException
    {
        return newApplication( null );
    }

    private Application newApplication( ActivationEventListener activationListener )
        throws AssemblyException
    {
        Energy4Java qi4j = new Energy4Java();
        ApplicationDescriptor model = qi4j.newApplicationModel( new ApplicationAssembler()
        {
            @Override
            public ApplicationAssembly assemble( ApplicationAssemblyFactory factory )
                throws AssemblyException
            {
                ApplicationAssembly assembly = factory.newApplicationAssembly();
                assembly.setName( applicationName );
                HashMap<String, LayerAssembly> createdLayers = new HashMap<>();
                for( Map.Entry<String, LayerDeclaration> entry : layers.entrySet() )
                {
                    LayerAssembly layer = entry.getValue().createLayer( assembly );
                    createdLayers.put( entry.getKey(), layer );
                }
                for( LayerDeclaration layer : layers.values() )
                {
                    layer.initialize( createdLayers );
                }
                return assembly;
            }
        } );
        Application application = model.newInstance( qi4j.api() );
        application.registerActivationEventListener( new ActivationEventListener()
        {

            @Override
            public void onEvent( ActivationEvent event )
            {
                if( event.source() instanceof Application )
                {
                    if( event.type() == ACTIVATING )
                    {
                        beforeActivation();
                    }
                    if( event.type() == ACTIVATED )
                    {
                        afterActivation();
                        ( (ActivationEventListenerRegistration) event.source() ).deregisterActivationEventListener( this );
                    }
                }
            }
        } );
        if( activationListener != null )
        {
            application.registerActivationEventListener( activationListener );
        }
        return application;
    }

    public Application newActivatedApplication()
        throws AssemblyException, ActivationException
    {
        return newActivatedApplication( null );
    }

    private Application newActivatedApplication( ActivationEventListener activationListener )
        throws AssemblyException, ActivationException
    {
        Application application = newApplication( activationListener );
        beforeActivation();
        application.activate();
        afterActivation();
        return application;
    }

    protected void beforeActivation()
    {
    }

    protected void afterActivation()
    {
    }

    public LayerDeclaration withLayer( String layerName )
    {
        LayerDeclaration layerDeclaration = new LayerDeclaration( layerName );
        layers.put( layerName, layerDeclaration );
        return layerDeclaration;
    }

    public static ApplicationBuilder fromJson( String json )
        throws JSONException, AssemblyException
    {
        JSONObject root = new JSONObject( json );
        return fromJson( root );
    }

    public static ApplicationBuilder fromJson( InputStream json )
        throws JSONException, AssemblyException
    {
        JSONObject root = new JSONObject( json );
        return fromJson( root );
    }

    public static ApplicationBuilder fromJson( JSONObject root )
        throws JSONException, AssemblyException
    {
        String applicationName = root.getString( "name" );
        ApplicationBuilder builder = new ApplicationBuilder( applicationName );
        JSONArray layers = root.optJSONArray( "layers" );
        if( layers != null )
        {
            for( int i = 0; i < layers.length(); i++ )
            {
                JSONObject layerObject = layers.getJSONObject( i );
                String layerName = layerObject.getString( "name" );
                LayerDeclaration layerDeclaration = builder.withLayer( layerName );
                JSONArray using = layerObject.optJSONArray( "uses" );
                if( using != null )
                {
                    for( int j = 0; j < using.length(); j++ )
                    {
                        layerDeclaration.using( using.getString( j ) );
                    }
                }
                JSONArray modules = layerObject.optJSONArray( "modules" );
                if( modules != null )
                {
                    for( int k = 0; k < modules.length(); k++ )
                    {
                        JSONObject moduleObject = modules.getJSONObject( k );
                        String moduleName = moduleObject.getString( "name" );
                        ModuleDeclaration moduleDeclaration = layerDeclaration.withModule( moduleName );
                        JSONArray assemblers = moduleObject.optJSONArray( "assemblers" );
                        if( assemblers != null )
                        {
                            for( int m = 0; m < assemblers.length(); m++ )
                            {
                                moduleDeclaration.withAssembler( assemblers.getString( m ) );
                            }
                        }
                    }
                }
            }
        }
        return builder;
    }

    public static void main( String[] args )
        throws JSONException, ActivationException, AssemblyException
    {
        ApplicationBuilder builder = fromJson( System.in );
        Application application = builder.newActivatedApplication();
    }
}
