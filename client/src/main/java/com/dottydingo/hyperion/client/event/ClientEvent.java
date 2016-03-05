package com.dottydingo.hyperion.client.event;

import com.dottydingo.hyperion.client.RequestMethod;

import java.io.Serializable;

/**
 * An event generated by the client calling the service
 */
public class ClientEvent implements Serializable
{
    private static final long serialVersionUID = 6888151339603349427L;

    private String host;
    private String entity;
    private RequestMethod requestMethod;
    private long duration;
    private boolean error;

    /**
     * Construct an event with the supplied parameters.
     * @param host The base URL for the host being called
     * @param entity The entity being called
     * @param requestMethod The request method
     * @param duration The duration fo the call
     * @param error a flag indicating if an error was returned
     */
    public ClientEvent(String host, String entity, RequestMethod requestMethod, long duration, boolean error)
    {
        this.host = host;
        this.entity = entity;
        this.requestMethod = requestMethod;
        this.duration = duration;
        this.error = error;
    }

    /**
     * Return the host URL being called
     * @return The host URL
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Return the entity being called
     * @return The entity
     */
    public String getEntity()
    {
        return entity;
    }

    /**
     * Return the request method
     * @return The request method
     */
    public RequestMethod getRequestMethod()
    {
        return requestMethod;
    }

    /**
     * Return the call duration
     * @return The duration
     */
    public long getDuration()
    {
        return duration;
    }

    /**
     * Return the flag indicating if there was an error making this call
     * @return True if there was an error, flase otherwise
     */
    public boolean isError()
    {
        return error;
    }
}
