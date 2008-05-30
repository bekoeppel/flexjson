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

public class PathTest extends TestCase {
    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testEqual() {
        Path path = new Path("foo", "bar", "baz");
        Path equalPath = new Path("foo", "bar", "baz");
        Path notEqualPath = new Path("Foo", "bar", "hizzle");
        Path tooBigPath = new Path("foo", "bar", "baz", "sherly");

        assertEquals("Assert that two paths with the same data are equal", equalPath, path);
        assertFalse("Assert that two paths with the same data are equal", path.equals(notEqualPath));
        assertFalse("Assert that two paths with the same data are equal", path.equals(tooBigPath));
        assertFalse("Assert that if we enqueue onto a path then we get something not equal", path.equals(equalPath.enqueue("snickers")));
        equalPath.pop();
        assertEquals("Assert that if we pop that same path then we get equal again", path, equalPath);
    }

    public void testEnqueAndPop() {
        Path path = new Path();
        assertEquals("Assert that our length is 0 when empty", 0, path.length());
        assertEquals("Assert after appending we have a length of 1", 1, path.enqueue("hello").length());
        assertEquals("Assert after appending we have a length of 2", 2, path.enqueue("world").length());
        assertEquals("Assert that after we pop we have the last thing we placed on there", "world", path.pop());
        assertEquals("Assert that after we pop we are back to 1", 1, path.length());
        assertEquals("Assert that after we pop we have the last thing we placed on there", "hello", path.pop());
        assertEquals("Assert that after we pop we are back to 0", 0, path.length());

        Path foobarbaz = new Path("foo", "bar", "baz");
        assertEquals("Assert that our path is 3.", 3, foobarbaz.length());
    }

    public static Test suite() {
        return new TestSuite(PathTest.class);
    }

    public static void main(String[] args) {
        TestRunner.run(suite());
    }

}
