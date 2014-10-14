package flexjson;

import java.math.BigDecimal;
import java.math.BigInteger;

public class JsonNumber extends Number {
    private String input;

    final String Digits = "(\\p{Digit}+)";
    final String HexDigits = "(\\p{XDigit}+)";
    // an exponent is 'e' or 'E' followed by an optionally
    // signed decimal integer.
    final String Exp = "[eE][+-]?" + Digits;
    final String fpRegex =
            ("[\\x00-\\x20]*" +  // Optional leading "whitespace"
                    "[+-]?(" + // Optional sign character
                    "NaN|" +           // "NaN" string
                    "Infinity|" +      // "Infinity" string

                    // A decimal floating-point string representing a finite positive
                    // number without a leading sign has at most five basic pieces:
                    // Digits . Digits ExponentPart FloatTypeSuffix
                    //
                    // Since this method allows integer-only strings as input
                    // in addition to strings of floating-point literals, the
                    // two sub-patterns below are simplifications of the grammar
                    // productions from section 3.10.2 of
                    // The Java™ Language Specification.

                    // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
                    "(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

                    // . Digits ExponentPart_opt FloatTypeSuffix_opt
                    "(\\.(" + Digits + ")(" + Exp + ")?)|" +

                    // Hexadecimal strings
                    "((" +
                    // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "(\\.)?)|" +

                    // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
                    "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

                    ")[pP][+-]?" + Digits + "))" +
                    "[fFdD]?))" +
                    "[\\x00-\\x20]*");// Optional trailing "whitespace"

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
        if (isHex()) {
            return new BigInteger(input.substring(2), 16);
        } else if (isOctal()) {
            return new BigInteger(input.substring(1), 8);
        } else {
            return new BigInteger(input);
        }
    }

    public Double toDouble() {
        return Double.parseDouble(input);
    }

    public Short toShort() {
        if (isHex()) {
            return Short.parseShort(input.substring(2), 16);
        } else if (isOctal()) {
            return Short.parseShort(input.substring(1), 8);
        } else {
            return Short.parseShort(input);
        }
    }

    public Integer toInteger() {
        if (isHex()) {
            return Integer.parseInt(input.substring(2), 16);
        } else if (isOctal()) {
            return Integer.parseInt(input.substring(1), 8);
        } else {
            return Integer.parseInt(input);
        }
    }

    public Float toFloat() {
        return Float.parseFloat(input);
    }

    public Long toLong() {
        if (isHex()) {
            return Long.parseLong(input.substring(2), 16);
        } else if (isOctal()) {
            return Long.parseLong(input.substring(1), 8);
        } else {
            return Long.parseLong(input);
        }
    }

    public Byte toByte() {
        if (isHex()) {
            return Byte.parseByte(input.substring(2), 16);
        } else if (isOctal()) {
            return Byte.parseByte(input.substring(1), 8);
        } else {
            return Byte.parseByte(input);
        }
    }

    public BigDecimal toBigDecimal() {
        return new BigDecimal(input);
    }

    private boolean isHex() {
        return input.startsWith("0x");
    }

    private boolean isOctal() {
        return input.length() > 1 && input.charAt(0) == '0' && Character.isDigit(input.charAt(1));
    }

    public boolean isDecimal() {
        return input.matches(fpRegex);
    }

    public boolean isLong() {
        return input.matches("\\-?\\d+");
    }
}
