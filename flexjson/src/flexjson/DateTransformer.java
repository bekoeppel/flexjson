package flexjson;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.reflect.Type;

public class DateTransformer implements Transformer, ObjectFactory {
    SimpleDateFormat simpleDateFormatter;

    public DateTransformer( String dateFormat ) {
        simpleDateFormatter = new SimpleDateFormat( dateFormat );
    }

    public String transform(Object value) {
        return simpleDateFormatter.format( value );
    }

    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        try {
            return simpleDateFormatter.parse( value.toString() );
        } catch (ParseException e) {
            throw new JSONException(String.format( "Failed to parse %s with %s pattern.", value, simpleDateFormatter.toPattern() ), e );
        }
    }
}
