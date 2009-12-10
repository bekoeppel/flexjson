package flexjson.factories;

import flexjson.ObjectFactory;
import flexjson.ClassLocator;
import flexjson.ObjectBinder;
import flexjson.JSONException;

import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.Collection;

public class ClassLocatorObjectFactory implements ObjectFactory {

    private ClassLocator locator;

    public ClassLocatorObjectFactory(ClassLocator locator) {
        this.locator = locator;
    }

    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        Class clazz = null;
        try {
            clazz = locator.locate( (Map)value, context.getCurrentPath() );
            if( clazz != null ) {
                Constructor constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                Object target = constructor.newInstance();
                if( target instanceof Collection) {
                    return context.bindIntoCollection( (Collection)value, (Collection<Object>)target, targetType );
                } else if( target instanceof Map ) {
                    if( targetType instanceof ParameterizedType ) {
                        ParameterizedType ptype = (ParameterizedType) targetType;
                        return context.bindIntoMap( (Map)value, (Map<Object,Object>)target, ptype.getActualTypeArguments()[0], ptype.getActualTypeArguments()[1] );
                    } else {
                        return context.bindIntoMap(  (Map)value, (Map<Object,Object>)target, null, null );
                    }
                } else {
                    return context.bindIntoObject( (Map)value,  target, clazz );
                }
            } else {
                return null;
            }
        } catch( ClassNotFoundException ex )  {
            throw new JSONException( String.format("%s: Could not find class %s", context.getCurrentPath(), ex.getMessage() ), ex);
        } catch (IllegalAccessException e) {
            throw new JSONException( String.format("%s: Could not instantiate class %s", context.getCurrentPath(), clazz.getName() ), e );
        } catch (InstantiationException e) {
            throw new JSONException( String.format("%s: Problem while instantiating class %s", context.getCurrentPath(), clazz.getName() ), e );
        } catch (NoSuchMethodException e) {
            throw new JSONException( String.format("%s: Could not find a no-arg constructor for %s", context.getCurrentPath(), clazz.getName() ), e );
        } catch (InvocationTargetException e) {
            throw new JSONException( String.format("%s: Problem while invoking the no-arg constructor for %s", context.getCurrentPath(), clazz.getName() ), e );
        }
    }
}
