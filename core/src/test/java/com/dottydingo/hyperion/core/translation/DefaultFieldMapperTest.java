package com.dottydingo.hyperion.core.translation;

import com.dottydingo.hyperion.core.persistence.PersistenceContext;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: mark
 * Date: 9/3/12
 * Time: 9:57 PM
 */
public class DefaultFieldMapperTest
{
    private static final TypeMapper clientTypeMapper = new TypeMapper(SimpleClientObject.class);
    private static final TypeMapper persistentTypeMapper = new TypeMapper(SimplePersistentObject.class);

    @Test
    public void testSameNameMapping()
    {
        DefaultFieldMapper<SimpleClientObject,SimplePersistentObject> mapper =
                new DefaultFieldMapper<SimpleClientObject, SimplePersistentObject>("name");

        SimpleClientObject client = new SimpleClientObject();
        client.setName("should be overwritten");

        SimplePersistentObject persistent = new SimplePersistentObject();
        persistent.setName("happy new value");

        ObjectWrapper<SimpleClientObject> clientObjectWrapper = new ObjectWrapper<SimpleClientObject>(client,
                clientTypeMapper);
        ObjectWrapper<SimplePersistentObject> persistentObjectWrapper = new ObjectWrapper<SimplePersistentObject>(persistent,
                persistentTypeMapper);

        PersistenceContext context = new PersistenceContext();
        mapper.convertToClient(persistentObjectWrapper,clientObjectWrapper, context);

        Assert.assertEquals("happy new value",client.getName());

        persistent.setName("Overwrite me");
        client.setName("new value");


        mapper.convertToPersistent(clientObjectWrapper,persistentObjectWrapper,context);
        Assert.assertEquals("new value",persistent.getName());
    }

    @Test
    public void testSparseMapping()
    {
        DefaultFieldMapper<SimpleClientObject,SimplePersistentObject> mapper =
                new DefaultFieldMapper<SimpleClientObject, SimplePersistentObject>("name");

        SimpleClientObject client = new SimpleClientObject();
        client.setName(null);

        SimplePersistentObject persistent = new SimplePersistentObject();
        persistent.setName("Don't overwrite me");

        PersistenceContext context = new PersistenceContext();

        ObjectWrapper<SimpleClientObject> clientObjectWrapper = new ObjectWrapper<SimpleClientObject>(client,
                clientTypeMapper);
        ObjectWrapper<SimplePersistentObject> persistentObjectWrapper = new ObjectWrapper<SimplePersistentObject>(persistent,
                persistentTypeMapper);
        mapper.convertToPersistent(clientObjectWrapper,persistentObjectWrapper,context);
        Assert.assertEquals("Don't overwrite me",persistent.getName());
    }

    @Test
    public void testDifferentNameMapping()
    {
        DefaultFieldMapper<SimpleClientObject,SimplePersistentObject> mapper =
                new DefaultFieldMapper<SimpleClientObject, SimplePersistentObject>("clientOnly","persistentOnly",
                        null);

        SimpleClientObject client = new SimpleClientObject();
        client.setClientOnly("should be overwritten");

        SimplePersistentObject persistent = new SimplePersistentObject();
        persistent.setPersistentOnly("happy new value");

        ObjectWrapper<SimpleClientObject> clientObjectWrapper = new ObjectWrapper<SimpleClientObject>(client,
                clientTypeMapper);
        ObjectWrapper<SimplePersistentObject> persistentObjectWrapper = new ObjectWrapper<SimplePersistentObject>(persistent,
                persistentTypeMapper);

        PersistenceContext context = new PersistenceContext();
        mapper.convertToClient(persistentObjectWrapper,clientObjectWrapper, context);

        Assert.assertEquals("happy new value",client.getClientOnly());

        persistent.setPersistentOnly("Overwrite me");
        client.setClientOnly("new value");


        mapper.convertToPersistent(clientObjectWrapper,persistentObjectWrapper,context);

        Assert.assertEquals("new value",persistent.getPersistentOnly());
    }

    @Test
    public void testWithConverter()
    {
        DefaultFieldMapper<SimpleClientObject,SimplePersistentObject> mapper =
                new DefaultFieldMapper<SimpleClientObject, SimplePersistentObject>("differentType","differentType",
                        new StringConverter());

        SimpleClientObject client = new SimpleClientObject();
        client.setDifferentType("42");

        SimplePersistentObject persistent = new SimplePersistentObject();
        persistent.setDifferentType(22);

        ObjectWrapper<SimpleClientObject> clientObjectWrapper = new ObjectWrapper<SimpleClientObject>(client,
                clientTypeMapper);
        ObjectWrapper<SimplePersistentObject> persistentObjectWrapper = new ObjectWrapper<SimplePersistentObject>(persistent,
                persistentTypeMapper);

        PersistenceContext context = new PersistenceContext();
        mapper.convertToClient(persistentObjectWrapper,clientObjectWrapper, context);

        Assert.assertEquals("22",client.getDifferentType());

        persistent.setDifferentType(99);
        client.setDifferentType("4");


        mapper.convertToPersistent(clientObjectWrapper,persistentObjectWrapper,context);
        Assert.assertEquals(new Integer(4), persistent.getDifferentType());
    }

    private class StringConverter implements ValueConverter<String,Integer>
    {
        @Override
        public String convertToClientValue(Integer persistentValue, PersistenceContext context)
        {
            if(persistentValue == null)
                return null;

            return persistentValue.toString();
        }

        @Override
        public Integer convertToPersistentValue(String clientValue, PersistenceContext context)
        {
            if(clientValue == null)
                return null;

            return Integer.parseInt(clientValue);
        }
    }
}
