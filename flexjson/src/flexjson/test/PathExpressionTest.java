package flexjson.test;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import flexjson.Path;
import flexjson.PathExpression;

/**
 * Created by IntelliJ IDEA.
 * User: charlie
 * Date: Jun 28, 2007
 * Time: 8:51:35 AM
 */
public class PathExpressionTest extends TestCase {


    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testExpressionMatching() {
        assertTrue( "Assert 'hello' matches 'hello'", new PathExpression("hello").matches( new Path("hello") ) );
        assertFalse( "Assert 'hello' doesn't match 'noob'", new PathExpression("hello").matches( new Path("noob") ) );
        assertTrue( "Assert 'hello.world' matches 'hello,world",new PathExpression("hello.world").matches( new Path("hello", "world") ) );
        assertFalse( "Assert 'hello' does not match empty path", new PathExpression("hello").matches( new Path() ) );
        assertTrue( "Assert that 'hello.*.world' does match 'hello,cat,world'", new PathExpression("hello.*.world").matches( new Path("hello","cat","world") ) );
        assertTrue( "Assert that 'hello.*.world' does match 'hello, cat, dog, world'", new PathExpression("hello.*.world").matches( new Path("hello","cat","dog", "world") ) );
        assertTrue( "Assert that '*.class' matches 'cat, class'", new PathExpression("*.class").matches( new Path("cat", "class") ) );
        assertTrue( "Assert that '*.class' matches 'cat, dog, sheep, class'", new PathExpression("*.class").matches( new Path("cat", "dog", "sheep", "class" ) ) );
        assertFalse( "Assert that '*.class' does not match 'cat, dog, sheep, diggums'", new PathExpression("*.class").matches( new Path("cat", "dog", "sheep", "diggums" ) ) );
        assertTrue( "Assert that '*' matches 'cat, dog, sheep, cow'", new PathExpression("*").matches( new Path("cat", "dog", "sheep", "cow" ) ) );
        assertTrue( "Assert that '*.class.*' matches 'billy.bong.class.yeker'", new PathExpression("*.class.*").matches( new Path("billy", "bong", "class", "yeker" ) ) );
        assertTrue( "Asser that '*' will match anything.", new PathExpression("*").matches( new Path("123", "8923", "fuggly", "buggly" ) ) );
        assertTrue( "Assert that '*.*' matches 'billy.bong.class.yeker'", new PathExpression("*.*").matches( new Path("billy", "bong", "class", "yeker" ) ) );
    }

    public static Test suite() {
        return new TestSuite( PathExpressionTest.class );
    }

    public static void main(String[] args) {
        TestRunner.run( suite() );
    }
}
