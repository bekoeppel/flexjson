package flexjson;

import java.util.*;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

public class ObjectBinder<T> {

    private List<DateFormat> dateFormats;
    private LinkedList<Object> objectStack = new LinkedList<Object>();
    private Path currentPath = new Path();
    private Map<Class,ObjectFactory> factories;
    private Map<Path,ClassLocator> locators = new HashMap<Path,ClassLocator>();

    public ObjectBinder() {
        factories = new HashMap<Class,ObjectFactory>();
        factories.put( Set.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Collection ) {
                    return bindCollection((Collection)value, new HashSet() );
                } else {
                    HashSet set = new HashSet();
                    set.add( bind( value ) );
                    return set;
                }
            }
        } );
        factories.put( List.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Collection ) {
                    return bindCollection((Collection)value, new ArrayList() );
                } else {
                    ArrayList set = new ArrayList();
                    set.add( bind( value ) );
                    return set;
                }
            }
        });
        factories.put( SortedSet.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Collection ) {
                    return bindCollection( (Collection)value, new TreeSet() );
                } else {
                    TreeSet set = new TreeSet();
                    set.add( bind( value ) );
                    return set;
                }
            }
        });
        factories.put( Float.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Number ) {
                    return ((Number)value).floatValue();
                } else {
                    throw cannotConvertValueToTargetType( value, Float.class );
                }
            }
        });
        factories.put( Short.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Number ) {
                    return ((Number)value).shortValue();
                } else {
                    throw cannotConvertValueToTargetType( value, Short.class );
                }
            }
        });
        factories.put( Long.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Number ) {
                    return ((Number)value).longValue();
                } else {
                    throw cannotConvertValueToTargetType( value, Long.class );
                }
            }
        });
        factories.put( Byte.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                if( value instanceof Number ) {
                    return ((Number)value).byteValue();
                } else {
                    throw cannotConvertValueToTargetType( value, Byte.class );
                }
            }
        });
        factories.put( Character.class, new ObjectFactory() {
            public Object instantiate(Object value) {
                return value.toString().charAt(0);
            }
        });
        dateFormats = new ArrayList<DateFormat>();
        dateFormats.add( DateFormat.getDateTimeInstance() );
        dateFormats.add(DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG ) );
        dateFormats.add(DateFormat.getDateTimeInstance( DateFormat.MEDIUM, DateFormat.MEDIUM ) );
        dateFormats.add(DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT ) );
        dateFormats.add( new SimpleDateFormat("EEE MMM d hh:mm:ss z yyyy") );
        dateFormats.add( new SimpleDateFormat("MM/dd/yy hh:mm:ss a"));
        dateFormats.add( new SimpleDateFormat("MM/dd/yy") );
        dateFormats.add( new SimpleDateFormat("dd/MM/yy HH:mm:ss"));
        dateFormats.add( new SimpleDateFormat("dd/MM/yy") );
        dateFormats.add( new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"));
        dateFormats.add( new SimpleDateFormat("yyyy/MM/dd"));
        // Wed Oct 15 10:27:02 EDT 2008
    }

    private <T extends Collection<Object>> T bindCollection(Collection value, T target) {
        for( Object obj : value ) {
            target.add( bind( obj ) );
        }
        return target;
    }

    public Object bind( Object input ) {
        if( input == null ) {
            return null;
        } else if( input instanceof Map) {
            return bindObject( (Map)input );
        } else if( input instanceof List) {
            return bindArray( (List)input );
        } else if( input instanceof Number ) {
            return input;
        } else if( input instanceof String ) {
            return input;
        } else if( input instanceof Enum ) {
            return input;
        } else if( input instanceof Boolean ) {
            return input;
        } else {
            throw new IllegalArgumentException("Do not know how to bind types " + input.getClass().getName() );
        }
    }

    public ObjectBinder use( String path, ClassLocator locator ) {
        Path p = path != null ? new Path( path.split("\\.") ) : new Path();
        locators.put( p, locator );
        return this;
    }

    public ObjectBinder use( String path, Class clazz ) {
        return use( path, new StaticClassLocator( clazz ) );
    }

    private Object bindObject( Map map ) {
        return bindObject( map, instantiate( findClassName( map, null ) ) ); // todo handle a given type if the user provides one.
    }

    private Object convert(Object value, Class targetType) {
        try {
            if( !targetType.isInterface() && !Modifier.isAbstract( targetType.getModifiers() ) ) {
                if( value.getClass().isAssignableFrom( targetType ) ) {
                    return value;
                } else if( Date.class.isAssignableFrom(targetType) ) {
                    return convertToDate(value, targetType);
                } else if( targetType.isEnum() ) {
                    return convertToEnum( value, targetType );
                } else if( value instanceof Map  && !Map.class.isAssignableFrom(targetType) ) {
                    return bindObject( (Map)value, instantiate( findClassName( (Map)value, targetType ) ) );
                } else {
                    ObjectFactory factory = findFactoryFor( targetType );
                    if( factory != null ) {
                        return factory.instantiate( value );
                    } else {
                        Constructor constructor = targetType.getConstructor( value.getClass() );
                        return constructor.newInstance( value );
                    }
                }
            } else if( targetType.isPrimitive() ) {
                if( value instanceof Number ) {
                    Number num = (Number) value;
                    if( targetType == int.class ) {
                        return num.intValue();
                    } else if( targetType == long.class ) {
                        return num.longValue();
                    } else if( targetType == double.class ) {
                        return num.doubleValue();
                    } else if( targetType == float.class ) {
                        return num.floatValue();
                    } else if( targetType == short.class ) {
                        return num.shortValue();
                    } else if( targetType == byte.class ) {
                        return num.byteValue();
                    }
                }
                return value;
            } else {
                ObjectFactory factory = findFactoryFor( targetType );
                if( factory != null ) {
                    return factory.instantiate( value );
                } else {
                    throw new JSONException( "Cannot instantiate abstract class or interface " + targetType.getName() + " at " + currentPath );
                }
            }
        } catch (InvocationTargetException e) {
            throw new JSONException( currentPath.toString(), e );
        } catch (NoSuchMethodException e) {
            throw new JSONException( currentPath.toString(), e );
        } catch (IllegalAccessException e) {
            throw new JSONException( currentPath.toString(), e );
        } catch (InstantiationException e) {
            throw new JSONException( currentPath.toString(), e );
        }
    }

    protected Class findClassName( Map map, Class targetType ) throws JSONException {
        return useMostSpecific( targetType, findClassInMap( map, findClassByPath( map ) ) );
    }

    protected Class useMostSpecific(Class classFromTarget, Class typeFound) {
        if( classFromTarget != null && typeFound != null ) {
            return typeFound.isAssignableFrom( classFromTarget ) ? classFromTarget : typeFound;
        } else if( typeFound != null ) {
            return typeFound;
        } else if( classFromTarget != null ) {
            return classFromTarget;
        } else {
            throw new JSONException( "Missing classname for path " + currentPath + ".  Cannot covert to object." );
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
                throw new JSONException(currentPath + ":Could not load " + classname, e );
            }
        } else {
            return override;
        }
    }

    protected Class findClassByPath( Map map ) {
        try {
            ClassLocator locator = locators.get( currentPath );
            return locator != null ? locator.locate( map, currentPath ) : null;
        } catch( ClassNotFoundException ex ) {
            throw new JSONException("Class locator for path " + currentPath + " could not locate the right class.", ex);
        }
    }

    private Object convertToEnum(Object value, Class targetType) {
        if( value instanceof String ) {
            return Enum.valueOf( targetType, value.toString() );
        } else {
            throw new JSONException( currentPath + ":  Don't know how to convert " + value + " to enumerated constant of " + targetType );
        }
    }

    private ObjectFactory findFactoryFor(Class targetType) {
        ObjectFactory factory = factories.get( targetType );
        if( factory == null && targetType.getSuperclass() != null ) {
            return findFactoryFor( targetType.getSuperclass() );
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

    public Object bindObject(Map map, Object target) {
        try {
            objectStack.add( target );
            BeanInfo info = Introspector.getBeanInfo( target.getClass() );
            for( PropertyDescriptor descriptor : info.getPropertyDescriptors() ) {
                if( map.containsKey( descriptor.getName() ) ) {
                    Object value = map.get( descriptor.getName() );
                    currentPath.enqueue( descriptor.getName() );
                    Method method = descriptor.getWriteMethod();
                    if( method != null ) {
                        Class[] types = method.getParameterTypes();
                        if( types.length == 1 ) {
                            Class paramType = types[0];
                            method.invoke( objectStack.getLast(), convert( value, paramType ) );
                        } else {
                            throw new JSONException("Expected a parameter for method " + target.getClass().getName() + "." + method.getName() + " but got " + types.length );
                        }
                    } else {
                        try {
                            Field field = target.getClass().getDeclaredField( descriptor.getName() );
                            field.setAccessible( true );
                            field.set( target, convert(value, field.getType() ) );
                        } catch (NoSuchFieldException e) {
                            // ignore must not be there.
                        }
                    }
                    currentPath.pop();
                }
            }
            return objectStack.removeLast();
        } catch (IllegalAccessException e) {
            throw new JSONException(currentPath + ":Could not access the no-arg constructor for " + target.getClass().getName(), e);
        } catch (InvocationTargetException ex ) {
            throw new JSONException(currentPath + ": Exception while trying to invoke setter method.", ex );
        } catch (IntrospectionException e) {
            throw new JSONException(currentPath + ":Could not inspect " + target.getClass().getName(), e );
        }
    }

    private Date convertToDate(Object value, Class targetType) throws InstantiationException, IllegalAccessException {
        if( value instanceof Double ) {
            Date d = (Date)targetType.newInstance();
            d.setTime( ((Double)value).longValue() );
            return d;
        } else if( value instanceof Long ) {
            Date d = (Date)targetType.newInstance();
            d.setTime( (Long)value );
            return d;
        } else if( value instanceof String ) {
            for( DateFormat dateFormat : dateFormats ) {
                try {
                    return dateFormat.parse((String)value);
                } catch( ParseException e ) {
                    // ignore for now.
                }
            }
            throw new JSONException(currentPath + ":Parsing date " + value + " was not recognized as a date format" );
        } else {
            throw new JSONException(currentPath + ":Could not convert " + value.getClass().getName() + " into a Date." );
        }
    }

    private Object bindArray( List input ) {
        return input;
    }

    private JSONException cannotConvertValueToTargetType(Object value, Class targetType) {
        return new JSONException( currentPath + ":  Can not convert " + value.getClass().getName() + " into " + targetType.getClass().getName() );
    }
}
