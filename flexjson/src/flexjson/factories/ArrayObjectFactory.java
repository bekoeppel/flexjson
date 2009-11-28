package flexjson.factories;

import flexjson.ObjectFactory;
import flexjson.ObjectBinder;

import java.lang.reflect.Type;
import java.lang.reflect.Array;
import java.util.List;

public class ArrayObjectFactory implements ObjectFactory {

    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        List list = (List) value;
        Object array = Array.newInstance( targetClass.getComponentType(), list.size() );
        for( int i = 0; i < list.size(); i++ ) {
            Object v = context.bind( list.get(i), targetClass.getComponentType() );
            Array.set( array, i, v );
        }
        return array;
    }
}
