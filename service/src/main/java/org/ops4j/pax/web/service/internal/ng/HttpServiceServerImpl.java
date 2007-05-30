package org.ops4j.pax.web.service.internal.ng;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ops4j.pax.web.service.HttpServiceConfiguration;
import java.util.Set;
import java.util.HashSet;
import javax.servlet.Servlet;

class HttpServiceServerImpl implements HttpServiceServer
{

    private static final Log m_logger = LogFactory.getLog( HttpServiceServerImpl.class );
    
    private HttpServiceConfiguration m_configuration;
    private State m_state;
    private JettyFactory m_jettyFactory;
    private JettyServer m_jettyServer;
    private Set<HttpServiceServerListener> m_listeners;

    HttpServiceServerImpl( final JettyFactory jettyFactory )
    {
        m_jettyFactory = jettyFactory;
        m_configuration = null;
        m_state = new Unconfigured();
        m_listeners = new HashSet<HttpServiceServerListener>();
    }

    public synchronized void start() {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "starting server: " + this + ". current state: " + m_state);
        }
        m_state.start();
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "started server: " + this + ". current state: " + m_state);
        }
    }

    public synchronized void stop() {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "stopping server: " + this + ". current state: " + m_state);
        }
        m_state.stop();
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "stopped server: " + this + ". current state: " + m_state);
        }
    }

    public synchronized void configure( final HttpServiceConfiguration configuration )
    {
        if ( configuration == null )
        {
            throw new IllegalArgumentException( "configuration == null" );
        }
        m_configuration = configuration;
        m_state.configure();
    }

    public HttpServiceConfiguration getConfiguration()
    {
        return m_configuration;
    }

    public void addListener( HttpServiceServerListener listener )
    {
        if ( listener == null)
        {
            throw new IllegalArgumentException( "listener == null" );
        }
        m_listeners.add( listener );
    }

    public void addServlet( final String alias, final Servlet servlet )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "adding servlet: [" + alias + "] -> " + servlet );
        }
        m_state.addServlet( alias, servlet);
    }

    public boolean isStarted()
    {
        return m_state instanceof Started;
    }

    void notifyListeners( HttpServiceServerEvent event )
    {
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "notifying listeners for event : " + event + " on " + this );
        }
        for ( HttpServiceServerListener listener : m_listeners)
        {
            listener.stateChanged( event );
        }
        if( m_logger.isDebugEnabled() )
        {
            m_logger.debug( "notify done for event : " + event + " on " + this );
        }
    }

    private void processConfiguration()
    {
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "processing configuration: " + m_configuration );
        }
        if( m_logger.isInfoEnabled() )
        {
            m_logger.info( "configuration processed" );
        }
    }

    private interface State
    {
        void start();
        void stop();
        void configure();
        void addServlet( String alias, Servlet servlet );
    }

    private class Started implements State
    {
        public void start()
        {
            throw new IllegalStateException( "server is already started. must be stopped first." );
        }

        public void stop()
        {
            m_jettyServer.stop();
            m_state = new Stopped();
            notifyListeners( HttpServiceServerEvent.STOPPED );
        }

        public void configure()
        {
            HttpServiceServerImpl.this.stop();
            processConfiguration();
            HttpServiceServerImpl.this.start();
        }

        public void addServlet( final String alias, final Servlet servlet )
        {
            System.out.println("add servlet");
            m_jettyServer.addServlet( alias, servlet);
        }
    }

    private class Stopped implements State
    {
        public void start()
        {
            m_jettyServer = m_jettyFactory.createServer();
            if ( m_configuration.isHttpEnabled() )
            {
                m_jettyServer.addConnector( m_jettyFactory.createConnector( m_configuration.getHttpPort() ) );
            }
            // TODO handle ssl port
            m_jettyServer.addContext();
            m_jettyServer.start();
            m_state = new Started();
            notifyListeners( HttpServiceServerEvent.STARTED );
        }

        public void stop()
        {
            // do nothing. already stopped
        }

        public void configure()
        {
            processConfiguration();
            notifyListeners( HttpServiceServerEvent.CONFIGURED );
        }

        public void addServlet( String alias, Servlet servlet )
        {
            //do nothing if server is not started
            System.out.println("add servlet " + this);
        }
    }

    private class Unconfigured extends Stopped
    {
        public void start()
        {
            throw new IllegalStateException( "server is not yet configured." );
        }

        public void configure()
        {
            processConfiguration();
            m_state = new Stopped();
            notifyListeners( HttpServiceServerEvent.CONFIGURED );
        }
    }

    // TODO verify synchronization 

}
