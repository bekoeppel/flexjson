package flexjson;

import java.util.Map;

public class StaticClassLocator implements ClassLocator {
    private Class target;

    public StaticClassLocator(Class clazz) {
        target = clazz;
    }

    public Class locate(Map map, Path currentPath) {
        return target;
    }
}
