package flexjson;

import flexjson.factories.*;

import java.util.*;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;

public class ObjectBinder {

    private LinkedList<Object> objectStack = new LinkedList<Object>();
    private Path currentPath = new Path();
    private Map<Class,ObjectFactory> factories;
    private Map<Path,ObjectFactory> pathFactories = new HashMap<Path,ObjectFactory>();

    public ObjectBinder() {
        factories = new HashMap<Class,ObjectFactory>();
        factories.put( Object.class, new BeanObjectFactory() );
        factories.put( Collection.class, new ListObjectFactory() );
        factories.put( List.class, new ListObjectFactory() );
        factories.put( Set.class, new SetObjectFactory() );
        factories.put( SortedSet.class, new SortedSetObjectFactory() );
        factories.put( Map.class, new MapObjectFactory() );
        factories.put( Integer.class, new IntegerObjectFactory() );
        factories.put( int.class, new IntegerObjectFactory() );
        factories.put( Float.class, new FloatObjectFactory() );
        factories.put( float.class, new FloatObjectFactory() );
        factories.put( Short.class, new ShortObjectFactory() );
        factories.put( short.class, new ShortObjectFactory() );
        factories.put( Long.class, new LongObjectFactory() );
        factories.put( long.class, new LongObjectFactory() );
        factories.put( Byte.class, new ByteObjectFactory() );
        factories.put( byte.class, new ByteObjectFactory() );
        factories.put( Boolean.class, new BooleanObjectFactory() );
        factories.put( boolean.class, new BooleanObjectFactory() );
        factories.put( Character.class, new CharacterObjectFactory() );
        factories.put( char.class, new CharacterObjectFactory() );
        factories.put( Enum.class, new EnumObjectFactory() );
        factories.put( Date.class, new DateObjectFactory() );
        factories.put( String.class, new StringObjectFactory() );
        factories.put( Array.class, new ArrayObjectFactory() );
    }

    public ObjectBinder use(Path path, ObjectFactory factory) {
        pathFactories.put( path, factory );
        return this;
    }

    public ObjectBinder use(Class clazz, ObjectFactory factory) {
        factories.put( clazz, factory );
        return this;
    }

    public Path getCurrentPath() {
        return currentPath;
    }

    public Object bind( Object input ) {
        return this.bind( input, null );
    }

    public Object bind( Object input, Type targetType ) {
        if( input == null ) {
            return null;
        } else if( input instanceof Map ) {
            Class targetClass = findClassName( (Map)input, getTargetClass(targetType) );
            return findFactoryFor( targetClass ).instantiate( this, input, targetType, targetClass);
        } else if( input instanceof List) {
            if( targetType instanceof ParameterizedType ) {
                return convert( input, targetType );
            } else {
                Object inner = ((List)input).get(0);
                if( inner instanceof Map ) {
                    return convert( input, new JSONParameterizedType( List.class, findClassName( (Map)inner, null ) ) );
                } else {
                    return convert( input, targetType );
                }
            }
        } else if( input instanceof Number ) {
            return input;
        } else if( input instanceof String ) {
            return input;
        } else if( input instanceof Enum ) {
            return input;
        } else if( input instanceof Boolean ) {
            return input;
        } else {
            throw new IllegalArgumentException("Missing classname for path " + currentPath + ".  Cannot covert to object." );
        }
    }

    public <T extends Collection<Object>> T bindIntoCollection(Collection value, T target, Type targetType) {
        Type valueType = null;
        if( targetType instanceof ParameterizedType) {
            valueType = ((ParameterizedType)targetType).getActualTypeArguments()[0];
        }
        getCurrentPath().enqueue("values");
        for( Object obj : value ) {
            target.add( bind( obj, valueType ) );
        }
        getCurrentPath().pop();
        return target;
    }

    public Object bindIntoMap(Map input, Map<Object, Object> result, Type keyType, Type valueType) {
        for( Object inputKey : input.keySet() ) {
            currentPath.enqueue("keys");
            Object key = bind( inputKey, keyType );
            currentPath.pop();
            currentPath.enqueue("values");
            Object value = bind( input.get(inputKey), valueType );
            currentPath.pop();
            result.put( key, value );
        }
        return result;
    }

    public JSONException cannotConvertValueToTargetType(Object value, Class targetType) {
        return new JSONException( String.format("%s:  Can not convert %s into %s", currentPath, value.getClass().getName(), targetType.getClass().getName() ) );
    }

    public Object bindIntoObject(Map jsonOwner, Object target, Type targetType) {
        try {
            objectStack.add( target );
            BeanInfo info = Introspector.getBeanInfo( target.getClass() );
            for( PropertyDescriptor descriptor : info.getPropertyDescriptors() ) {
                Object value = findFieldInJson( jsonOwner, descriptor );
                if( value != null ) {
                    currentPath.enqueue( descriptor.getName() );
                    Method setMethod = descriptor.getWriteMethod();
                    if( setMethod != null ) {
                        Type[] types = setMethod.getGenericParameterTypes();
                        if( types.length == 1 ) {
                            Type paramType = types[0];
                            setMethod.invoke( objectStack.getLast(), convert( value, resolveParameterizedTypes( paramType, targetType ) ) );
                        } else {
                            throw new JSONException(currentPath + ":  Expected a single parameter for method " + target.getClass().getName() + "." + setMethod.getName() + " but got " + types.length );
                        }
                    } else {
                        try {
                            Field field = target.getClass().getDeclaredField( descriptor.getName() );
                            field.setAccessible( true );
                            if( value instanceof Map ) {
                                field.set( target, convert(value, findClassName( (Map)value, getTargetClass( field.getGenericType() ) ) ) );
                            } else {
                                field.set( target, convert( value, field.getGenericType() ) );
                            }
                        } catch (NoSuchFieldException e) {
                            // ignore must not be there.
                        }
                    }
                    currentPath.pop();
                }
            }
            return objectStack.removeLast();
        } catch (IllegalAccessException e) {
            throw new JSONException(currentPath + ":  Could not access the no-arg constructor for " + target.getClass().getName(), e);
        } catch (InvocationTargetException ex ) {
            throw new JSONException(currentPath + ":  Exception while trying to invoke setter method.", ex );
        } catch (IntrospectionException e) {
            throw new JSONException(currentPath + ":  Could not inspect " + target.getClass().getName(), e );
        }
    }

