package flexjson;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;
import flexjson.*;
import flexjson.transformer.DateTransformer;
import flexjson.transformer.Transformer;
import flexjson.mock.Person;
import flexjson.mock.*;
import flexjson.mock.superhero.*;

import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.lang.reflect.Array;

public class JSONDeserializerTest extends TestCase {

    public void testDeserializeNoIncludes() {
        Person charlie = new FixtureCreator().createCharlie();
        String json = new JSONSerializer().serialize(charlie);
        Person jsonCharlie = new JSONDeserializer<Person>().deserialize(json);
        assertNotNull("Make sure we deserialized something non-null", jsonCharlie);

        assertEquals(charlie.getLastname(), jsonCharlie.getLastname());
        assertEquals(charlie.getFirstname(), jsonCharlie.getFirstname());
        assertEquals(charlie.getBirthdate(), jsonCharlie.getBirthdate());
        assertEquals(charlie.getHome().getState(), jsonCharlie.getHome().getState());
        assertEquals(charlie.getHome().getStreet(), jsonCharlie.getHome().getStreet());
        assertEquals(charlie.getHome().getCity(), jsonCharlie.getHome().getCity());
        assertEquals(charlie.getWork().getCity(), jsonCharlie.getWork().getCity());
        assertEquals(jsonCharlie, jsonCharlie.getWork().getPerson());
    }

    public void testDeserializeWithIncludes() {
        Person charlie = new FixtureCreator().createCharlie();
        String json = new JSONSerializer().include("phones", "hobbies").serialize(charlie);
        Person jsonCharlie = new JSONDeserializer<Person>().deserialize(json);
        assertNotNull("Make sure we deserialized something non-null", jsonCharlie);

        assertFalse("Make sure that our phones are not empty", jsonCharlie.getPhones().isEmpty());
        assertEquals(2, jsonCharlie.getPhones().size());

        assertEquals(3, jsonCharlie.getHobbies().size());
        assertEquals("Fixing Horse Races", jsonCharlie.getHobbies().get(1));
    }

    public void testSubClassDeserialize() {
        Employee dilbert = new FixtureCreator().createDilbert();
        String json = new JSONSerializer().include("phones", "hobbies").serialize(dilbert);
        Person jsonDilbert = new JSONDeserializer<Person>().deserialize(json);
        assertNotNull("Make sure we got back dilbert.", jsonDilbert);
        assertTrue("Make sure dilbert came back as an employee.", jsonDilbert instanceof Employee);
        assertEquals("Make sure dilbert has a company.", dilbert.getCompany(), ((Employee) jsonDilbert).getCompany());
    }

    public void testDeserializeInterfaces() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer().include("powers").serialize(superman);
        Hero jsonSuperMan = new JSONDeserializer<Hero>().deserialize(json);
        assertNotNull("Make sure we got back a superman", jsonSuperMan);

