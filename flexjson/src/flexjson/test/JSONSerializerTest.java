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

public class JSONSerializerTest extends TestCase {

    private MockPerson charlie, ben;
    private Map colors;
    private List people;
    private MockNetwork network;

    public void setUp() {
        MockAddress home = new MockAddress("4132 Pluto Drive", "Atlanta", "Ga", new MockZipcode("33913") );
        MockAddress work = new MockAddress("44 Planetary St.", "Neptune", "Milkiway", new MockZipcode("30328-0764") );
        MockPhone pagerPhone = new MockPhone("pager", "404 555-1234");
        MockPhone cellPhone = new MockPhone("cell", "770 777 5432");

        Calendar cal = Calendar.getInstance();
        cal.set(1976, 3, 21, 8, 11);
        charlie = new MockPerson("Charlie", "Hubbard", cal.getTime(), home, work );
        charlie.getPhones().add( pagerPhone );
        charlie.getPhones().add( cellPhone );

        charlie.getHobbies().add( "Shorting volatile stocks" );
        charlie.getHobbies().add( "Fixing Horse Races" );
        charlie.getHobbies().add( "Taking dives in the 3rd round" );

        MockAddress benhome = new MockAddress("8735 Hilton Way", "Chattanooga", "Tn", new MockZipcode("82742") );
        MockAddress benwork = new MockAddress("44 Planetary St.", "Neptune", "Milkiway", new MockZipcode("12345") );
        
        Calendar benCal = Calendar.getInstance();
        benCal.set(1978, 7, 5, 8, 11);
        ben = new MockPerson("Ben", "Hubbard", benCal.getTime(), benhome, benwork );
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

        network = new MockNetwork("My Network", charlie, ben );
    }

    public void testObject() {
        JSONSerializer serializer = new JSONSerializer();

        String charlieJson = serializer.serialize( charlie );
        assertTrue( charlieJson.contains( MockPerson.class.getName() ) );
        assertTrue( charlieJson.contains("\"firstname\"") );
        assertTrue( charlieJson.contains("\"Charlie\"") );
        assertTrue( charlieJson.contains("\"lastname\"") );
        assertTrue( charlieJson.contains("\"Hubbard\"") );
        assertTrue( charlieJson.contains("work") );
        assertTrue( charlieJson.contains("home") );
        assertTrue( charlieJson.contains("street") );
        assertTrue( charlieJson.contains( MockAddress.class.getName() ) );
        assertTrue( charlieJson.contains("zipcode") );
        assertTrue( charlieJson.contains( MockZipcode.class.getName() ) );
        assertFalse( charlieJson.contains("phones") );
        assertFalse( charlieJson.contains( MockPhone.class.getName() ) );
        assertFalse( charlieJson.contains("hobbies") );

        JSONSerializer benSerializer = new JSONSerializer();
        benSerializer.exclude("home", "work");
        String benJson = benSerializer.serialize( ben );
        assertTrue( benJson.contains( MockPerson.class.getName() ) );
        assertTrue( benJson.contains("firstname") );
        assertTrue( benJson.contains("Ben") );
        assertTrue( benJson.contains("lastname") );
        assertTrue( benJson.contains("Hubbard") );
        assertTrue( benJson.contains("birthdate") );

        assertFalse( benJson.contains( MockAddress.class.getName() ) );
        assertFalse( benJson.contains( "work") );
        assertFalse( benJson.contains( "home" ) );
        assertFalse( benJson.contains("street") );
        assertFalse( benJson.contains("city") );
        assertFalse( benJson.contains("state") );
        assertFalse( benJson.contains( MockZipcode.class.getName() ) );
        assertFalse( benJson.contains("zipcode") );
        assertFalse( benJson.contains( MockPhone.class.getName() ) );
        assertFalse( benJson.contains("hobbies") );

        serializer.exclude("home.zipcode", "work.zipcode" );

        String json2 = serializer.serialize(charlie);
        assertTrue( json2.contains( MockPerson.class.getName() ) );
        assertTrue( json2.contains("work") );
        assertTrue( json2.contains("home") );
        assertTrue( json2.contains("street") );
        assertTrue( json2.contains( MockAddress.class.getName() ) );
        assertFalse( json2.contains("zipcode") );
        assertFalse( json2.contains("phones") );
        assertFalse( json2.contains( MockZipcode.class.getName() ) );
        assertFalse( json2.contains( MockPhone.class.getName() ) );
        assertFalse( json2.contains("hobbies") );

        serializer.include("hobbies").exclude("phones.areaCode", "phones.exchange", "phones.number" );

        String json3 = serializer.serialize(charlie);
        assertTrue( json3.contains( MockPerson.class.getName() ) );
        assertTrue( json3.contains("work") );
        assertTrue( json3.contains("home") );
        assertTrue( json3.contains("street") );
        assertTrue( json3.contains( MockAddress.class.getName() ) );
        assertTrue( json3.contains("phones") );
        assertTrue( json3.contains("phoneNumber") );
        assertTrue( json3.contains( MockPhone.class.getName() ) );
        assertTrue( json3.contains("hobbies") );
        assertFalse( json3.contains("zipcode") );
        assertFalse( json3.contains( MockZipcode.class.getName() ) );
        assertFalse( json3.contains("areaCode") );
        assertFalse( json3.contains("exchange") );
        assertFalse( json3.contains("number") );

        assertTrue( json3.startsWith("{") );
        assertTrue( json3.endsWith("}") );
    }

