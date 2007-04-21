/**
 * Copyright 2007 Charlie Hubbard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package flexjson.test;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MockPhone {
    private PhoneNumberType type;
    private String areaCode;
    private String exchange;
    private String number;

    private static final Pattern PHONE_PATTERN = Pattern.compile("\\(?(\\d{3})\\)?[\\s-](\\d{3})[\\s-](\\d{4})");

    public MockPhone( PhoneNumberType aType, String number) {
        this.type = aType;
        Matcher matcher = PHONE_PATTERN.matcher( number );
        if( matcher.matches() ) {
            this.areaCode = matcher.group(1);
            this.exchange = matcher.group(2);
            this.number = matcher.group(3);
        } else {
            throw new IllegalArgumentException( number + " does not match one of these formats: (xxx) xxx-xxxx, xxx xxx-xxxx, or xxx xxx xxxx.");
        }
    }

    public String getAreaCode() {
        return areaCode;
    }

    public PhoneNumberType getType() {
        return type;
    }

    public void setType(PhoneNumberType type) {
        this.type = type;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getPhoneNumber() {
        return "(" + areaCode + ") " + exchange + "-" + number;
    }
}
