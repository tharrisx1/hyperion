package com.dottydingo.hyperion.service.pipeline.context;

import com.dottydingo.service.endpoint.context.ContextBuilder;
import com.dottydingo.service.endpoint.context.EndpointRequest;
import com.dottydingo.service.endpoint.context.EndpointResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class HyperionContextBuilder extends ContextBuilder<HyperionContext,EndpointRequest,EndpointResponse>
{
    @Override
    protected HyperionContext createContextInstance(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        return new HyperionContext();
    }
}