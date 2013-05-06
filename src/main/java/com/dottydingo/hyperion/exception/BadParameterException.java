package com.dottydingo.hyperion.exception;

/**
 */
public class BadParameterException extends BadRequestException
{
    public BadParameterException(String message)
    {
        super(message);
    }

    public BadParameterException(String message, Throwable cause)
    {
        super(message, cause);
    }
}