package flexjson;

import flexjson.model.State;
import flexjson.transformer.TypeTransformerMap;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class TransformerUtilTest {

    @Test
    public void confirmImmutabilityOfDefaultTypeTransformers() {

        // Default is not modifiable
        TypeTransformerMap typeTransformerMap = TransformerUtil.getDefaultTypeTransformers();
        typeTransformerMap.getTransformer(new State());
        assertFalse("TypeTransformerMap should not contain this key", typeTransformerMap.containsKey(State.class));

    }

    @Test
    public void confirmMutabilityOfChildTypeTransformerMap() {

        TypeTransformerMap freshTypeTransformerMap = new TypeTransformerMap(TransformerUtil.getDefaultTypeTransformers());
        freshTypeTransformerMap.getTransformer(new State());
        assertTrue("TypeTransformerMap should contain this key", freshTypeTransformerMap.containsKey(State.class));

    }

}
