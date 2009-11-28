package flexjson.factories;

import flexjson.ObjectFactory;
import flexjson.ObjectBinder;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;
import java.util.Map;
import java.util.HashMap;

public class MapObjectFactory implements ObjectFactory {
    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        ParameterizedType ptype = (ParameterizedType) targetType;
        if( ptype != null ) {
            return bindMap( context, (Map)value, new HashMap<Object,Object>(), ptype.getActualTypeArguments()[0], ptype.getActualTypeArguments()[1] );
        } else {
            return bindMap( context, (Map)value, new HashMap<Object,Object>(), null, null );
        }
    }

    private Map bindMap(ObjectBinder context, Map input, Map<Object,Object> result, Type keyType, Type valueType) {
        for( Object inputKey : input.keySet() ) {
            context.getCurrentPath().enqueue("keys");
            Object key = context.bind( inputKey, keyType );
            context.getCurrentPath().pop();
            context.getCurrentPath().enqueue("values");
            Object value = context.bind( input.get(inputKey), valueType );
            context.getCurrentPath().pop();
            result.put( key, value );
        }
        return result;
    }
}
