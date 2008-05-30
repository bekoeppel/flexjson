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
package flexjson;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class PathExpressionTest extends TestCase {


    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testExpressionMatching() {
        assertTrue("Assert 'hello' matches 'hello'", new PathExpression("hello", true).matches(new Path("hello")));
        assertFalse("Assert 'hello' doesn't match 'noob'", new PathExpression("hello", true).matches(new Path("noob")));
        assertTrue("Assert 'hello.world' matches 'hello,world", new PathExpression("hello.world", true).matches(new Path("hello", "world")));
        assertFalse("Assert 'hello' does not match empty path", new PathExpression("hello", true).matches(new Path()));
        assertTrue("Assert that 'hello.*.world' does match 'hello,cat,world'", new PathExpression("hello.*.world", true).matches(new Path("hello", "cat", "world")));
        assertTrue("Assert that 'hello.*.world' does match 'hello, cat, dog, world'", new PathExpression("hello.*.world", true).matches(new Path("hello", "cat", "dog", "world")));
        assertTrue("Assert that '*.class' matches 'cat, class'", new PathExpression("*.class", true).matches(new Path("cat", "class")));
        assertTrue("Assert that '*.class' matches 'cat, dog, sheep, class'", new PathExpression("*.class", true).matches(new Path("cat", "dog", "sheep", "class")));
        assertFalse("Assert that '*.class' does not match 'cat, dog, sheep, diggums'", new PathExpression("*.class", true).matches(new Path("cat", "dog", "sheep", "diggums")));
        assertTrue("Assert that '*' matches 'cat, dog, sheep, cow'", new PathExpression("*", true).matches(new Path("cat", "dog", "sheep", "cow")));
        assertTrue("Assert that '*.class.*' matches 'billy.bong.class.yeker'", new PathExpression("*.class.*", true).matches(new Path("billy", "bong", "class", "yeker")));
        assertTrue("Asser that '*' will match anything.", new PathExpression("*", true).matches(new Path("123", "8923", "fuggly", "buggly")));
        assertTrue("Assert that '*.*' matches 'billy.bong.class.yeker'", new PathExpression("*.*", true).matches(new Path("billy", "bong", "class", "yeker")));
    }

    public static Test suite() {
        return new TestSuite(PathExpressionTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }
}
