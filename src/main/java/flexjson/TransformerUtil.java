package flexjson;

import flexjson.transformer.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TransformerUtil {

    private static final TypeTransformerMap defaultTransformers = new TypeTransformerMap() {
        {
            // define all standard type transformers
            Transformer transformer = new NullTransformer();
            putTransformer(void.class, new TransformerWrapper(transformer));

            transformer = new ObjectTransformer();
            putTransformer(Object.class, new TransformerWrapper(transformer));

            transformer = new ClassTransformer();
            putTransformer(Class.class, new TransformerWrapper(transformer));

            transformer = new BooleanTransformer();
            putTransformer(boolean.class, new TransformerWrapper(transformer));
            putTransformer(Boolean.class, new TransformerWrapper(transformer));

            transformer = new NumberTransformer();
            putTransformer(Number.class, new TransformerWrapper(transformer));

            putTransformer(Integer.class, new TransformerWrapper(transformer));
            putTransformer(int.class, new TransformerWrapper(transformer));

            putTransformer(Long.class, new TransformerWrapper(transformer));
            putTransformer(long.class, new TransformerWrapper(transformer));

            putTransformer(Double.class, new TransformerWrapper(transformer));
            putTransformer(double.class, new TransformerWrapper(transformer));

            putTransformer(Float.class, new TransformerWrapper(transformer));
            putTransformer(float.class, new TransformerWrapper(transformer));

            putTransformer(BigDecimal.class, new TransformerWrapper(transformer));
            putTransformer(BigInteger.class, new TransformerWrapper(transformer));

            transformer = new StringTransformer();
            putTransformer(String.class, new TransformerWrapper(transformer));

            transformer = new CharacterTransformer();
            putTransformer(Character.class, new TransformerWrapper(transformer));
            putTransformer(char.class, new TransformerWrapper(transformer));

            transformer = new BasicDateTransformer();
            putTransformer(Date.class, new TransformerWrapper(transformer));

            transformer = new DefaultCalendarTransformer();
            putTransformer(Calendar.class, new TransformerWrapper(transformer));

            transformer = new EnumTransformer();
            putTransformer(Enum.class, new TransformerWrapper(transformer));

            transformer = new IterableTransformer();
            putTransformer(Iterable.class, new TransformerWrapper(transformer));

            transformer = new MapTransformer();
            putTransformer(Map.class, new TransformerWrapper(transformer));

            transformer = new ArrayTransformer();
            putTransformer(Arrays.class, new TransformerWrapper(transformer));

            try {
                Class hibernateProxy = Class.forName("org.hibernate.proxy.HibernateProxy");
                putTransformer(hibernateProxy, new TransformerWrapper(new HibernateTransformer()));
            } catch (ClassNotFoundException ex) {
                // no hibernate so ignore.
            }

            locked = true;

        }
    };

    public static TypeTransformerMap getDefaultTypeTransformers() {
        return defaultTransformers;
    }

}
