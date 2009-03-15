package flexjson;

import java.lang.reflect.Type;

public interface ObjectFactory {
    public Object instantiate(Object value, Type targetType);
}
