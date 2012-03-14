package flexjson.factories;

import flexjson.JsonNumber;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class BigDecimalFactory implements ObjectFactory {

    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        if( value instanceof JsonNumber) {
            return ((JsonNumber)value).toBigDecimal();
        } else {
            return new BigDecimal( value.toString() );
        }
    }
}
