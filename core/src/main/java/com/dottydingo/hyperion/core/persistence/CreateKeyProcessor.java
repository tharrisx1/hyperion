package com.dottydingo.hyperion.core.persistence;

import com.dottydingo.hyperion.api.ApiObject;

import java.io.Serializable;

/**
 */
public interface CreateKeyProcessor<C extends ApiObject,ID extends Serializable>
{
    ID lookup(C item, PersistenceContext persistenceContext);
}
