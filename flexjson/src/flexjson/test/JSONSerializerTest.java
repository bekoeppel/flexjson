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

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;

import java.util.*;

import flexjson.JSONSerializer;
import flexjson.test.mock.*;

public class JSONSerializerTest extends TestCase {

    private Person charlie, ben, pedro;
    private Map colors;
    private List people;
    private Network network;
    private Zipcode pedroZip;

    @SuppressWarnings({"unchecked"})
    public void setUp() {
        Address home = new Address("4132 Pluto Drive", "Atlanta", "Ga", new Zipcode("33913") );
        Address work = new Address("44 Planetary St.", "Neptune", "Milkiway", new Zipcode("30328-0764") );
        pedroZip = new Zipcode("49404");
        Address pedroHome = new Address(" 12 Acrelândia Way", "Rio de Janeiro", "Brazil", pedroZip);
        Address pedroWork = new Address(" 12 Acrelândia Way", "Rio de Janeiro", "Brazil", pedroZip );

        Phone pagerPhone = new Phone( PhoneNumberType.PAGER, "404 555-1234");
        Phone cellPhone = new Phone( PhoneNumberType.MOBILE, "770 777 5432");
        Phone pedroPhone = new Phone( PhoneNumberType.MOBILE, "123 555 2323");

        Calendar pedroCal = Calendar.getInstance();
        pedroCal.set( 1980, 4, 12, 11, 45);
        pedro = new Person("Pedro", "Neves", pedroCal.getTime(), pedroHome, pedroWork );
        pedro.getPhones().add( pedroPhone );

        Calendar cal = Calendar.getInstance();
        cal.set(1976, 3, 21, 8, 11);
        charlie = new Person("Charlie", "Hubbard", cal.getTime(), home, work );
        charlie.getPhones().add( pagerPhone );
        charlie.getPhones().add( cellPhone );

        charlie.getHobbies().add( "Shorting volatile stocks" );
        charlie.getHobbies().add( "Fixing Horse Races" );
        charlie.getHobbies().add( "Taking dives in the 3rd round" );

        Address benhome = new Address("8735 Hilton Way", "Chattanooga", "Tn", new Zipcode("82742") );
        Address benwork = new Address("44 Planetary St.", "Neptune", "Milkiway", new Zipcode("12345") );
        
        Calendar benCal = Calendar.getInstance();
        benCal.set(1978, 7, 5, 8, 11);
        ben = new Person("Ben", "Hubbard", benCal.getTime(), benhome, benwork );
        ben.getHobbies().add( "Purse snatching" );
        ben.getHobbies().add( "Running sweat shops" );
        ben.getHobbies().add( "Fixing prices" );

        colors = new HashMap();

        colors.put("blue", "#0000ff");
        colors.put("green", "#00ff00" );
        colors.put("black", "#000000" );
        colors.put("grey", "#888888" );
        colors.put("yellow", "#00ffff" );
        colors.put("purple", "#ff00ff" );
        colors.put("white", "#ffffff" );

        people = new ArrayList();
        people.add( charlie );
        people.add( ben );
        people.add( pedro );

        network = new Network("My Network", charlie, ben );
    }

