package flexjson.transformer;

/**
 * A Boolean Transformer that writes out as String values in the JSON stream.  This is
 * great for writing out things like "Yes", "No", "Y", "N", "T", "F", etc.  It doesn't
 * handle numeric values.  This was written as a separate Transformer so the performance
 * of {@link BooleanTransformer} isn't impacted.
 */
public class BooleanAsStringTransformer extends AbstractTransformer {

    private String truthValue;
    private String falseValue;

    public BooleanAsStringTransformer(String truthValue, String falseValue) {
        this.truthValue = truthValue;
        this.falseValue = falseValue;
    }

    public void transform(Object object) {
        getContext().writeQuoted(((Boolean) object) ? truthValue : falseValue);
    }
}
