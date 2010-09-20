package flexjson;

import java.lang.reflect.Method;
import java.util.*;

public class BeanProperty {
    private String name;
    private BeanAnalyzer bean;
    private Class propertyType;
    protected Method readMethod;
    protected Method writeMethod;
    protected Map<Class<?>, Method> writeMethods = new HashMap<Class<?>,Method>();

    public BeanProperty(String name, BeanAnalyzer bean) {
        this.name = name;
        this.bean = bean;
    }

    public String getName() {
        return name;
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
}