    private Object convert(Object value, Type targetType) {
        Class targetClass = getTargetClass( targetType );
        ObjectFactory factory = findFactoryFor( targetClass );
        if( factory != null ) {
            return factory.instantiate(this, value, targetType, targetClass);
        } else {
            throw new JSONException( String.format( "%s:  Cannot instantiate abstract class or interface %s", currentPath, targetClass.getName() ) );
        }
    }

    private Class getTargetClass(Type targetType) {
        if( targetType == null ) {
            return null;
        } else if( targetType instanceof Class ) {
            return (Class)targetType;
        } else if( targetType instanceof ParameterizedType ) {
            return (Class)((ParameterizedType)targetType).getRawType();
        } else if( targetType instanceof GenericArrayType ) {
            return Array.class;
        } else if( targetType instanceof WildcardType ) {
            return null; // nothing you can do about these.  User will have to specify this with use()
        } else if( targetType instanceof TypeVariable ) {
            return null; // nothing you can do about these.  User will have to specify this with use()
        } else {
            throw new JSONException(currentPath + ":  Unknown type " + targetType );
        }
    }

    private Type resolveParameterizedTypes(Type genericType, Type targetType) {
        if( genericType instanceof Class ) {
            return genericType;
        } else if( genericType instanceof ParameterizedType ) {
            return genericType;
        } else if( genericType instanceof TypeVariable ) {
            return targetType;
        } else if( genericType instanceof WildcardType ) {
            return targetType;
        } else if( genericType instanceof GenericArrayType ) {
            return ((GenericArrayType)genericType).getGenericComponentType();
        } else {
            throw new JSONException( currentPath + ":  Unknown generic type " + genericType + ".");
        }
    }


    public Class findClassName( Map map, Class targetType ) throws JSONException {
        if( !pathFactories.containsKey( currentPath ) ) {
            return useMostSpecific( targetType, findClassInMap( map, null ) );
        } else {
            return null;
        }
    }

    protected Class useMostSpecific(Class classFromTarget, Class typeFound) {
        if( classFromTarget != null && typeFound != null ) {
            return typeFound.isAssignableFrom( classFromTarget ) ? classFromTarget : typeFound;
        } else if( typeFound != null ) {
            return typeFound;
        } else if( classFromTarget != null ) {
            return classFromTarget;
        } else {
            return Map.class;
        }
    }

    protected Class findClassInMap( Map map, Class override ) {
        if( override == null ) {
            String classname = (String)map.remove("class");
            try {
                if( classname != null ) {
                    return Class.forName( classname );
                } else {
                    return null;
                }
            } catch( ClassNotFoundException e ) {
                throw new JSONException( String.format( "%s:  Could not load %s", currentPath, classname ), e );
            }
        } else {
            return override;
        }
    }

    private ObjectFactory findFactoryFor(Class targetType) {
        ObjectFactory factory = pathFactories.get( currentPath );
        if( factory == null ) {
            if( targetType.isArray() ) return factories.get(Array.class);
            return findFactoryByTargetClass(targetType);
        }
        return factory;
    }

    private ObjectFactory findFactoryByTargetClass(Class targetType) {
        ObjectFactory factory;
        factory = factories.get( targetType );
        if( factory == null && targetType.getSuperclass() != null ) {
            for( Class intf : targetType.getInterfaces() ) {
                factory = findFactoryByTargetClass( intf );
                if( factory != null ) return factory;
            }
            return findFactoryByTargetClass( targetType.getSuperclass() );
        } else {
            return factory;
        }
    }

    protected Object instantiate( Class clazz ) {
        try {
            Constructor constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible( true );
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new JSONException(currentPath + ":There was an exception trying to instantiate an instance of " + clazz.getName(), e );
        } catch (IllegalAccessException e) {
            throw new JSONException(currentPath + ":There was an exception trying to instantiate an instance of " + clazz.getName(), e );
        } catch (InvocationTargetException e) {
            throw new JSONException(currentPath + ":There was an exception trying to instantiate an instance of " + clazz.getName(), e );
        } catch (NoSuchMethodException e) {
            throw new JSONException(currentPath + ": " + clazz.getName() + " lacks a no argument constructor.  Flexjson will instantiate any protected, private, or public no-arg constructor.", e );
        }
    }

    private Object findFieldInJson( Map map, PropertyDescriptor descriptor ) {
        Object value = map.get( descriptor.getName() );
        if( value == null ) {
            String field = descriptor.getName();
            value = map.get( upperCase(field) );
        }

        return value;
    }

    private String upperCase(String field) {
        return Character.toUpperCase( field.charAt(0) ) + field.substring(1);
    }

}
