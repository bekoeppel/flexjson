package flexjson.locators;

import flexjson.ClassLocator;
import flexjson.Path;

import java.util.Map;
import java.util.HashMap;

/**
 * This implementation uses a single field out of the object as the type discriminator.
 * Each unique value of the type field is mapped to a java class using the 
 * {@link TypeLocator#add(Object, Class)} method.
 */
public class TypeLocator<T> implements ClassLocator {

    private String fieldname;
    private Map<T,Class> types = new HashMap<T,Class>();

    public TypeLocator( String fieldname ) {
        this.fieldname = fieldname;
    }

    public TypeLocator add( T value, Class type ) {
        types.put( value, type );
        return this;
    }

    public Class locate(Map map, Path currentPath) throws ClassNotFoundException {
        return types.get( map.get( fieldname ) );
    }
}
