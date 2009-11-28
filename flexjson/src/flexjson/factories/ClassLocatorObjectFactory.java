package flexjson.factories;

import flexjson.ObjectFactory;
import flexjson.ClassLocator;
import flexjson.ObjectBinder;
import flexjson.JSONException;

import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

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
                return context.bindIntoObject( (Map)value,  target, clazz );
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
