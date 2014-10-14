package flexjson.transformer;

import java.util.Calendar;

public class DefaultCalendarTransformer extends AbstractTransformer {

    public void transform(Object object) {
        if( object == null ) {
            getContext().write("null");
            return;
        }
        getContext().write(String.valueOf(((Calendar)object).getTimeInMillis()));
    }
}
