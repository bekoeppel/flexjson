package flexjson.transformer;

public class ValueTransformer extends AbstractTransformer {
    public void transform(Object object) {
        if( object == null ) {
            getContext().write("null");
            return;
        }
        getContext().writeQuoted( object.toString() );
    }
}
