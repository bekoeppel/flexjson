package flexjson.test;

import flexjson.test.mock.*;
import flexjson.test.mock.superhero.*;

import java.util.Calendar;
import java.util.Date;

public class FixtureCreator {
    public Person createCharlie() {
        Address home = new Address("4132 Pluto Drive", "Atlanta", "Ga", new Zipcode("33913") );
        Address work = new Address("44 Planetary St.", "Neptune", "Milkiway", new Zipcode("30328-0764") );

        Phone pagerPhone = new Phone( PhoneNumberType.PAGER, "404 555-1234");
        Phone cellPhone = new Phone( PhoneNumberType.MOBILE, "770 777 5432");

        Calendar cal = Calendar.getInstance();
        cal.set(1976, Calendar.MARCH, 21, 8, 11);
        Person charlie = new Person("Charlie", "Hubbard", cal.getTime(), home, work );
        charlie.getPhones().add( pagerPhone );
        charlie.getPhones().add( cellPhone );

        charlie.getHobbies().add( "Shorting volatile stocks" );
        charlie.getHobbies().add( "Fixing Horse Races" );
        charlie.getHobbies().add( "Taking dives in the 3rd round" );

        return charlie;
    }

    public Person createBen() {
        Address benhome = new Address("8735 Hilton Way", "Chattanooga", "Tn", new Zipcode("82742") );
        Address benwork = new Address("44 Planetary St.", "Neptune", "Milkiway", new Zipcode("12345") );

        Calendar benCal = Calendar.getInstance();
        benCal.set(1978, Calendar.JULY, 5, 8, 11);
        Person ben = new Person("Ben", "Hubbard", benCal.getTime(), benhome, benwork );
        ben.getHobbies().add( "Purse snatching" );
        ben.getHobbies().add( "Running sweat shops" );
        ben.getHobbies().add( "Fixing prices" );

        return ben;
    }

    public Network createNetwork( String name, Person... people ) {
        return new Network( name, people );
    }

    public Person createPedro() {
        Zipcode pedroZip = new Zipcode("49404");
        Address pedroHome = new Address(" 12 Acrelândia Way", "Rio de Janeiro", "Brazil", pedroZip);
        Address pedroWork = new Address(" 12 Acrelândia Way", "Rio de Janeiro", "Brazil", pedroZip );

        Phone pedroPhone = new Phone( PhoneNumberType.MOBILE, "123 555 2323");

        Calendar pedroCal = Calendar.getInstance();
        pedroCal.set( 1980, Calendar.APRIL, 12, 11, 45);
        Person pedro = new Person("Pedro", "Neves", pedroCal.getTime(), pedroHome, pedroWork );
        pedro.getPhones().add( pedroPhone );

        return pedro;
    }

    public Employee createDilber() {
        return new Employee("Dilbert", "", new Date(), new Address( "123 Finland Dr", "Cubicleville", "Hell", new Zipcode("66666") ), new Address( "123 Finland Dr", "Cubicleville", "Hell", new Zipcode("66666") ), "Initech" );        
    }

    public Hero createSuperman() {
        return new Hero("Super Man", new SecretIdentity("Clark Kent"), new SecretLair("Fortress of Solitude"), new XRayVision(0.8f), new HeatVision(0.7f), new Flight(1000.0f), new Invincible() );
    }

    public Villian createLexLuthor() {
        return new Villian("Lex Luthor", createSuperman(), new SecretLair("Legion of Doom") );
    }
}
