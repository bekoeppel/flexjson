package flexjson.test;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;
import junit.textui.TestRunner;
import flexjson.*;
import flexjson.test.mock.Person;
import flexjson.test.mock.Employee;
import flexjson.test.mock.Pair;
import flexjson.test.mock.superhero.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class JSONDeserializerTest extends TestCase {

    public void testDeserializeNoIncludes() {
        Person charlie = new FixtureCreator().createCharlie();
        String json = new JSONSerializer().serialize( charlie );
        Person jsonCharlie = new JSONDeserializer<Person>().deserialize( json );
        assertNotNull("Make sure we deserialized something non-null", jsonCharlie );

        assertEquals( charlie.getLastname(), jsonCharlie.getLastname() );
        assertEquals( charlie.getFirstname(), jsonCharlie.getFirstname() );
        assertEquals( charlie.getBirthdate(), jsonCharlie.getBirthdate() );
        assertEquals( charlie.getHome().getState(), jsonCharlie.getHome().getState() );
        assertEquals( charlie.getHome().getStreet(), jsonCharlie.getHome().getStreet() );
        assertEquals( charlie.getHome().getCity(), jsonCharlie.getHome().getCity() );
        assertEquals( charlie.getWork().getCity(), jsonCharlie.getWork().getCity() );
        assertEquals( jsonCharlie, jsonCharlie.getWork().getPerson() );
    }

    public void testDeserializeWithIncludes() {
        Person charlie = new FixtureCreator().createCharlie();
        String json = new JSONSerializer().include("phones", "hobbies").serialize( charlie );
        Person jsonCharlie = new JSONDeserializer<Person>().deserialize( json );
        assertNotNull("Make sure we deserialized something non-null", jsonCharlie );

        assertFalse( "Make sure that our phones are not empty", jsonCharlie.getPhones().isEmpty() );
        assertEquals( 2, jsonCharlie.getPhones().size() );

        assertEquals( 3, jsonCharlie.getHobbies().size() );
        assertEquals( "Fixing Horse Races", jsonCharlie.getHobbies().get(1) );
    }

    public void testSubClassDeserialize() {
        Employee dilbert = new FixtureCreator().createDilber();
        String json = new JSONSerializer().include("phones", "hobbies").serialize( dilbert );
        Person jsonDilbert = new JSONDeserializer<Person>().deserialize( json );
        assertNotNull("Make sure we got back dilbert.", jsonDilbert);
        assertTrue("Make sure dilbert came back as an employee.", jsonDilbert instanceof Employee );
        assertEquals("Make sure dilbert has a company.", dilbert.getCompany() ,((Employee)jsonDilbert).getCompany() );
    }

    public void testDeserializeInterfaces() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer().include("powers").serialize( superman );
        Hero jsonSuperMan = new JSONDeserializer<Hero>().deserialize( json );
        assertNotNull("Make sure we got back a superman", jsonSuperMan );

        assertEquals( "Make sure the super powers were created properly.", 4, jsonSuperMan.getPowers().size() );
        assertHeroHasPowers(jsonSuperMan);
    }

    public void testNoClassHints() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer().exclude("*.class").serialize( superman );
        Hero jsonSuperMan = new JSONDeserializer<Hero>().use(null, Hero.class).use("lair", SecretLair.class ).use("secretIdentity", SecretIdentity.class).deserialize( json );

        assertNotNull("Make sure we got back a superman", jsonSuperMan);
        assertEquals("Assert our name is super man", "Super Man", jsonSuperMan.getName() );
        assertNotNull("Assert our secret identiy was restored", jsonSuperMan.getIdentity() );
        assertEquals("Assert our secret identity is Clark Kent", "Clark Kent", jsonSuperMan.getIdentity().getName() );
        assertNotNull("Assert our secret lair was restored", jsonSuperMan.getLair() );
        assertEquals("Assert our lair is the fortrees of solitude", "Fortress of Solitude", jsonSuperMan.getLair().getName() );
    }

    public void testNoHintsButClassesForCollection() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer().include("powers.class").exclude("*.class").serialize( superman );
        Hero jsonSuperMan = new JSONDeserializer<Hero>()
                .use(null, Hero.class)
                .deserialize( json );
        assertHeroHasPowers(jsonSuperMan);
    }

    private void assertHeroHasPowers(Hero jsonSuperMan) {
        for( int i = 0; i < jsonSuperMan.getPowers().size(); i++ ) {
            assertTrue("Make sure super powers are instances of SuperPower", jsonSuperMan.getPowers().get(i) instanceof SuperPower);
        }
    }

    public void testNoClassHintsForCollections() {
        Hero superman = new FixtureCreator().createSuperman();
        String json = new JSONSerializer()
                .include("powers")
                .include("powers.class")
                .transform(new SimpleClassnameTransformer(), "powers.class")
                .exclude("*.class").serialize( superman );
        Hero jsonSuperMan = new JSONDeserializer<Hero>()
                .use(null, Hero.class)
                .use("lair", SecretLair.class )
                .use("secretIdentity", SecretIdentity.class)
                .use("powers", new SimpleClassLocator("flexjson.test.mock.superhero") )
                .deserialize( json );
       assertHeroHasPowers( jsonSuperMan );
    }

    public void testListSerialization() {
        FixtureCreator fixtures = new FixtureCreator();
        Person ben = fixtures.createBen();
        Person charlie = fixtures.createCharlie();
        Person pedro = fixtures.createPedro();
        List<Person> list = new ArrayList<Person>(3);
        list.add( ben );
        list.add( charlie );
        list.add( pedro );

        String json = new JSONSerializer().serialize( list );
        List<Person> people = new JSONDeserializer<List<Person>>().deserialize( json );
        assertEquals( ArrayList.class, people.getClass() );
    }

    public void testGenericTypeDeserialization() {
        FixtureCreator fixtures = new FixtureCreator();
        Pair<Hero,Villian> archenemies = new Pair<Hero,Villian>( fixtures.createSuperman(), fixtures.createLexLuthor() );
        String json = new JSONSerializer().exclude("*.class").serialize( archenemies );
        Pair<Hero,Villian> deserialArchEnemies = new JSONDeserializer<Pair<Hero,Villian>>()
                .use( null, Pair.class )
                .use( "first", Hero.class )
                .use( "second", Villian.class )
                .deserialize( json );

        assertEquals( archenemies.getFirst().getClass(), deserialArchEnemies.getFirst().getClass() );
        assertEquals( archenemies.getSecond().getClass(), deserialArchEnemies.getSecond().getClass() );

        assertEquals( archenemies.getFirst().getIdentity(), deserialArchEnemies.getFirst().getIdentity() );
        assertEquals( archenemies.getFirst().getLair(), deserialArchEnemies.getFirst().getLair() );
        assertEquals( archenemies.getFirst().getName(), deserialArchEnemies.getFirst().getName() );

        assertEquals( archenemies.getSecond().getName(), deserialArchEnemies.getSecond().getName() );
        assertEquals( archenemies.getSecond().getLair(), deserialArchEnemies.getSecond().getLair() );

    }

    public void testMixedCase() {
        String json = "{\"Birthdate\":196261875187,\"Firstname\":\"Charlie\",\"Home\":{\"City\":\"Atlanta\",\"State\":\"Ga\",\"Street\":\"4132 Pluto Drive\",\"Zipcode\":{\"zipcode\":\"33913\"}},\"lastname\":\"Hubbard\",\"Work\":{\"City\":\"Neptune\",\"State\":\"Milkiway\",\"Street\":\"44 Planetary St.\",\"Zipcode\":{\"Zipcode\":\"30328-0764\"}}}";
        Person charlie = new JSONDeserializer<Person>().use( null, Person.class ).deserialize( json );
        assertEquals( "Charlie", charlie.getFirstname() );
        assertEquals( "Hubbard", charlie.getLastname() );
        assertEquals( "Atlanta", charlie.getHome().getCity() );
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
        public String transform(Object value) {
            int classname = value.toString().lastIndexOf('.');
            if( classname > 0 ) {
                return value.toString().substring( classname + 1 );
            } else {
                return value.toString();
            }
        }
    }

    public static class SimpleClassLocator implements ClassLocator {

        private String packageName;

        public SimpleClassLocator(String packageName) {
            this.packageName = packageName;
        }

        public Class locate(Map map, Path currentPath) throws ClassNotFoundException {
            return Class.forName( packageName + "." + map.get("class").toString() );
        }
    }

}