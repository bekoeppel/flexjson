package flexjson.transformer;

import java.text.SimpleDateFormat;

/**
 * User: brandongoodin
 * Date: Dec 12, 2007
 * Time: 11:20:39 PM
 */
public class DateTransformer extends AbstractTransformer {

    SimpleDateFormat simpleDateFormatter;

    public DateTransformer(String dateFormat) {
        simpleDateFormatter = new SimpleDateFormat(dateFormat);
    }


    public void transform(Object value) {
        getContext().writeQuoted(simpleDateFormatter.format(value));
    }

}
