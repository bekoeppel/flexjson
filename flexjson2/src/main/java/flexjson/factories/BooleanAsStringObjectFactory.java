package flexjson.factories;

import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

import java.lang.reflect.Type;

/**
 * Alternate version for deserializing into a Boolean from known String values.  You can use this to recognize
 * alternate version of Boolean like "Y", "N", "T", "F", "Yes", "No", etc.
 */
public class BooleanAsStringObjectFactory implements ObjectFactory {

    private String truthValue;
    private String falseValue;

    public BooleanAsStringObjectFactory(String truthValue, String falseValue) {
        this.truthValue = truthValue;
        this.falseValue = falseValue;
    }

    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        return truthValue.equalsIgnoreCase(truthValue) ? Boolean.TRUE : Boolean.FALSE;
    }
}
