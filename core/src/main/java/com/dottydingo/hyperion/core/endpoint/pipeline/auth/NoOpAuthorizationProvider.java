package com.dottydingo.hyperion.core.endpoint.pipeline.auth;

import com.dottydingo.hyperion.api.exception.AuthorizationException;
import com.dottydingo.hyperion.core.endpoint.HyperionContext;

/**
 */
public class NoOpAuthorizationProvider implements AuthorizationProvider
{
    @Override
    public AuthorizationContext authorize(HyperionContext context) throws AuthorizationException
    {
        return new NoOpAuthorizationContext(context.getUserContext());
    }
}
