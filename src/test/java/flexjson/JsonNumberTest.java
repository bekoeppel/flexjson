package flexjson;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by charlie on 10/13/14.
 */
public class JsonNumberTest {

    @Test
    public void testJsonNumberFormats() {
        JsonNumber n3_4 = new JsonNumber("3.4");
        JsonNumber n3e5 = new JsonNumber("3E+5");
        JsonNumber n3 = new JsonNumber("3");

        assertTrue("Assert that 3.4 is a decimal", n3_4.isDecimal());
        assertTrue("Assert that 3e+5 is a decimal", n3e5.isDecimal());
        assertTrue("Assert that 3 is a decimal", n3.isDecimal());

        assertFalse("Assert that 3.4 is not a long", n3_4.isLong());
        assertFalse("Assert that 3E+5 is not a long", n3e5.isLong());
        assertTrue("Assert that n3 is a long", n3.isLong());

    }
}
