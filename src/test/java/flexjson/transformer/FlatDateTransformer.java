package flexjson.transformer;

import flexjson.BasicType;
import flexjson.TypeContext;

import java.util.Calendar;
import java.util.Date;

/**
 * This is a transformer that flattens a date inline with it's object context.
 * If it is not in an Object context then it creates its own.
 */
public class FlatDateTransformer extends AbstractTransformer {

    String prefix = "";

    public FlatDateTransformer(String prefix) {
        this.prefix = prefix;
    }

    public void transform(Object o) {

        boolean setContext = false;

        TypeContext typeContext = getContext().peekTypeContext();
        String propertyName = typeContext != null ? typeContext.getPropertyName() : "";
        if(prefix.trim().equals("")) prefix = propertyName;

        if (typeContext == null || typeContext.getBasicType() != BasicType.OBJECT) {
            typeContext = getContext().writeOpenObject();
            setContext = true;
        }

        Date date = (Date) o;
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        if (!typeContext.isFirst()) getContext().writeComma();
        typeContext.increment();
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
        if( prefix.trim().equals("")) {
            return suffix.toLowerCase();
        } else {
            return prefix + suffix;
        }
    }

    @Override
    public Boolean isInline() {
        return Boolean.TRUE;
    }
}
