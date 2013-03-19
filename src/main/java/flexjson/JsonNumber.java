package flexjson;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonNumber extends Number {
    private String input;

    public JsonNumber(String input) {
        this.input = input;
    }

    @Override
    public int intValue() {
        return toInteger();
    }

    @Override
    public long longValue() {
        return toLong();
    }

    @Override
    public float floatValue() {
        return toFloat();
    }

    @Override
    public double doubleValue() {
        return toDouble();
    }

    public BigInteger toBigInteger() {
        if( isHex() ) {
            return new BigInteger( input.substring(2), 16 );
        } else if( isOctal() ) {
            return new BigInteger( input.substring(1), 8 );
        } else {
            return new BigInteger( input );
        }
    }

    public Double toDouble() {
        return Double.parseDouble( input );
    }

    public Short toShort() {
        if( isHex() ) {
            return Short.parseShort( input.substring(2), 16 );
        } else if( isOctal() ) {
            return Short.parseShort(input.substring(1), 8);
        } else {
            return Short.parseShort( input );
        }
    }

    public Integer toInteger() {
        if( isHex() ) {
            return Integer.parseInt(input.substring(2), 16);
        } else if( isOctal() ) {
            return Integer.parseInt(input.substring(1), 8);
        } else {
            return Integer.parseInt(input);
        }
    }

    public Float toFloat() {
        return Float.parseFloat( input );
    }

    public Long toLong() {
        if( isHex() ) {
            return Long.parseLong(input.substring(2), 16);
        } else if( isOctal() ) {
            return Long.parseLong(input.substring(1), 8);
        } else {
            return Long.parseLong(input);
        }
    }

    public Byte toByte() {
        if( isHex() ) {
            return Byte.parseByte(input.substring(2), 16);
        } else if( isOctal() ) {
            return Byte.parseByte(input.substring(1), 8);
        } else {
            return Byte.parseByte(input);
        }
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal( input );
    }

    private boolean isHex() {
        return input.startsWith("0x");
    }

    private boolean isOctal() {
        return input.length() > 1 && input.charAt(0) == '0' && Character.isDigit( input.charAt(1) );
    }

    public boolean isDecimal() {
        return input.contains(".");
    }
}
