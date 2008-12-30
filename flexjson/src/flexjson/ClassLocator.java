package flexjson;

import java.util.Map;

public interface ClassLocator {
    public Class locate(Map map, Path currentPath) throws ClassNotFoundException;
}
