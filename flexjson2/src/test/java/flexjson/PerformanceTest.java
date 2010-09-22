package flexjson;

import flexjson.mock.Person;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class PerformanceTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testSerializationPerformance() {
        FixtureCreator fixture = new FixtureCreator();
        Person target = fixture.createCharlie();
        long elapsedNoIncludes = timeSerialization(target);
        long elapsedIncludes = timeSerialization(target, "birthdate");
        double performanceHit = elapsedNoIncludes / (double)elapsedIncludes; 
        assertTrue( "Performance comparison was outside tolerance: 0.9 < " + performanceHit + " < 1.1",  performanceHit > 0.9 && performanceHit < 1.1 );

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

    public void testDeserializationPerformance() {

    }

    public static Test suite() {
        return new TestSuite(PerformanceTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
