/**
 * Copyright 2007 Charlie Hubbard and Brandon Goodin
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

import flexjson.transformer.Transformer;
import flexjson.transformer.TypeTransformerMap;

import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * JSONSerializer is the main class for performing serialization of Java objects
 * to JSON.  JSONSerializer by default performs a shallow serialization.  While
 * this might seem strange there is a method to this madness.  Shallow serialization
 * allows the developer to control what is serialized out of the object graph.
 * This helps with performance, but more importantly makes good OO possible, fixes
 * the circular reference problem, and doesn't require boiler plate translation code.
 * You don't have to change your object model to make JSON work so it reduces your
 * work load, and keeps you
 * <a href="http://en.wikipedia.org/wiki/Don't_repeat_yourself">DRY</a>.
 * </p>
 * <p/>
 * <p>
 * Let's go through a simple example:
 * </p>
 * <p/>
 * <pre>
 *    JSONSerializer serializer = new JSONSerializer();
 *    return serializer.serialize( person );
 * <p/>
 * </pre>
 * <p/>
 * <p>
 * What this statement does is output the json from the instance of person.  So
 * the JSON we might see for this could look like:
 * </p>
 * <p/>
 * <pre>
 *    { "class": "com.mysite.Person",
 *      "firstname": "Charlie",
 *      "lastname": "Rose",
 *      "age", 23
 *      "birthplace": "Big Sky, Montanna"
 *    }
 * <p/>
 * </pre>
 * <p>
 * In this case it's look like it's pretty standard stuff.  But, let's say
 * Person had many hobbies (i.e. Person.hobbies is a java.util.List).  In
 * this case if we executed the code above we'd still get the same output.
 * This is a very important feature of flexjson, and that is any instance
 * variable that is a Collection, Map, or Object reference won't be serialized
 * by default.  This is what gives flexjson the shallow serialization.
 * </p>
 * <p/>
 * <p>
 * How would we include the <em>hobbies</em> field?  Using the {@link JsonSerializer#include}
 * method allows us to include these fields in the serialization process.  Here is
 * how we'd do that:
 * </p>
 * <p/>
 * <pre>
 *    return new JSONSerializer().include("hobbies").serialize( person );
 * <p/>
 * </pre>
 * <p/>
 * That would produce output like:
 * <p/>
 * <pre>
 *    { "class": "com.mysite.Person",
 *      "firstname": "Charlie",
 *      "lastname": "Rose",
 *      "age", 23
 *      "birthplace": "Big Sky, Montanna",
 *      "hobbies", [
 *          "poker",
 *          "snowboarding",
 *          "kite surfing",
 *          "bull riding"
 *      ]
 *    }
 * <p/>
 * </pre>
 * <p/>
 * <p>
 * If the <em>hobbies</em> field contained objects, say Hobby instances, then a
 * shallow copy of those objects would be performed.  Let's go further and say
 * <em>hobbies</em> had a List of all the people who enjoyed this hobby.
 * This would create a circular reference between Person and Hobby.  Since the
 * shallow copy is being performed on Hobby JSONSerialize won't serialize the people
 * field when serializing Hobby instances thus breaking the chain of circular references.
 * </p>
 * <p/>
 * <p>
 * But, for the sake of argument and illustration let's say we wanted to send the
 * <em>people</em> field in Hobby.  We can do the following:
 * </p>
 * <p/>
 * <pre>
 *    return new JSONSerializer().include("hobbies.people").serialize( person );
 * <p/>
 * </pre>
 * <p/>
 * <p>
 * JSONSerializer is smart enough to know that you want <em>hobbies</em> field included and
 * the <em>people</em> field inside hobbies' instances too.  The dot notation allows you
 * do traverse the object graph specifying instance fields.  But, remember a shallow copy
 * will stop the code from getting into an infinte loop.
 * </p>
 * <p/>
 * <p>
 * You can also use the exclude method to exclude fields that would be included.  Say
 * we have a User object.  It would be a serious security risk if we sent the password
 * over the network.  We can use the exclude method to prevent the password field from
 * being sent.
 * </p>
 * <p/>
 * <pre>
 *   return new JSONSerialize().exclude("password").serialize(user);
 * <p/>
 * </pre>
 * <p/>
 * <p>
 * JSONSerializer will also pay attention to any method or field annotated by
 * {@link flexjson.JSON}.  You can include and exclude fields permenantly using the
 * annotation.  This is good like in the case of User.password which should never
 * ever be sent through JSON.  However, fields like <em>hobbies</em> or
 * <em>favoriteMovies</em> depends on the situation so it's best NOT to annotate
 * those fields, and use the {@link JsonSerializer#include} method.
 * </p>
 * <p/>
 * <p>
 * In a shallow copy only these types of instance fields will be sent:
 * <strong>String</strong>, <strong>Date</strong>, <strong>Number</strong>,
 * <strong>Boolean</strong>, <strong>Character</strong>, <strong>Enum</strong>,
 * <strong>Object</strong> and <strong>null</strong>.  Subclasses of Object will be serialized
 * except for Collection or Arrays.  Anything that would cause a N objects would not be sent.
 * All types will be excluded by default.  Fields marked static or transient are not serialized.
 * </p>
 * <p>
 * Includes and excludes can include wildcards.  Wildcards allow you to do things like exclude
 * all class attributes.  For example *.class would remove the class attribute that all objects
 * have when serializing.  A open ended wildcard like * would cause deep serialization to take
 * place.  Be careful with that one.  Although you can limit it's depth with an exclude like
 * *.foo.  The order of evaluation of includes and excludes is the order in which you called their
 * functions.  First call to those functions will cause those expressions to be evaluated first.
 * The first expression to match a path that action will be taken thus short circuiting all other
 * expressions defined later.
 * </p>
 * <p>
 * Transforers are a new addition that allow you to modify the values that are being serialized.
 * This allows you to create different output for certain conditions.  This is very important in
 * web applications.  Say you are saving your text to the DB that could contain &lt; and &gt;.  If
 * you plan to add that content to your HTML page you'll need to escape those characters.  Transformers
 * allow you to do this.  Flexjson ships with a simple HTML encoder {@link flexjson.transformer.HtmlEncoderTransformer}.
 * Transformers are specified in dot notation just like include and exclude methods, but it doesn't
 * support wildcards.
 * </p>
 * <p>
 * JSONSerializer is safe to use the serialize() methods from two seperate
 * threads.  It is NOT safe to use combination of {@link JsonSerializer#include(String[])}
 * {@link JsonSerializer#transform(Transformer, String[])}, or {@link flexjson.JsonSerializer#exclude(String[])}
 * from multiple threads at the same time.  It is also NOT safe to use
 * {@link flexjson.JsonSerializer#serialize(Object)} and include/exclude/transform from
 * multiple threads.  The reason for not making them more thread safe is to boost performance.
 * Typical use case won't call for two threads to modify the JsonSerializer at the same type it's
 * trying to serialize.
 * </p>
 */
