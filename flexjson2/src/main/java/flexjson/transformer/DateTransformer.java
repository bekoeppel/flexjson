package flexjson.transformer;

import flexjson.ObjectBinder;
import flexjson.JSONException;
import flexjson.ObjectFactory;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.lang.reflect.Type;

/**
 * User: brandongoodin
 * Date: Dec 12, 2007
 * Time: 11:20:39 PM
 */
public class DateTransformer extends AbstractTransformer implements ObjectFactory {

    private String dateFormat;
    private ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>();

    public DateTransformer(String dateFormat) {
        this.dateFormat = dateFormat;
    }


    public void transform(Object value) {
        getContext().writeQuoted(getFormatter().format(value));
    }

    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
        try {
            return getFormatter().parse(value.toString());
        } catch (ParseException e) {
            throw new JSONException(String.format( "Failed to parse %s with %s pattern.", value, dateFormat ), e );
        }
    }

    private SimpleDateFormat getFormatter() {
        if( formatter.get() == null ) {
            formatter.set( new SimpleDateFormat(dateFormat) );
        }
        return formatter.get();
    }
}
