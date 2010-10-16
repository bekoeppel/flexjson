package flexjson;

import flexjson.mock.Person;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.junit.Ignore;

public class PerformanceTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    @Ignore("Not reliable test.")
    public void testSerializationPerformance() {
        FixtureCreator fixture = new FixtureCreator();
        Person target = fixture.createCharlie();

        long elapsedNoIncludes = 0;
        long elapsedIncludes = 0;
        for( int i = 0; i < 100; i++ ) {
            elapsedNoIncludes += timeSerialization(target);
            elapsedIncludes += timeSerialization(target, "birthdate");
        }
        double performanceHit = elapsedNoIncludes / (double)elapsedIncludes; 
        assertTrue( "Performance comparison was outside tolerance: 0.6 < " + performanceHit + " < 1.1",  performanceHit > 0.6 && performanceHit < 1.1 );

        long deepElapsed = 0;
        long wildcardElapsed = 0;
        for( int i = 0; i < 100; i++ ) {
            deepElapsed += timeDeepSerialization(target);
            wildcardElapsed += timeDeepSerialization(target, "*");
        }
        double deepPerformance = deepElapsed / (double)wildcardElapsed;
        assertTrue(  "Performance comparison was outside tolerance: 0.9 < " + deepPerformance + " < 2.0", deepPerformance > 0.9 && deepPerformance < 2.0 );
    }

    private long timeSerialization(Object target, String... includes ) {
        long start = System.nanoTime();
        String json = new JSONSerializer().include(includes).serialize( target );
        long end = System.nanoTime();
        return end-start;
    }

    private long timeDeepSerialization(Object target, String... includes ) {
        long start = System.nanoTime();
        String json = new JSONSerializer().include(includes).deepSerialize( target );
        long end = System.nanoTime();
        return end-start;
    }

    @Ignore("Not reliable test.")
    public void testDeserializationPerformance() {

    }

    public static Test suite() {
        return new TestSuite(PerformanceTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
