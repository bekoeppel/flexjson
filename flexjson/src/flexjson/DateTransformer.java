package flexjson;

import java.text.SimpleDateFormat;

public class DateTransformer implements Transformer {
    SimpleDateFormat simpleDateFormatter;


    public DateTransformer( String dateFormat ) {
        simpleDateFormatter = new SimpleDateFormat( dateFormat );
    }


    public String transform(Object value) {
        return simpleDateFormatter.format( value );
    }
}