public class JsonSerializer {

    public final static char[] HEX = "0123456789ABCDEF".toCharArray();

    private OutputHandler out;

    private TypeTransformerMap typeTransformerMap = new TypeTransformerMap();
    private Map<Path, Transformer> pathTransformerMap = new HashMap<Path, Transformer>();

    private List<PathExpression> pathExpressions = new ArrayList<PathExpression>();

    private String rootName;
    private boolean prettyPrint = false;

    // OutputHander Configuration

    /**
     * This passes the generated JSON into the provided Writer.
     * This can be used to stream JSON back to a browser rather
     * than wait for it to all complete and then dump it all at
     * once like the StringBufferOutputHandler and StringBuilderOutputHandler
     *
     * @param out an implementation of java.io.Writer
     * @return this instance of JsonSerializer for chaining configs
     */
    public JsonSerializer out(Writer out) {
        this.out = new WriterOutputHandler(out);
        return this;
    }

    /**
     * This uses the passed in StringBuilder to write the JSON output to.
     *
     * @param out - SringBuilder to write to
     * @return this JsonSerializer for chaining configurations
     */
    public JsonSerializer out(StringBuilder out) {
        this.out = new StringBuilderOutputHandler(out);
        return this;
    }

    /**
     * This uses the passed in StringBuilder to write the JSON output to
     *
     * @param out - StringBuffer to write to
     * @return this JsonSerializer for chaining configurations
     */
    public JsonSerializer out(StringBuffer out) {
        this.out = new StringBufferOutputHandler(out);
        return this;
    }