        assertEquals("Make sure the super powers were created properly.", 4, jsonSuperMan.getPowers().size());
        assertHeroHasPowers(jsonSuperMan);
    }

    public void testNoClassHints() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer().exclude("*.class").serialize(superman);
        Hero jsonSuperMan = new JSONDeserializer<Hero>().use(null, Hero.class).use("lair", SecretLair.class).use("secretIdentity", SecretIdentity.class).deserialize(json);

        assertNotNull("Make sure we got back a superman", jsonSuperMan);
        assertEquals("Assert our name is super man", "Super Man", jsonSuperMan.getName());
        assertNotNull("Assert our secret identiy was restored", jsonSuperMan.getIdentity());
        assertEquals("Assert our secret identity is Clark Kent", "Clark Kent", jsonSuperMan.getIdentity().getName());
        assertNotNull("Assert our secret lair was restored", jsonSuperMan.getLair());
        assertEquals("Assert our lair is the fortrees of solitude", "Fortress of Solitude", jsonSuperMan.getLair().getName());
    }

    public void testNoHintsButClassesForCollection() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer().include("powers.class").exclude("*.class").serialize(superman);
        Hero jsonSuperMan = new JSONDeserializer<Hero>()
                .deserialize(json, Hero.class);
        assertHeroHasPowers(jsonSuperMan);
    }

    private void assertHeroHasPowers(Hero jsonSuperMan) {
        for (int i = 0; i < jsonSuperMan.getPowers().size(); i++) {
            assertTrue("Make sure super powers are instances of SuperPower", jsonSuperMan.getPowers().get(i) instanceof SuperPower);
        }
    }

    public void testNoClassHintsForCollections() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer()
                .include("powers")
                .include("powers.class")
                .transform(new SimpleClassnameTransformer(), "powers.class")
                .exclude("*.class").serialize(superman);
        Hero jsonSuperMan = new JSONDeserializer<Hero>()
                .use("lair", SecretLair.class)
                .use("secretIdentity", SecretIdentity.class)
                .use("powers.values", new SimpleClassLocator("flexjson.mock.superhero"))
                .deserialize(json, Hero.class);
        assertHeroHasPowers(jsonSuperMan);
    }

    public void testListSerialization() {
        FixtureCreator fixtures = new FixtureCreator();
        Person ben = fixtures.createBen();
        Person charlie = fixtures.createCharlie();
        Person pedro = fixtures.createPedro();
        List<Person> list = new ArrayList<Person>(3);
        list.add(ben);
        list.add(charlie);
        list.add(pedro);

        String json = new JSONSerializer().serialize(list);
        List<Person> people = new JSONDeserializer<List<Person>>().deserialize(json);
        assertEquals(ArrayList.class, people.getClass());

        json = new JSONSerializer().exclude("*.class").serialize( list );
        people = new JSONDeserializer<List<Person>>().use("values", Person.class).deserialize(json);

        assertEquals(ArrayList.class, people.getClass() );
        assertEquals(3, people.size());
        assertEquals(Person.class, people.get(0).getClass());

        List<Map> peopleMap = new JSONDeserializer<List<Map>>().deserialize(json);

        assertEquals(ArrayList.class, peopleMap.getClass() );
        assertEquals(3, peopleMap.size());
        assertEquals(HashMap.class, peopleMap.get(0).getClass());
    }

    public void testGenericTypeDeserialization() {
        FixtureCreator fixtures = new FixtureCreator();
        Pair<Hero, Villian> archenemies = new Pair<Hero, Villian>(fixtures.createSuperman(), fixtures.createLexLuthor());
        String json = new JSONSerializer().exclude("*.class").serialize(archenemies);
        Pair<Hero, Villian> deserialArchEnemies = new JSONDeserializer<Pair<Hero, Villian>>()
                .use("first", Hero.class)
                .use("second", Villian.class)
                .deserialize(json, Pair.class);

        assertEquals(archenemies.getFirst().getClass(), deserialArchEnemies.getFirst().getClass());
        assertEquals(archenemies.getSecond().getClass(), deserialArchEnemies.getSecond().getClass());

        assertEquals(archenemies.getFirst().getIdentity(), deserialArchEnemies.getFirst().getIdentity());
        assertEquals(archenemies.getFirst().getLair(), deserialArchEnemies.getFirst().getLair());
        assertEquals(archenemies.getFirst().getName(), deserialArchEnemies.getFirst().getName());

        assertEquals(archenemies.getSecond().getName(), deserialArchEnemies.getSecond().getName());
        assertEquals(archenemies.getSecond().getLair(), deserialArchEnemies.getSecond().getLair());

    }

    public void testGeneralMapDeserialization() {
        FixtureCreator fixtures = new FixtureCreator();
        String json = new JSONSerializer().exclude("*.class").serialize(fixtures.createCharlie());
        Map<String, Object> deserialized = new JSONDeserializer<Map<String, Object>>().deserialize(json);

        assertEquals("Charlie", deserialized.get("firstname"));
        assertEquals("Hubbard", deserialized.get("lastname"));
        assertTrue(Map.class.isAssignableFrom(deserialized.get("work").getClass()));
        assertTrue(Map.class.isAssignableFrom(deserialized.get("home").getClass()));
    }

    public void testListDeserializationNoClass() {
        FixtureCreator fixtures = new FixtureCreator();
        Person ben = fixtures.createBen();
        Person charlie = fixtures.createCharlie();
        Person pedro = fixtures.createPedro();
        List<Person> list = new ArrayList<Person>(3);
        list.add(ben);
        list.add(charlie);
        list.add(pedro);

        String json = new JSONSerializer().exclude("*.class").serialize(list);
        List<Person> people = new JSONDeserializer<List<Person>>().use("values", Person.class ).deserialize(json);
        assertEquals(ArrayList.class, people.getClass());
        assertEquals(3, list.size());
        assertEquals(ben.getFirstname(), list.get(0).getFirstname());
        assertEquals(charlie.getFirstname(), list.get(1).getFirstname());
        assertEquals(pedro.getFirstname(), list.get(2).getFirstname());
    }

    public void testMixedCase() {
        String json = "{\"Birthdate\":196261875187,\"Firstname\":\"Charlie\",\"Home\":{\"City\":\"Atlanta\",\"State\":\"Ga\",\"Street\":\"4132 Pluto Drive\",\"Zipcode\":{\"zipcode\":\"33913\"}},\"lastname\":\"Hubbard\",\"Work\":{\"City\":\"Neptune\",\"State\":\"Milkiway\",\"Street\":\"44 Planetary St.\",\"Zipcode\":{\"Zipcode\":\"30328-0764\"}}}";
        Person charlie = new JSONDeserializer<Person>().use(null, Person.class).deserialize(json);
        assertEquals("Charlie", charlie.getFirstname());
        assertEquals("Hubbard", charlie.getLastname());
        assertEquals("Atlanta", charlie.getHome().getCity());
    }

    public void testDefaultDateFormats() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("MM/dd/yy");
        Person charlie = new Person("Charlie", "Hubbard", new Date(), null, null);
        charlie.setBirthdate(df.parse("03/21/76"));
        DateTransformer transformer = new DateTransformer( df.toPattern() );

        String json = new JSONSerializer().transform(transformer, "birthdate").serialize(charlie);
        Person newUser = new JSONDeserializer<Person>().deserialize(json);
        assertEquals( charlie.getBirthdate(), newUser.getBirthdate() );
        assertEquals( "03/21/76", df.format(newUser.getBirthdate()) );
    }

    public void testDateTransforming() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        Person charlie = new Person("Charlie", "Hubbard", new Date(), null, null);
        charlie.setBirthdate(df.parse("2009/01/02"));
        DateTransformer transformer = new DateTransformer("yyyy/MM/dd");

        String json = new JSONSerializer().transform(transformer, "birthdate").serialize(charlie);
        Person newUser = new JSONDeserializer<Person>().use(transformer, "birthdate").deserialize(json);
        assertEquals( charlie.getBirthdate(), newUser.getBirthdate() );
        assertEquals( "2009/01/02", df.format(newUser.getBirthdate()) );

        json = new JSONSerializer().serialize(charlie);
        newUser = new JSONDeserializer<Person>().deserialize(json);
        assertEquals( charlie.getBirthdate(), newUser.getBirthdate() );
        assertEquals( "2009/01/02", df.format(newUser.getBirthdate()) );
    }

    public void testMapWithEmbeddedObject() {
        Map<String,Network> networks = new JSONDeserializer<Map<String,Network>>().deserialize( "{\"1\": {\"class\":\"flexjson.mock.Network\", \"name\": \"Charlie\"} }" );

        assertNotNull( networks );
        assertEquals( 1, networks.size() );
        assertTrue( networks.containsKey("1") );
        assertNotNull( networks.get("1") );
        assertEquals( Network.class, networks.get("1").getClass() );
        assertEquals( "Charlie", networks.get( "1" ).getName() );

        Map<String,Pair<Phone,Network>> complex = new JSONDeserializer<Map<String,Pair<Phone,Network>>>()
                .use("values", Pair.class)
                .use("values.first", Phone.class)
                .use("values.second", Network.class)
                .deserialize( "{\"1\": { \"first\": { \"areaCode\": \"404\" }, \"second\": {\"name\": \"Charlie\"} } }" );
        assertNotNull( complex );
        assertEquals( 1, complex.size() );
        assertTrue( complex.containsKey("1") );
        assertNotNull( complex.get("1") );
        assertEquals( Pair.class, complex.get("1").getClass() );
        assertEquals( Phone.class, complex.get("1").getFirst().getClass() );
        assertEquals( Network.class, complex.get("1").getSecond().getClass() );
        assertEquals( "404", complex.get( "1" ).getFirst().getAreaCode() );
        assertEquals( "Charlie", complex.get( "1" ).getSecond().getName() );
    }

    public void testArrayType() {
        Person charlie = new FixtureCreator().createCharlie();
        Person ben = new FixtureCreator().createBen();

        Group group = new Group( "brothers", charlie, ben );
        String json = new JSONSerializer().include("people").exclude("*.class").serialize( group );
        Group bro = new JSONDeserializer<Group>().use( null, Group.class ).deserialize( json );

        assertNotNull( bro );
        assertEquals( "brothers", bro.getGroupName() );
        assertEquals( 2, bro.getPeople().length );
        assertEquals( "Charlie", bro.getPeople()[0].getFirstname() );
        assertEquals( "Ben", bro.getPeople()[1].getFirstname() );
    }

    public void testDeserialization() {
      JSONDeserializer<Map<String, Object>> deserializer = new JSONDeserializer<Map<String, Object>>();
      String input = "{property: true, property2:5, property3:'abc'}";
      Map<String, Object> result = deserializer.deserialize(input);
      assertNotNull(result);
      assertEquals(3, result.size());
    }


    // this test should be successful
    public void testNullDeserialization() {
        String input = "{property: null, property2:5, property3:'abc'}";

        JSONDeserializer<Map<String, Object>> deserializer = new JSONDeserializer<Map<String, Object>>();
        deserializer.use( null, HashMap.class );
        Map<String, Object> result = deserializer.deserialize(input);

        assertNotNull(result);
        // fails on this line, because the first property is not deserialized
        assertEquals(3, result.size());
        assertTrue(result.containsKey("property"));
        assertNull("the value should be null", result.get("property"));
    }

    public void testArrayAndClassLocatorsInsideMaps() {
        ClassLocator locator = new ClassLocator() {
            public Class locate(ObjectBinder context, Path currentPath) throws ClassNotFoundException {
                Object source = context.getSource();
                if( source instanceof Map ) {
                    Map map = (Map)source;
                    if( map.containsKey("actLevStart") ) return HashMap.class;
                    if( map.containsKey("class") ) return Class.forName( (String)map.get("class") );
                    return HashMap.class;
                } else if( source instanceof List ) {
                    return LinkedList.class;
                } else {
                    return source.getClass();
                }
            }
        };
        Map<String,Object> bound = new JSONDeserializer<Map<String,Object>>().use("values", locator)
                .deserialize( "{'foo1': 'bar1', 'foo2': {'actLevStart': 1, 'actLevEnd': 2}," +
                        "'foo3': {'someMapKey': 'someMapValue'}, 'foo4': [1, 2, 3]}" );

        assertEquals( "bar1", bound.get("foo1") );
        assertTrue( bound.get("foo2") instanceof Map );
        assertTrue( bound.get("foo4") instanceof LinkedList );
    }

    public void testArraysAndClassLocators() {
        ClassLocator locator = new ClassLocator() {
            public Class locate(ObjectBinder context, Path currentPath) throws ClassNotFoundException {
                Object source = context.getSource();
                if( source instanceof Map ) {
                    Map map = (Map)source;
                    if( map.containsKey("actLevStart") ) return HashMap.class;
                    if( map.containsKey("class") ) return Class.forName( (String)map.get("class") );
                    return HashMap.class;
                } else if( source instanceof List ) {
                    return LinkedList.class;
                } else {
                    return source.getClass();
                }
            }
        };
        List<Map<String,Object>> list = new JSONDeserializer<List<Map<String,Object>>>().use("values", locator).deserialize( "[{'foo1': 'bar1', 'foo2': {'actLevStart': 1, 'actLevEnd': 2 }, 'foo3': {'someMapKey': 'someMapValue'}}]");

        assertEquals( 1, list.size() );
        assertEquals( 3, list.get(0).size() );
    }

    public void testPrimitives() {
        List<Date> dates = new ArrayList<Date>();
        dates.add( new Date() );
        dates.add( new Date(1970, 1, 12) );
        dates.add( new Date(1986, 3, 21) );

        String json = new JSONSerializer().serialize( dates );
        List<Date> jsonDates = new JSONDeserializer<List<Date>>().use(null,ArrayList.class).use("values", Date.class ).deserialize( json );

        assertEquals( jsonDates.size(), dates.size() );
        assertEquals( Date.class, jsonDates.get(0).getClass() );

        List<? extends Number> numbers = Arrays.asList( 1, 0.5, 100.4f, (short)5 );
        json = new JSONSerializer().serialize( numbers );
        List<Number> jsonNumbers = new JSONDeserializer<List<Number>>().deserialize( json );

        assertEquals( numbers.size(), jsonNumbers.size() );
        for( int i = 0; i < numbers.size(); i++ ) {
            assertEquals( numbers.get(i).floatValue(), jsonNumbers.get(i).floatValue() );
        }

        List<Boolean> bools = Arrays.asList( true, false, true, false, false );
        json = new JSONSerializer().serialize( bools );
        List<Boolean> jsonBools = new JSONDeserializer<List<Boolean>>().deserialize( json );

        assertEquals( bools.size(), jsonBools.size() );
        for( int i = 0; i < bools.size(); i++ ) {
            assertEquals( bools.get(i), jsonBools.get(i) );
        }

        assertEquals( numbers.size(), jsonNumbers.size() );
    }

    public void testArray() {
       Person[] p = new Person[3];
        FixtureCreator fixture = new FixtureCreator();
        p[0] = fixture.createCharlie();
        p[1] = fixture.createDilbert();
        p[2] = fixture.createBen();

        String json = new JSONSerializer().serialize( p );

        Person[] jsonP = new JSONDeserializer<Person[]>().use("values", Person.class).deserialize(json, Array.class);

        assertEquals( 3, jsonP.length );
        assertEquals( "Charlie", jsonP[0].getFirstname() );
        assertEquals( "Dilbert", jsonP[1].getFirstname() );
        assertEquals( "Ben", jsonP[2].getFirstname() );
    }

    public void setUp() {
    }

    public void tearDown() {
    }

    public static Test suite() {
        return new TestSuite(JSONDeserializerTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    public static class SimpleClassnameTransformer implements Transformer {
        public void transform(Object value) {
            int classname = value.toString().lastIndexOf('.');
            if (classname > 0) {
                JSONContext.get().write( value.toString().substring(classname + 1) );
            } else {
                JSONContext.get().write( value.toString() );
            }
        }
    }

    public static class SimpleClassLocator implements ClassLocator {

        private String packageName;

        public SimpleClassLocator(String packageName) {
            this.packageName = packageName;
        }

        public Class locate(ObjectBinder context, Path currentPath) throws ClassNotFoundException {
            Map map = (Map) context.getSource();
            return Class.forName(packageName + "." + map.get("class").toString());
        }
    }
}