    public void testObject() {
        JSONSerializer serializer = new JSONSerializer();

        String charlieJson = serializer.serialize( charlie );
        assertStringValue( Person.class.getName(), charlieJson );
        assertAttribute( "firstname", charlieJson );
        assertStringValue("Charlie", charlieJson );
        assertAttribute( "lastname", charlieJson );
        assertStringValue("Hubbard", charlieJson );
        assertAttribute( "work", charlieJson );
        assertAttribute( "home", charlieJson );
        assertAttribute( "street", charlieJson );
        assertStringValue( Address.class.getName(), charlieJson );
        assertAttribute( "zipcode", charlieJson );
        assertStringValue( Zipcode.class.getName(), charlieJson );
        assertAttributeMissing("person", charlieJson);

        assertAttributeMissing( "phones", charlieJson );
        assertStringValueMissing( Phone.class.getName(), charlieJson );
        assertAttributeMissing( "hobbies", charlieJson );

        JSONSerializer benSerializer = new JSONSerializer();
        benSerializer.exclude("home", "work");
        String benJson = benSerializer.serialize( ben );
        assertStringValue( Person.class.getName(), benJson );
        assertAttribute( "firstname", benJson );
        assertStringValue( "Ben", benJson );
        assertAttribute( "lastname", benJson );
        assertStringValue( "Hubbard", benJson );
        assertAttribute( "birthdate", benJson );

        assertStringValueMissing( Address.class.getName(), benJson );
        assertAttributeMissing( "work", benJson );
        assertAttributeMissing( "home", benJson );
        assertAttributeMissing( "street", benJson );
        assertAttributeMissing( "city", benJson );
        assertAttributeMissing( "state", benJson );
        assertStringValueMissing( Zipcode.class.getName(), benJson );
        assertAttributeMissing( "zipcode", benJson );
        assertStringValueMissing( Phone.class.getName(), benJson );
        assertAttributeMissing( "hobbies", benJson );
        assertAttributeMissing("person", benJson);

        serializer.exclude("home.zipcode", "work.zipcode" );

        String json2 = serializer.serialize(charlie);
        assertStringValue( Person.class.getName(), json2 );
        assertAttribute( "work", json2 );
        assertAttribute( "home", json2 );
        assertAttribute( "street", json2 );
        assertStringValue( Address.class.getName(), json2 );
        assertAttributeMissing( "zipcode", json2 );
        assertAttributeMissing( "phones", json2 );
        assertStringValueMissing( Zipcode.class.getName(), json2 );
        assertStringValueMissing( Phone.class.getName(), json2 );
        assertAttributeMissing( "hobbies", json2 );
        assertAttributeMissing( "type", json2 );
        assertStringValueMissing( "PAGER", json2 );

        serializer.include("hobbies").exclude("phones.areaCode", "phones.exchange", "phones.number" );

        String json3 = serializer.serialize(charlie);
        assertStringValue( Person.class.getName(), json3 );
        assertAttribute( "work", json3 );
        assertAttribute( "home", json3 );
        assertAttribute( "street", json3 );
        assertStringValue( Address.class.getName(), json3 );
        assertAttribute( "phones", json3 );
        assertAttribute( "phoneNumber", json3 );
        assertStringValue( Phone.class.getName(), json3 );
        assertAttribute( "hobbies", json3 );

        assertAttributeMissing( "zipcode", json3 );
        assertAttributeMissing( Zipcode.class.getName(), json3 );
        assertAttributeMissing( "areaCode", json3 );
        assertAttributeMissing( "exchange", json3 );
        assertAttributeMissing( "number", json3 );
        assertAttribute( "type", json3 );
        assertStringValue( "PAGER", json3 );

        assertTrue( json3.startsWith("{") );
        assertTrue( json3.endsWith("}") );
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testMap() {
        JSONSerializer serializer = new JSONSerializer();
        String colorsJson = serializer.serialize( colors );
        for( Iterator i = colors.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            assertAttribute( entry.getKey().toString(), colorsJson );
            assertStringValue( entry.getValue().toString(), colorsJson );
        }
        assertTrue( colorsJson.startsWith("{") );
        assertTrue( colorsJson.endsWith("}") );
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testCollection() {
        JSONSerializer serializer = new JSONSerializer();
        String colorsJson = serializer.serialize( colors.values() );
        for( Iterator i = colors.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
            assertAttributeMissing( entry.getKey().toString(), colorsJson );
            assertStringValue( entry.getValue().toString(), colorsJson );
        }
        assertTrue( colorsJson.startsWith("[") );
        assertTrue( colorsJson.endsWith("]") );
    }

    public void testString() {
        assertSerializedTo("Hello", "\"Hello\"");
        assertSerializedTo("Hello World", "\"Hello World\"");
        assertSerializedTo("Hello\nWorld", "\"Hello\\nWorld\"");
        assertSerializedTo("Hello 'Charlie'", "\"Hello 'Charlie'\"");
        assertSerializedTo("Hello \"Charlie\"", "\"Hello \\\"Charlie\\\"\"");
    }

    public void testListOfObjects() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.serialize( people );

        assertStringValue( Person.class.getName(), peopleJson );
        assertAttribute( "firstname", peopleJson );
        assertStringValue("Charlie", peopleJson);
        assertStringValue("Ben", peopleJson);
        assertAttribute( "lastname", peopleJson );
        assertStringValue("Hubbard", peopleJson);
        assertStringValue( Address.class.getName(), peopleJson );
        assertStringValue("Pedro", peopleJson);
        assertStringValue("Neves", peopleJson);

        serializer = new JSONSerializer().exclude("home", "work");
        peopleJson = serializer.serialize( people );

        assertStringValue( Person.class.getName(), peopleJson );
        assertAttribute( "firstname", peopleJson );
        assertStringValue("Charlie", peopleJson);
        assertStringValue("Ben", peopleJson);
        assertAttribute( "lastname", peopleJson );
        assertStringValue("Hubbard", peopleJson);
        assertStringValueMissing( Address.class.getName(), peopleJson );
    }

    public void testDeepIncludes() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.include("people.hobbies").serialize( network );

        assertAttribute("name", peopleJson);
        assertStringValue( "My Network", peopleJson );
        assertAttribute("firstname", peopleJson );
        assertStringValue( "Charlie", peopleJson );
        assertStringValue( "Ben", peopleJson );
        assertAttribute( "lastname", peopleJson );
        assertStringValue( "Hubbard", peopleJson );
        assertAttribute( "hobbies", peopleJson );
        assertStringValue( "Purse snatching", peopleJson );
    }

