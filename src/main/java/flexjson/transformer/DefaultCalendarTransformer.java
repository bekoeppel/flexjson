package flexjson.transformer;

import java.util.Calendar;

public class DefaultCalendarTransformer extends AbstractTransformer {

    public void transform(Object object) {
        getContext().write(String.valueOf(((Calendar)object).getTimeInMillis()));
    }
}