    /**
     * format output with indentations
     *
     * @param prettyPrint
     */
    public JsonSerializer prettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    /**
     * This wraps the resulting JSON in a javascript object that contains a single
     * field named rootName.  This is great to use in conjunction with other libraries
     * like EXTJS whose data models require them to be wrapped in a JSON object.
     *
     * @param rootName - name to assign to root object
     * @return this JsonSerializer for chaining configurations
     */
    public JsonSerializer rootName(String rootName) {
        this.rootName = rootName;
        return this;
    }

    // SERIALIZATION

    /**
     * This performs a shallow serialization of the target instance.
     *
     * @param target the instance to serialize to JSON
     */
    public String serialize(Object target) {
        serialize(target, SerializationType.SHALLOW);
        return getJson();
    }

    /**
     * This performs a deep serialization of the target instance.  It will include
     * all collections, maps, and arrays by default so includes are ignored except
     * if you want to include something being excluded by an annotation.  Excludes
     * are honored.  However, cycles in the target's graph are NOT followed.  This
     * means some members won't be included in the JSON if they would create a cycle.
     * Rather than throwing an exception the cycle creating members are simply not
     * followed.
     *
     * @param target the instance to serialize to JSON.
     */
    public String deepSerialize(Object target) {
        serialize(target, SerializationType.DEEP);
        return getJson();
    }

    protected void serialize(Object target, SerializationType serializationType) {
        // initialize context
        JsonContext context = JsonContext.get();
        if (out == null) out = new StringBuilderOutputHandler(new StringBuilder());
        context.setOut(out);
        context.setPrettyPrint(prettyPrint);
        context.serializationType(serializationType);
        context.addTypeTransformers(typeTransformerMap);
        context.addPathTransformers(pathTransformerMap);
        context.addPathExpressions(pathExpressions);

        try {

            //initiate serialization of target tree
            if (rootName == null || rootName.trim().equals("")) {
                context.transform(target);
            } else {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(rootName, target);
                context.transform(map);
            }


        } finally {
            // cleanup context
            JsonContext.cleanup();

        }
    }

    // TRANSFORMER CONFIGURATIONS

    /**
     * This adds a Transformer used to manipulate the value of all the fields you give it.
     * Fields can be in dot notation just like {@link flexjson.JsonSerializer#include} and
     * {@link flexjson.JsonSerializer#exclude } methods.  However, transform doesn't support wildcards.
     * Specifying more than one field allows you to add a single instance to multiple fields.
     * It's there for handiness. :-)
     *
     * @param transformer the instance used to transform values
     * @param fields      the paths to the fields you want to transform.  They can be in dot notation.
     * @return Hit you back with the JSONSerializer for method chain goodness.
     */
    public JsonSerializer transform(Transformer transformer, String... fields) {
        for (String field : fields) {
            if (field.length() == 0) {
                pathTransformerMap.put(new Path(), transformer);
            } else {
                pathTransformerMap.put(new Path(field.split("\\.")), transformer);
            }
        }
        return this;
    }


    /**
     * This adds a Transformer used to manipulate the value of all fields that match the type.
     *
     * @param transformer the instance used to transform values
     * @param types       you want to transform.
     * @return Hit you back with the JSONSerializer for method chain goodness.
     */
    public JsonSerializer transform(Transformer transformer, Class... types) {

        for (Class type : types) {
            typeTransformerMap.put(type, transformer);
        }

        return this;
    }

    // INCLUDE/EXCLUDE CONFIGURATION

    protected void addExclude(String field) {
        int index = field.lastIndexOf('.');
        if (index > 0) {
            PathExpression expression = new PathExpression(field.substring(0, index), true);
            if (!expression.isWildcard()) {
                pathExpressions.add(expression);
            }
        }
        pathExpressions.add(new PathExpression(field, false));
    }

