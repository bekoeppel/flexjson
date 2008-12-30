package flexjson;

import java.util.Map;
import java.util.HashMap;

public class JSONDeserializer<T> {

    private ObjectBinder binder;

    public JSONDeserializer() {
        binder = new ObjectBinder();
    }

    public T deserialize( String input ) {
        return (T)binder.bind( new JSONTokener( input ).nextValue() );
    }

    public JSONDeserializer<T> use( String path, ClassLocator locator ) {
        binder.use( path, locator );
        return this;
    }

    public JSONDeserializer<T> use( String path, Class clazz ) {
        binder.use( path, clazz );
        return this;
    }
}
