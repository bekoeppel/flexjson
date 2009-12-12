package flexjson;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class PerformanceTest extends TestCase {

    public void setUp() {
    }

    public void tearDown() {
    }

    public void testPerformance() {
        
    }

    public static Test suite() {
        return new TestSuite(PerformanceTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}
