package flexjson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class BeanProperty {
    private String name;
    private BeanAnalyzer bean;
    private Class propertyType;
    protected Field property;
    protected Method readMethod;
    protected Method writeMethod;
    protected Map<Class<?>, Method> writeMethods = new HashMap<Class<?>,Method>();

    public BeanProperty(String name, BeanAnalyzer bean) {
        this.name = name;
        this.bean = bean;
        this.property = bean.getDeclaredField( name );
    }

    public BeanProperty(Field property, BeanAnalyzer bean ) {
        this.name = property.getName();
        this.bean = bean;
        this.property = property;
        this.propertyType = property.getType();
    }

    public String getName() {
        return name;
    }

    public Field getProperty() {
        return property;
    }

    public Class getPropertyType() {
        return propertyType;
    }

    public Method getReadMethod() {
        if( readMethod == null && bean.getSuperBean() != null && bean.getSuperBean().hasProperty( name ) ) {
            return bean.getSuperBean().getProperty( name ).getReadMethod();
        } else {
            return readMethod;
        }
    }

    public Method getWriteMethod() {
        if( writeMethod == null ) {
            writeMethod = writeMethods.get( propertyType );
            if( writeMethod == null && bean.getSuperBean() != null && bean.getSuperBean().hasProperty( name ) ) {
                return bean.getSuperBean().getProperty( name ).getWriteMethod();
            }
        }
        return writeMethod;
    }

    public Collection<Method> getWriteMethods() {
        return writeMethods.values();
    }

    public void addWriteMethod(Method method) {
        Class clazz = method.getParameterTypes()[0];
        if( propertyType == null ) {
            propertyType = clazz;
        }
        writeMethods.put( clazz, method );
        method.setAccessible(true);
    }

    public void setReadMethod(Method method) {
        if( propertyType == null ) {
            propertyType = method.getReturnType();
            readMethod = method;
            readMethod.setAccessible(true);
        } else if( propertyType == method.getReturnType() ) {
            readMethod = method;
            readMethod.setAccessible(true);
        }
    }

    public Boolean isAnnotated() {
        if( readMethod != null ) {
            if (readMethod.isAnnotationPresent(JSON.class)) {
                return readMethod.getAnnotation(JSON.class).include();
            }
        } else if( property != null ) {
            if (property.isAnnotationPresent(JSON.class)) {
                return property.getAnnotation(JSON.class).include();
            }
        }
        return null;
    }

    public Object getValue( Object instance ) throws InvocationTargetException, IllegalAccessException {
        if( readMethod != null ) {
            return getReadMethod().invoke(instance, (Object[]) null);
        } else if( property != null ) {
            return property.get( instance );
        } else {
            return null;
        }
    }

}
