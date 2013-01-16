package flexjson;

import flexjson.model.Account;
import flexjson.model.AccountType;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;

public class JSONDeserializerObjectFactoryTest {

    @Test
    public void testOverrideEnumObjectFactory() {
        Account account = new JSONDeserializer<Account>().use(Enum.class, new OrdinalObjectFactory()).deserialize("{'id': '5', 'accountNumber': '1234567-123', 'accountType': " + AccountType.Savings.ordinal() + "}", Account.class);
        assertEquals( AccountType.Savings, account.getAccountType() );
    }

    public class OrdinalObjectFactory implements ObjectFactory {
        public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {
            try {
                if( value instanceof Number ) {
                    Enum[] values = (Enum[])((Class) targetType).getDeclaredMethod("values").invoke(targetType);
                    for( Enum e : values ) {
                        if( e.ordinal() == ((Number)value).intValue() ) {
                            return e;
                        }
                    }
                }
                throw new JSONException( String.format("%s:  Don't know how to convert %s to enumerated constant of %s", context.getCurrentPath(), value, targetType ) );
            } catch (IllegalAccessException e) {
                throw new JSONException( String.format("%s:  Could not access %s.values()", context.getCurrentPath(), targetType ), e );
            } catch (InvocationTargetException e) {
                throw new JSONException( String.format("%s:  Could not invoke %s.values()", context.getCurrentPath(), targetType ), e );
            } catch (NoSuchMethodException e) {
                throw new JSONException( String.format("%s:  Could not find method %s.values()", context.getCurrentPath(), targetType ), e );
            }
        }
    }

}
