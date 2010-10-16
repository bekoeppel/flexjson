/**
 * Copyright 2007 Charlie Hubbard and Brandon Goodin
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
package flexjson;

import flexjson.mock.*;
import flexjson.transformer.DateTransformer;
import flexjson.transformer.HtmlEncoderTransformer;
import flexjson.model.ListContainer;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JSONSerializerTest extends TestCase {

    final Logger logger = LoggerFactory.getLogger(SimpleSerializeTest.class);

    private Person charlie, ben, pedro;
    private Map colors;
    private List people;
    private Network network;
    private Zipcode pedroZip;
    private Employee dilbert;

    @SuppressWarnings({"unchecked"})
    public void setUp() {
        FixtureCreator fixtureCreator = new FixtureCreator();
        pedroZip = new Zipcode("49404");
        pedro = fixtureCreator.createPedro();
        charlie = fixtureCreator.createCharlie();
        ben = fixtureCreator.createBen();
        colors = fixtureCreator.createColorMap();

        people = new ArrayList();
        people.add(charlie);
        people.add(ben);
        people.add(pedro);

        dilbert = fixtureCreator.createDilbert();

        network = fixtureCreator.createNetwork("My Network", charlie, ben );
    }

    public void testObject() {
        JSONSerializer serializer = new JSONSerializer();

        String charlieJson = serializer.serialize(charlie);

        assertStringValue(Person.class.getName(), charlieJson);
        assertAttribute("firstname", charlieJson);
        assertStringValue("Charlie", charlieJson);
        assertAttribute("lastname", charlieJson);
        assertStringValue("Hubbard", charlieJson);
        assertAttribute("work", charlieJson);
        assertAttribute("home", charlieJson);
        assertAttribute("street", charlieJson);
        assertStringValue(Address.class.getName(), charlieJson);
        assertAttribute("zipcode", charlieJson);
        assertStringValue(Zipcode.class.getName(), charlieJson);
        assertAttributeMissing("person", charlieJson);

        assertAttributeMissing("phones", charlieJson);
        assertStringValueMissing(Phone.class.getName(), charlieJson);
        assertAttributeMissing("hobbies", charlieJson);

        JSONSerializer benSerializer = new JSONSerializer();
        benSerializer.exclude("home", "work");
        String benJson = benSerializer.serialize(ben);
        assertStringValue(Person.class.getName(), benJson);
        assertAttribute("firstname", benJson);
        assertStringValue("Ben", benJson);
        assertAttribute("lastname", benJson);
        assertStringValue("Hubbard", benJson);
        assertAttribute("birthdate", benJson);

        assertStringValueMissing(Address.class.getName(), benJson);
        assertAttributeMissing("work", benJson);
        assertAttributeMissing("home", benJson);
        assertAttributeMissing("street", benJson);
        assertAttributeMissing("city", benJson);
        assertAttributeMissing("state", benJson);
        assertStringValueMissing(Zipcode.class.getName(), benJson);
        assertAttributeMissing("zipcode", benJson);
        assertStringValueMissing(Phone.class.getName(), benJson);
        assertAttributeMissing("hobbies", benJson);
        assertAttributeMissing("person", benJson);

        serializer.exclude("home.zipcode", "work.zipcode");

        String json2 = serializer.serialize(charlie, new StringBuilder());
        assertStringValue(Person.class.getName(), json2);
        assertAttribute("work", json2);
        assertAttribute("home", json2);
        assertAttribute("street", json2);
        assertStringValue(Address.class.getName(), json2);
        assertAttributeMissing("zipcode", json2);
        assertAttributeMissing("phones", json2);
        assertStringValueMissing(Zipcode.class.getName(), json2);
        assertStringValueMissing(Phone.class.getName(), json2);
        assertAttributeMissing("hobbies", json2);
        assertAttributeMissing("type", json2);
        assertStringValueMissing("PAGER", json2);

        serializer.include("hobbies").exclude("phones.areaCode", "phones.exchange", "phones.number");

        String json3 = serializer.serialize(charlie, new StringBuilder());
        assertStringValue(Person.class.getName(), json3);
        assertAttribute("work", json3);
        assertAttribute("home", json3);
        assertAttribute("street", json3);
        assertStringValue(Address.class.getName(), json3);
        assertAttribute("phones", json3);
        assertAttribute("phoneNumber", json3);
        assertStringValue(Phone.class.getName(), json3);
        assertAttribute("hobbies", json3);

        assertAttributeMissing("zipcode", json3);
        assertAttributeMissing(Zipcode.class.getName(), json3);
        assertAttributeMissing("areaCode", json3);
        assertAttributeMissing("exchange", json3);
        assertAttributeMissing("number", json3);
        assertAttribute("type", json3);
        assertStringValue("PAGER", json3);

        assertTrue(json3.startsWith("{"));
        assertTrue(json3.endsWith("}"));
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testMap() {
        JSONSerializer serializer = new JSONSerializer();
        String colorsJson = serializer.serialize(colors);
        for (Iterator i = colors.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            assertAttribute(entry.getKey().toString(), colorsJson);
            assertStringValue(entry.getValue().toString(), colorsJson);
        }
        assertTrue(colorsJson.startsWith("{"));
        assertTrue(colorsJson.endsWith("}"));

        colors.put( null, "#aaaaaa" );
        colors.put( "orange", null );

        String json = serializer.serialize( colors );
        assertTrue( "Assert null is present as a key", json.contains("null:") );
        assertStringValue( "#aaaaaa", json );
        assertAttribute( "orange", json );
        assertTrue( "Assert null is present as a value", json.contains( ":null" ) );
    }

    public void testArray() {
        int[] array = new int[30];
        for (int i = 0; i < array.length; i++) {
            array[i] = i;
        }

        String json = new JSONSerializer().serialize(array);

        for (int i = 0; i < array.length; i++) {
            assertNumber(i, json);
        }

        assertFalse("Assert that there are no double quotes in the output", json.contains("\""));
        assertFalse("Assert that there are no single quotes in the output", json.contains("\'"));
    }

    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public void testCollection() {
        JSONSerializer serializer = new JSONSerializer();
        String colorsJson = serializer.serialize(colors.values());
        for (Iterator i = colors.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            assertAttributeMissing(entry.getKey().toString(), colorsJson);
            assertStringValue(entry.getValue().toString(), colorsJson);
        }
        assertTrue(colorsJson.startsWith("["));
        assertTrue(colorsJson.endsWith("]"));
    }

    public void testString() {
        assertSerializedTo("Hello", "\"Hello\"");
        assertSerializedTo("Hello World", "\"Hello World\"");
        assertSerializedTo("Hello\nWorld", "\"Hello\\nWorld\"");
        assertSerializedTo("Hello 'Charlie'", "\"Hello 'Charlie'\"");
        assertSerializedTo("Hello \"Charlie\"", "\"Hello \\\"Charlie\\\"\"");
        assertSerializedTo(
                "¥ Shadowing the senior pastor as he performed weekly duties including sermon\n" +
                "preparation, wedding, funerals, and other activities.\n" +
                "¥ Teaching Junior High School Sunday School.\n" +
                "¥ Participating in session meetings, worship planning meetings, and staff meetings.\n" +
                "¥ Assisting in research for sermon preparation.\n" +
                "¥ Speaking occasionally in church including scripture reading and giving the\n" +
                "announcements.",
                "\"¥ Shadowing the senior pastor as he performed weekly duties including sermon\\n" +
                "preparation, wedding, funerals, and other activities.\\n" +
                "¥ Teaching Junior High School Sunday School.\\n" +
                "¥ Participating in session meetings, worship planning meetings, and staff meetings.\\n" +
                "¥ Assisting in research for sermon preparation.\\n" +
                "¥ Speaking occasionally in church including scripture reading and giving the\\n" +
                "announcements.\"");
    }

    public void testListOfObjects() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.serialize(people);

        assertStringValue(Person.class.getName(), peopleJson);
        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertStringValue("Ben", peopleJson);
        assertAttribute("lastname", peopleJson);
        assertStringValue("Hubbard", peopleJson);
        assertStringValue(Address.class.getName(), peopleJson);
        assertStringValue("Pedro", peopleJson);
        assertStringValue("Neves", peopleJson);

        serializer = new JSONSerializer().exclude("home", "work");
        peopleJson = serializer.serialize(people);

        assertStringValue(Person.class.getName(), peopleJson);
        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertStringValue("Ben", peopleJson);
        assertAttribute("lastname", peopleJson);
        assertStringValue("Hubbard", peopleJson);
        assertStringValueMissing(Address.class.getName(), peopleJson);
    }

    public void testDeepIncludes() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.include("people.hobbies").serialize(network);

        assertAttribute("name", peopleJson);
        assertStringValue("My Network", peopleJson);
        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertStringValue("Ben", peopleJson);
        assertAttribute("lastname", peopleJson);
        assertStringValue("Hubbard", peopleJson);
        assertAttribute("hobbies", peopleJson);
        assertStringValue("Purse snatching", peopleJson);
    }

    public void testDates() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.exclude("home", "work").serialize(charlie);
        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertNumber(charlie.getBirthdate().getTime(), peopleJson);
        assertStringValueMissing("java.util.Date", peopleJson);
    }

    public void testSimpleShallowWithListInMap() {
        JSONSerializer serializer = new JSONSerializer();
        Map wrapper = new HashMap();
        wrapper.put("name","Joe Blow");
        wrapper.put("people",people);
        String peopleJson = serializer.serialize(wrapper);
        logger.info(peopleJson);
        assertFalse(peopleJson.contains("["));
    }

    public void testSimpleShallowWithListInObject() {
        JSONSerializer serializer = new JSONSerializer();
        ListContainer wrapper = new ListContainer();
        wrapper.setName("Joe Blow");
        wrapper.setPeople(people);
        String peopleJson = serializer.serialize(wrapper);
        logger.info(peopleJson);
        assertFalse(peopleJson.contains("["));
    }

    public void testRootName() {
        JSONSerializer serializer = new JSONSerializer().rootName("people");
        String peopleJson = serializer.serialize(people);
        logger.info(peopleJson);
        assertTrue(peopleJson.startsWith("{\"people\":"));
    }

    public void testSetIncludes() {
        JSONSerializer serializer = new JSONSerializer();
        serializer.setIncludes(Arrays.asList("people.hobbies", "phones", "home", "people.resume"));
        List<PathExpression> includes = serializer.getIncludes();

        assertFalse(includes.isEmpty());
        assertEquals(4, includes.size());
        assertTrue(includes.contains(new PathExpression("people.hobbies", true)));
        assertTrue(includes.contains(new PathExpression("people.resume", true)));
        assertTrue(includes.contains(new PathExpression("phones", true)));
        assertTrue(includes.contains(new PathExpression("home", true)));
    }

    public void testI18n() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.include("work", "home").serialize(pedro);

        assertAttribute("work", json);
        assertAttribute("home", json);

        assertEquals(2, occurs("Acrelândia", json));
    }

    public void testDeepSerialization() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.deepSerialize(network);

        assertAttribute("name", peopleJson);
        assertStringValue("My Network", peopleJson);
        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertStringValue("Ben", peopleJson);
        assertAttribute("lastname", peopleJson);
        assertStringValue("Hubbard", peopleJson);
        assertAttributeMissing("hobbies", peopleJson); // there is an annotation that explicitly excludes this!
        assertStringValueMissing("Purse snatching", peopleJson);
    }

    public void testDeepSerializationWithIncludeOverrides() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.include("people.hobbies").deepSerialize(network);

        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertAttribute("hobbies", peopleJson);
        assertStringValue("Purse snatching", peopleJson);
        assertStringValue("Running sweat shops", peopleJson);
        assertStringValue("Fixing prices", peopleJson);
    }

    public void testDeepSerializationWithExcludes() {
        JSONSerializer serializer = new JSONSerializer();
        String peopleJson = serializer.exclude("people.work").deepSerialize(network);

        assertAttribute("firstname", peopleJson);
        assertStringValue("Charlie", peopleJson);
        assertAttributeMissing("work", peopleJson);
        assertStringValue("4132 Pluto Drive", peopleJson);
        assertAttribute("home", peopleJson);
        assertAttribute("phones", peopleJson);
    }

    public void testDeepSerializationCycles() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.deepSerialize(people);

        assertAttribute("zipcode", json);
        assertEquals(2, occurs(pedroZip.getZipcode(), json));
        assertAttributeMissing("person", json);
    }

    public void testSerializeSuperClass() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.serialize(dilbert);

        assertAttribute("company", json);
        assertStringValue("Initech", json);
        assertAttribute("firstname", json);
        assertStringValue("Dilbert", json);
    }

    public void testSerializePublicFields() {
        Spiderman spiderman = new Spiderman();

        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.serialize(spiderman);

        assertAttribute("spideySense", json);
        assertAttribute("superpower", json);
        assertStringValue("Creates web", json);
    }

    /**
     * https://sourceforge.net/tracker/index.php?func=detail&aid=2927626&group_id=194042&atid=947842#
     */
    public void testExcludingPublicFields() {
        Spiderman spiderman = new Spiderman();

        String json = new JSONSerializer().exclude("superpower").serialize( spiderman );

        assertAttributeMissing("superpower", json);
        assertAttribute("spideySense", json);
    }

    public void testPrettyPrint() {
        JSONSerializer serializer = new JSONSerializer();

        serializer.include("phones").prettyPrint(true);
        String charlieJson = serializer.serialize(charlie);
        logger.info(charlieJson);
    }

    public void testWildcards() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.include("phones").exclude("*.class").serialize(charlie);

        assertAttributeMissing("class", json);
        assertAttribute("phones", json);
        assertAttributeMissing("hobbies", json);
    }

    public void testWildcardDepthControl() {
        JSONSerializer serializer = new JSONSerializer();
        serializer.include("*.class").prettyPrint(true);
        String json = serializer.serialize(charlie);

        assertAttributeMissing("phones", json);
        assertAttributeMissing("hobbies", json);
    }

    public void testExcludeAll() {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.exclude("*").serialize(charlie);

        assertEquals("{}", json);
        assertAttributeMissing("class", json);
        assertAttributeMissing("phones", json);
        assertAttributeMissing("firstname", json);
        assertAttributeMissing("lastname", json);
        assertAttributeMissing("hobbies", json);
    }

    public void testMixedWildcards() {
        JSONSerializer serializer = new JSONSerializer();
        serializer.include("firstname", "lastname").exclude("*").prettyPrint(true);
        String json = serializer.serialize(charlie);

        assertAttribute("firstname", json);
        assertStringValue("Charlie", json);
        assertAttribute("lastname", json);
        assertStringValue("Hubbard", json);
        assertAttributeMissing("class", json);
        assertAttributeMissing("phones", json);
        assertAttributeMissing("birthdate", json);

        serializer = new JSONSerializer();
        serializer.include("firstname", "lastname", "phones.areaCode", "phones.exchange", "phones.number").exclude("*").prettyPrint(true);
        json = serializer.serialize(charlie);

        assertAttribute("firstname", json);
        assertStringValue("Charlie", json);
        assertAttribute("lastname", json);
        assertStringValue("Hubbard", json);
        assertAttributeMissing("class", json);
        assertAttribute("phones", json);
        assertAttributeMissing("birthdate", json);
    }

    public void testHtmlTransformation() {
        String json = new JSONSerializer().transform(new HtmlEncoderTransformer(), "").serialize("Marker & Thompson");
        assertEquals("Assert that the & was replaced with &amp;", "\"Marker &amp; Thompson\"", json);

        Map<String, String> map = new HashMap<String, String>();
        map.put("Chuck D", "Chuck D <chuckd@publicenemy.com>");
        map.put("Run", "Run <run@rundmc.com>");
        json = new JSONSerializer().transform(new HtmlEncoderTransformer(), String.class).serialize(map);
        assertStringValue("Chuck D &lt;chuckd@publicenemy.com&gt;", json);
        assertStringValue("Run &lt;run@rundmc.com&gt;", json);

        Person xeno = new Person("><eno", "h&d", new Date(), new Address("1092 Hemphill", "Atlanta", "GA", new Zipcode("30319")), new Address("333 \"Diddle & Town\"", "Atlanta", "30329", new Zipcode("30320")));

        json = new JSONSerializer().transform(new HtmlEncoderTransformer(), "firstname", "lastname").exclude("*.class").serialize(xeno);

        assertStringValue("&gt;&lt;eno", json);
        assertStringValue("h&amp;d", json);
        assertStringValue("333 \\\"Diddle & Town\\\"", json);
        assertStringValueMissing("333 &quot;Diddle &amp; Town&quot;", json);
        assertAttributeMissing("class", json);
    }

    public void testDateTransforming() {
        String json = new JSONSerializer().transform(new DateTransformer("yyyy-MM-dd"), "birthdate").serialize(charlie);

        assertAttribute("birthdate", json);
        assertStringValue("1988-11-23", json);
    }

    private int occurs(String str, String json) {
        int current = 0;
        int count = 0;
        while (current >= 0) {
            current = json.indexOf(str, current);
            if (current > 0) {
                count++;
                current += str.length();
            }
        }
        return count;
    }

    private void assertAttributeMissing(String attribute, String json) {
        assertAttribute(attribute, json, false);
    }

    private void assertAttribute(String attribute, String peopleJson) {
        assertAttribute(attribute, peopleJson, true);
    }

    private void assertAttribute(String attribute, String peopleJson, boolean isPresent) {
        if (isPresent) {
            assertTrue("'" + attribute + "' attribute is missing", peopleJson.contains("\"" + attribute + "\":"));
        } else {
            assertFalse("'" + attribute + "' attribute is present when it's not expected.", peopleJson.contains("\"" + attribute + "\":"));
        }
    }

    private void assertStringValue(String value, String json, boolean isPresent) {
        if (isPresent) {
            assertTrue("'" + value + "' value is missing", json.contains("\"" + value + "\""));
        } else {
            assertFalse("'" + value + "' value is present when it's not expected.", json.contains("\"" + value + "\""));
        }
    }

    private void assertNumber(Number number, String json) {
        assertTrue(number + " is missing as a number.", json.contains(number.toString()));
    }

    private void assertStringValueMissing(String value, String json) {
        assertStringValue(value, json, false);
    }

    private void assertStringValue(String value, String json) {
        assertStringValue(value, json, true);
    }

    private void assertSerializedTo(String original, String expected) {
        JSONSerializer serializer = new JSONSerializer();
        String json = serializer.serialize(original);
        assertEquals(expected, json);
    }

    public void tearDown() {
    }

    public static Test suite() {
        return new TestSuite(JSONSerializerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
