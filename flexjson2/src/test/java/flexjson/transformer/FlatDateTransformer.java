package flexjson.transformer;

import flexjson.BasicType;
import flexjson.TypeContext;

import java.util.Calendar;
import java.util.Date;

/**
 * This is a transformer that flattens a date inline with it's object context.
 * If it is not in an Object context then it creates its own.
 */
public class FlatDateTransformer extends AbstractTransformer implements Defer {

    public void transform(Object o) {

        boolean setContext = false;

        TypeContext typeContext = getContext().peekTypeContext();

        if (typeContext == null || typeContext.getBasicType() != BasicType.OBJECT) {
            typeContext = getContext().writeOpenObject();
            setContext = true;
        }

        Date date = (Date) o;
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        if (!typeContext.isFirst()) getContext().writeComma();
        typeContext.setFirst(false);
        getContext().writeName(fieldName("Month"));
        getContext().transform(c.get(Calendar.MONTH));

        getContext().writeComma();
        getContext().writeName(fieldName("Day"));
        getContext().transform(c.get(Calendar.DAY_OF_MONTH));

        getContext().writeComma();
        getContext().writeName(fieldName("Year"));
        getContext().transform(c.get(Calendar.YEAR));

        if (setContext) {
            getContext().writeCloseObject();
        }

    }

    private String fieldName(String suffix) {
        TypeContext typeContext = getContext().peekTypeContext();
        String propertyName =  typeContext.getPropertyName();
        if( propertyName == null || propertyName.trim().equals("")) {
            return suffix.toLowerCase();
        } else {
            return propertyName + suffix;
        }

    }
}