    public void testDates() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.exclude("home", "work").serialize( charlie );
        assertAttribute( "firstname", peopleJson );
        assertStringValue( "Charlie", peopleJson );
        assertNumber( charlie.getBirthdate().getTime(), peopleJson );
        assertStringValueMissing( "java.util.Date", peopleJson );
    }

    public void testRootName() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.serialize("people", people);
        assertTrue( peopleJson.startsWith( "{\"people\":") );
    }

    public void testSetIncludes() {
        JSONSerializer serializer = new JSONSerializer();
        serializer.setIncludes( Arrays.asList( "people.hobbies", "phones", "home", "people.resume" ) );

        List includes = serializer.getIncludes();
        assertFalse( includes.isEmpty() );
        assertEquals( 4, includes.size() );
        assertTrue( includes.contains("people.hobbies") );
        assertTrue( includes.contains("people.resume") );
        assertTrue( includes.contains("phones") );
        assertTrue( includes.contains("home") );
    }

    public void testI18n() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.include("work","home").serialize( pedro );

        assertAttribute("work", json);
        assertAttribute("home", json);

        assertEquals( 2, occurs("Acrelândia", json) );
    }

    public void testDeepSerialization() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.deepSerialize( network );

        assertAttribute("name", peopleJson);
        assertStringValue( "My Network", peopleJson );
        assertAttribute("firstname", peopleJson );
        assertStringValue( "Charlie", peopleJson );
        assertStringValue( "Ben", peopleJson );
        assertAttribute( "lastname", peopleJson );
        assertStringValue( "Hubbard", peopleJson );
        assertAttributeMissing( "hobbies", peopleJson ); // there is an annotation that explicitly excludes this!
        assertStringValueMissing( "Purse snatching", peopleJson );
    }

    public void testDeepSerializationWithIncludeOverrides() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.include("people.hobbies").deepSerialize( network );

        assertAttribute("firstname", peopleJson );
        assertStringValue( "Charlie", peopleJson );
        assertAttribute( "hobbies", peopleJson );
        assertStringValue( "Purse snatching", peopleJson );
        assertStringValue( "Running sweat shops", peopleJson );
        assertStringValue( "Fixing prices", peopleJson );
    }

    public void testDeepSerializationWithExcludes() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.exclude("people.work").deepSerialize( network );

        assertAttribute("firstname", peopleJson );
        assertStringValue( "Charlie", peopleJson );
        assertAttributeMissing("work", peopleJson);
        assertStringValue("4132 Pluto Drive", peopleJson);
        assertAttribute("home", peopleJson);
        assertAttribute("phones", peopleJson);
    }

    public void testDeepSerializationCycles() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.deepSerialize( people );

        assertAttribute("zipcode", json);
        assertEquals( 2, occurs( pedroZip.getZipcode(), json ) );
        assertAttributeMissing("person", json);
    }

    private int occurs(String str, String json) {
        int current = 0;
        int count = 0;
        while( current >= 0 ) {
            current = json.indexOf( str, current );
            if( current > 0 ) {
                count++;
                current += str.length();
            }
        }
        return count;
    }

    private void assertAttributeMissing(String attribute, String json) {
        assertAttribute( attribute, json, false );
    }

    private void assertAttribute(String attribute, String peopleJson) {
        assertAttribute( attribute, peopleJson, true );
    }

    private void assertAttribute(String attribute, String peopleJson, boolean isPresent ) {
        if( isPresent ) {
            assertTrue( "'" + attribute + "' attribute is missing", peopleJson.contains("\"" + attribute + "\":" ) );
        } else {
            assertFalse( "'" + attribute + "' attribute is present when it's not expected.", peopleJson.contains("\"" + attribute + "\":" ) );
        }
    }

    private void assertStringValue( String value, String json, boolean isPresent ) {
        if( isPresent ) {
            assertTrue( "'" + value + "' value is missing", json.contains( "\"" + value + "\"" ) );
        } else {
            assertFalse( "'" + value + "' value is present when it's not expected.", json.contains( "\"" + value + "\"" ) );
        }
    }

    private void assertNumber(Number time, String json) {
        assertTrue( time + " is missing as a number.", json.contains( time.toString() ) );
    }

    private void assertStringValueMissing( String value, String json ) {
        assertStringValue( value, json, false );
    }

    private void assertStringValue( String value, String json ) {
        assertStringValue( value, json, true );
    }

    private void assertSerializedTo(String original, String expected) {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.serialize( original );
        assertEquals( expected, json );
    }

    public void tearDown() {
    }

    public static Test suite() {
        return new TestSuite( JSONSerializerTest.class );
    }

    public static void main(String[] args) {
        TestRunner.run( suite () );
    }
}