    public void testMap() {
        JSONSerializer serializer = new JSONSerializer();
        String colorsJson = serializer.serialize( colors );
        for( Iterator i = colors.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            assertTrue( colorsJson.contains( entry.getKey().toString() ) );
            assertTrue( colorsJson.contains( entry.getValue().toString() ) );
        }
        assertTrue( colorsJson.startsWith("{") );
        assertTrue( colorsJson.endsWith("}") );
    }

    public void testCollection() {
        JSONSerializer serializer = new JSONSerializer();
        String colorsJson = serializer.serialize( colors.values() );
        for( Iterator i = colors.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry)i.next();
            assertTrue( colorsJson.contains( entry.getValue().toString() ) );
            assertFalse( colorsJson.contains( entry.getKey().toString() ) );
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

        assertTrue( peopleJson.contains( MockPerson.class.getName() ) );
        assertTrue( peopleJson.contains("\"firstname\"") );
        assertTrue( peopleJson.contains("\"Charlie\"") );
        assertTrue( peopleJson.contains("\"Ben\"") );
        assertTrue( peopleJson.contains("\"lastname\"") );
        assertTrue( peopleJson.contains("\"Hubbard\"") );
        assertTrue( peopleJson.contains( MockAddress.class.getName() ) );

        serializer = new JSONSerializer().exclude("home", "work");
        peopleJson = serializer.serialize( people );

        assertTrue( peopleJson.contains( MockPerson.class.getName() ) );
        assertTrue( peopleJson.contains("\"firstname\"") );
        assertTrue( peopleJson.contains("\"Charlie\"") );
        assertTrue( peopleJson.contains("\"Ben\"") );
        assertTrue( peopleJson.contains("\"lastname\"") );
        assertTrue( peopleJson.contains("\"Hubbard\"") );
        assertFalse( peopleJson.contains( MockAddress.class.getName() ) );
    }

    public void testDeepIncludes() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.include("people.hobbies").serialize( network );
        assertTrue( peopleJson.contains("\"name\"") );
        assertTrue( peopleJson.contains("\"My Network\"") );
        assertTrue( peopleJson.contains("\"firstname\"") );
        assertTrue( peopleJson.contains("\"Charlie\"") );
        assertTrue( peopleJson.contains("\"Ben\"") );
        assertTrue( peopleJson.contains("\"lastname\"") );
        assertTrue( peopleJson.contains("\"Hubbard\"") );
        assertTrue( peopleJson.contains("\"hobbies\"") );
        assertTrue( peopleJson.contains("\"Purse snatching\"") );
    }

    public void testDates() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.exclude("home", "work").serialize( charlie );
        assertTrue( peopleJson.contains("\"firstname\"") );
        assertTrue( peopleJson.contains("\"Charlie\"") );
        assertTrue( peopleJson.contains("new Date(") );
        assertFalse( peopleJson.contains("java.util.Date") );
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