    protected void addInclude(String field) {
        pathExpressions.add(new PathExpression(field, true));
    }

    /**
     * This takes in a dot expression representing fields
     * to exclude when serialize method is called.  You
     * can hand it one or more fields.  Example are: "password",
     * "bankaccounts.number", "people.socialsecurity", or
     * "people.medicalHistory".  In exclude method dot notations
     * will only exclude the final field (i.e. rightmost field).
     * All the fields to the left of the last field will be included.
     * In order to exclude the medicalHistory field we have to
     * include the people field since people would've been excluded
     * anyway since it's a Collection of Person objects.  The order of
     * evaluation is the order in which you call the exclude method.
     * The first call to exclude will be evaluated before other calls to
     * include or exclude.  The field expressions are evaluated in order
     * you pass to this method.
     *
     * @param fields one or more field expressions to exclude.
     * @return this instance for method chaining.
     */
    public JsonSerializer exclude(String... fields) {
        for (String field : fields) {
            addExclude(field);
        }
        return this;
    }

    /**
     * This takes in a dot expression representing fields to
     * include when serialize method is called.  You can hand
     * it one or more fields.  Examples are: "hobbies",
     * "hobbies.people", "people.emails", or "character.inventory".
     * When using dot notation each field between the dots will
     * be included in the serialization process.  The order of
     * evaluation is the order in which you call the include method.
     * The first call to include will be evaluated before other calls to
     * include or exclude.  The field expressions are evaluated in order
     * you pass to this method.
     *
     * @param fields one or more field expressions to include.
     * @return this instance for method chaining.
     */
    public JsonSerializer include(String... fields) {
        for (String field : fields) {
            addInclude(field);
        }
        return this;
    }

    // INCLUDE/EXCLUDE TEST/DEBUG HOOKS

    /**
     * Return the fields included in serialization.  These fields will be in dot notation.
     *
     * @return A List of dot notation fields included in serialization.
     */
    public List<PathExpression> getIncludes() {
        List<PathExpression> expressions = new ArrayList<PathExpression>();
        for (PathExpression expression : pathExpressions) {
            if (expression.isIncluded()) {
                expressions.add(expression);
            }
        }
        return expressions;
    }

    /**
     * Return the fields excluded from serialization.  These fields will be in dot notation.
     *
     * @return A List of dot notation fields excluded from serialization.
     */
    public List<PathExpression> getExcludes() {
        List<PathExpression> excludes = new ArrayList<PathExpression>();
        for (PathExpression expression : pathExpressions) {
            if (!expression.isIncluded()) {
                excludes.add(expression);
            }
        }
        return excludes;
    }


    /**
     * Sets the fields included in serialization.  These fields must be in dot notation.
     * This is just here so that JSONSerializer can be treated like a bean so it will
     * integrate with Spring or other frameworks.  <strong>This is not ment to be used
     * in code use include method for that.</strong>
     *
     * @param fields the list of fields to be included for serialization.  The fields arg should be a
     *               list of strings in dot notation.
     */
    public void setIncludes(List<String> fields) {
        for (String field : fields) {
            pathExpressions.add(new PathExpression(field, true));
        }
    }

    /**
     * Sets the fields excluded in serialization.  These fields must be in dot notation.
     * This is just here so that JSONSerializer can be treated like a bean so it will
     * integrate with Spring or other frameworks.  <strong>This is not ment to be used
     * in code use exclude method for that.</strong>
     *
     * @param fields the list of fields to be excluded for serialization.  The fields arg should be a
     *               list of strings in dot notation.
     */
    public void setExcludes(List<String> fields) {
        for (String field : fields) {
            addExclude(field);
        }
    }

    /**
     * This retrieves the toString value of the configured OutputHandler.
     * <p/>
     * In cases where a response writer is passed in this will not be needed
     * because the generated JSON will be streamed back to the client.
     *
     * @return
     */
    public String getJson() {
        return out.toString();
    }

}
