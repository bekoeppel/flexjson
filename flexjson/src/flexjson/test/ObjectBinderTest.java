package flexjson.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.util.*;

import flexjson.test.mock.Person;
import flexjson.test.mock.Address;
import flexjson.test.mock.Phone;
import flexjson.test.mock.PhoneNumberType;
import flexjson.ObjectBinder;

public class ObjectBinderTest extends TestCase {

    public void setUp() {
    }

    public void testPlainObject() {
        Map map = new HashMap();
        map.put("class", Person.class.getName() );
        map.put("firstname", "Charlie");
        map.put("lastname", "Hubbard");

        ObjectBinder binder = new ObjectBinder();
        Object boundObject = binder.bind( map );

        assertEquals("Assert that we received a person class back.", Person.class, boundObject.getClass() );
        Person p = (Person)boundObject;
        assertEquals( "Charlie", p.getFirstname() );
        assertEquals( "Hubbard", p.getLastname() );
    }

    public void testObjectWithLongDate() {
        Date targetDate = new Date();
        Map map = new HashMap();
        map.put("class", Person.class.getName() );
        map.put("birthdate", targetDate.getTime() );

        ObjectBinder binder = new ObjectBinder();
        Person person = (Person)binder.bind( map );

        assertEquals( targetDate, person.getBirthdate() );
    }

    public void testObjectWithStringDate() {
        Date targetDate = new Date( System.currentTimeMillis() / 1000 * 1000 );
        Map map = new HashMap();
        map.put("class", Person.class.getName() );
        map.put("birthdate", targetDate.toString() );

        ObjectBinder binder = new ObjectBinder();
        Person person = (Person)binder.bind( map );

        assertEquals( targetDate, person.getBirthdate() );
    }

    public void testObjectWithDoubleDate() {
        Date targetDate = new Date();
        Map map = new HashMap();
        map.put("class", Person.class.getName() );
        map.put("birthdate", (double)targetDate.getTime() );

        ObjectBinder binder = new ObjectBinder();
        Person person = (Person)binder.bind( map );

        assertEquals( targetDate, person.getBirthdate() );
    }

    public void testObjectWithEmbeddedCollections() {
        Map zipcode = new HashMap();
        zipcode.put("zipcode", "30117");

        Map home = new HashMap();
        home.put("class", Address.class.getName() );
        home.put("street", "210 Habersham Place");
        home.put("city", "Carrollton");
        home.put("state", "Georgia");
        home.put("zipcode", zipcode );

        Map zipcode2 = new HashMap();
        zipcode2.put("zipcode", "30314" );

        Map work = new HashMap();
        work.put("class", Address.class.getName() );
        work.put("street", "" );
        work.put("city", "Atlanta");
        work.put("state", "Georgia");
        work.put("zipcode", zipcode2);

        Map map = new HashMap();
        map.put("class", Person.class.getName() );
        map.put("firstname", "Charlie");
        map.put("lastname", "Hubbard");
        map.put("home", home );
        map.put("work", work );

        List<Phone> phones = new ArrayList<Phone>();
        phones.add( new Phone(PhoneNumberType.HOME, "404 123 5555") );
        phones.add( new Phone(PhoneNumberType.WORK, "404 321 5555") );
        phones.add( new Phone(PhoneNumberType.FAX, "404 678 5555") );

        map.put("phones", phones);

        ObjectBinder binder = new ObjectBinder();
        Person person = (Person)binder.bind( map );

        assertTrue( "Make sure our array has stuff in it.", !person.getPhones().isEmpty() );
        assertEquals( 3, person.getPhones().size() );
        for( int i = 0; i < phones.size(); i++ ) {
            validatePhone( phones.get(i), (Phone)person.getPhones().get(i));
        }

        assertEquals( "Make sure our array has stuff in it.", 3, person.getPhones().size() );
    }

    private void validatePhone(Phone expected, Phone actual ) {
        assertEquals( expected.getAreaCode(), actual.getAreaCode() );
        assertEquals( expected.getExchange(), actual.getExchange() );
        assertEquals( expected.getNumber(), actual.getNumber() );
        assertEquals( expected.getType(), actual.getType() );
    }

    public void testObjectToEmbeddedObject() {
        Map zipcode = new HashMap();
        zipcode.put("zipcode", "30117");

        Map home = new HashMap();
        home.put("class", Address.class.getName() );
        home.put("street", "210 Habersham Place");
        home.put("city", "Carrollton");
        home.put("state", "Georgia");
        home.put("zipcode", zipcode );

        Map zipcode2 = new HashMap();
        zipcode2.put("zipcode", "30314" );
        
        Map work = new HashMap();
        work.put("class", Address.class.getName() );
        work.put("street", "" );
        work.put("city", "Atlanta");
        work.put("state", "Georgia");
        work.put("zipcode", zipcode2);

        Map map = new HashMap();
        map.put("class", Person.class.getName() );
        map.put("firstname", "Charlie");
        map.put("lastname", "Hubbard");
        map.put("home", home );
        map.put("work", work );

        ObjectBinder binder = new ObjectBinder();
        Person person = (Person)binder.bind( map );

        assertEquals( home.get("street"), person.getHome().getStreet() );
        assertEquals( home.get("city"), person.getHome().getCity() );
        assertEquals( home.get("state"), person.getHome().getState() );
        assertEquals( zipcode.get("zipcode"), person.getHome().getZipcode().getZipcode() );
    }

    public void tearDown() {
    }

    public static Test suite() {
        return new TestSuite( ObjectBinderTest.class );
    }

    public static void main(String[] args) {
        TestRunner.run( suite () );
    }
}
