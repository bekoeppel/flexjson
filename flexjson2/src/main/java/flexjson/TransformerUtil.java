package flexjson;

import flexjson.transformer.*;

import java.util.*;
import java.math.BigDecimal;
import java.math.BigInteger;

public class TransformerUtil {

    private static final TypeTransformerMap defaultTransformers = new TypeTransformerMap();

    static {
        // define all standard type transformers
        Transformer transformer = new NullTransformer();
        defaultTransformers.put(null, transformer);

        transformer = new ObjectTransformer();
        defaultTransformers.put(Object.class, transformer);

        transformer = new ClassTransformer();
        defaultTransformers.put(Class.class, transformer);

        transformer = new BooleanTransformer();
        defaultTransformers.put(boolean.class, transformer);
        defaultTransformers.put(Boolean.class, transformer);

        transformer = new NumberTransformer();
        defaultTransformers.put(Number.class, transformer);

        defaultTransformers.put(Integer.class, transformer);
        defaultTransformers.put(int.class, transformer);

        defaultTransformers.put(Long.class, transformer);
        defaultTransformers.put(long.class, transformer);

        defaultTransformers.put(Double.class, transformer);
        defaultTransformers.put(double.class, transformer);

        defaultTransformers.put(Float.class, transformer);
        defaultTransformers.put(float.class, transformer);

        defaultTransformers.put(BigDecimal.class, transformer);
        defaultTransformers.put(BigInteger.class, transformer);

        transformer = new StringTransformer();
        defaultTransformers.put(String.class, transformer);

        transformer = new CharacterTransformer();
        defaultTransformers.put(Character.class, transformer);
        defaultTransformers.put(char.class, transformer);

        transformer = new BasicDateTransformer();
        defaultTransformers.put(Date.class, transformer);

        transformer = new EnumTransformer();
        defaultTransformers.put(Enum.class, transformer);

        transformer = new IterableTransformer();
        defaultTransformers.put(Iterable.class, transformer);

        transformer = new MapTransformer();
        defaultTransformers.put(Map.class, transformer);

        transformer = new NullTransformer();
        defaultTransformers.put(void.class, transformer);

        transformer = new ArrayTransformer();
        defaultTransformers.put(Arrays.class, transformer);

        try {
            Class hibernateProxy = Class.forName("org.hibernate.proxy.HibernateProxy");
            defaultTransformers.put( hibernateProxy, new HibernateTransformer() );
        } catch( ClassNotFoundException ex ) {
            // no hibernate so ignore.
        }


        Collections.unmodifiableMap(defaultTransformers);
    }

    public static TypeTransformerMap getDefaultTypeTransformers() {
        return defaultTransformers;
    }

}
