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

import java.util.*;
import java.util.Date;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

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
 *
 * <p>
 * Let's go through a simple example:
 * </p>
 *
 * <pre>
 *    JSONSerializer serializer = new JSONSerializer();
 *    return serializer.serialize( person );
 *
 * </pre>
 *
 * <p>
 * What this statement does is output the json from the instance of person.  So
 * the JSON we might see for this could look like:
 * </p>
 *
 * <pre>
 *    { "class": "com.mysite.Person",
 *      "firstname": "Charlie",
 *      "lastname": "Rose",
 *      "age", 23
 *      "birthplace": "Big Sky, Montanna"
 *    }
 *
 * </pre>
 * <p>
 * In this case it's look like it's pretty standard stuff.  But, let's say
 * Person had many hobbies (i.e. Person.hobbies is a java.util.List).  In
 * this case if we executed the code above we'd still get the same output.
 * This is a very important feature of flexjson, and that is any instance
 * variable that is a Collection, Map, or Object reference won't be serialized
 * by default.  This is what gives flexjson the shallow serialization.
 * </p>
 *
 * <p>
 * How would we include the <em>hobbies</em> field?  Using the {@link flexjson.JSONSerializer#include}
 * method allows us to include these fields in the serialization process.  Here is
 * how we'd do that:
 * </p>
 *
 * <pre>
 *    return new JSONSerializer().include("hobbies").serialize( person );
 *
 * </pre>
 *
 * That would produce output like:
 *
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
 *
 * </pre>
 *
 * <p>
 * If the <em>hobbies</em> field contained objects, say Hobby instances, then a
 * shallow copy of those objects would be performed.  Let's go further and say
 * <em>hobbies</em> had a List of all the people who enjoyed this hobby.
 * This would create a circular reference between Person and Hobby.  Since the
 * shallow copy is being performed on Hobby JSONSerialize won't serialize the people
 * field when serializing Hobby instances thus breaking the chain of circular references.
 * </p>
 *
 * <p>
 * But, for the sake of argument and illustration let's say we wanted to send the
 * <em>people</em> field in Hobby.  We can do the following:
 * </p>
 *
 * <pre>
 *    return new JSONSerializer().include("hobbies.people").serialize( person );
 *
 * </pre>
 *
 * <p>
 * JSONSerializer is smart enough to know that you want <em>hobbies</em> field included and
 * the <em>people</em> field inside hobbies' instances too.  The dot notation allows you
 * do traverse the object graph specifying instance fields.  But, remember a shallow copy
 * will stop the code from getting into an infinte loop.
 * </p>
 *
 * <p>
 * You can also use the exclude method to exclude fields that would be included.  Say
 * we have a User object.  It would be a serious security risk if we sent the password
 * over the network.  We can use the exclude method to prevent the password field from
 * being sent.
 * </p>
 *
 * <pre>
 *   return new JSONSerialize().exclude("password").serialize(user);
 *
 * </pre>
 *
 * <p>
 * JSONSerializer will also pay attention to any method or field annotated by
 * {@link flexjson.JSON}.  You can include and exclude fields permenantly using the
 * annotation.  This is good like in the case of User.password which should never
 * ever be sent through JSON.  However, fields like <em>hobbies</em> or
 * <em>favoriteMovies</em> depends on the situation so it's best NOT to annotate
 * those fields, and use the {@link flexjson.JSONSerializer#include} method.
 * </p>
 *
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
 * allow you to do this.  Flexjson ships with a simple HTML encoder {@link flexjson.HTMLEncoder}.
 * Transformers are specified in dot notation just like include and exclude methods, but it doesn't
 * support wildcards.
 * </p>
 * <p>
 * JSONSerializer is safe to use the serialize() methods from two seperate
 * threads.  It is NOT safe to use combination of {@link flexjson.JSONSerializer#include(String[])}
 * {@link JSONSerializer#transform(Transformer, String[])}, or {@link flexjson.JSONSerializer#exclude(String[])}
 * from multiple threads at the same time.  It is also NOT safe to use
 * {@link flexjson.JSONSerializer#serialize(String, Object)} and include/exclude/transform from
 * multiple threads.  The reason for not making them more thread safe is to boost performance.
 * Typical use case won't call for two threads to modify the JSONSerializer at the same type it's
 * trying to serialize.
 * </p>
 */
public class JSONSerializer {

    public final static char[] HEX = "0123456789ABCDEF".toCharArray();

    List<PathExpression> pathExpressions = new ArrayList<PathExpression>();
    Map<Path, Transformer> transformations = new HashMap<Path,Transformer>();

    /**
     * Create a serializer instance.  It's unconfigured in terms of fields
     * it should include or exclude.
     */
    public JSONSerializer() {
    }

    /**
     * This performs a shallow serialization of target instance.  It wraps
     * the resulting JSON in a javascript object that contains a single field
     * named rootName.  This is great to use in conjunction with other libraries
     * like EXTJS whose data models require them to be wrapped in a JSON object.
     * 
     * @param rootName the name of the field to assign the resulting JSON.
     * @param target the instance to serialize to JSON.
     * @return the JSON object with one field named rootName and the value being the JSON of target.
     */
    public String serialize( String rootName, Object target ) {
        return new ShallowVisitor().visit( rootName, target );
    }

    /**
     * This performs a shallow serialization of the target instance.
     *
     * @param target the instance to serialize to JSON
     * @return the JSON representing the target instance.
     */
    public String serialize( Object target ) {
        return new ShallowVisitor().visit( target );
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
     * @return the JSON representing the target instance deep serialization.
     */
    public String deepSerialize( Object target ) {
        return new DeepVisitor().visit( target );
    }

    /**
     * This performs a deep serialization of target instance.  It wraps
     * the resulting JSON in a javascript object that contains a single field
     * named rootName.  This is great to use in conjunction with other libraries
     * like EXTJS whose data models require them to be wrapped in a JSON object.
     * See {@link flexjson.JSONSerializer#deepSerialize(Object)} for more
     * in depth explaination.
     *
     * @param rootName the name of the field to assign the resulting JSON.
     * @param target the instance to serialize to JSON.
     * @return the JSON object with one field named rootName and the value being the JSON of target.
     */
    public String deepSerialize( String rootName, Object target ) {
        return new DeepVisitor().visit( rootName, target );
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
    public JSONSerializer exclude( String... fields ) {
        for( String field : fields ) {
            addExclude( field );
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
    public JSONSerializer include( String... fields ) {
        for( String field : fields ) {
            pathExpressions.add( new PathExpression( field, true ) );
        }
        return this;
    }

    /**
     * This adds a Transformer used to manipulate the value of all the fields you give it.
     * Fields can be in dot notation just like {@link JSONSerializer#include} and
     * {@link JSONSerializer#exclude } methods.  However, transform doesn't support wildcards.
     * Specifying more than one field allows you to add a single instance to multiple fields.
     * It's there for handiness. :-) 
     * @param transformer the instance used to transform values
     * @param fields the paths to the fields you want to transform.  They can be in dot notation.
     * @return Hit you back with the JSONSerializer for method chain goodness.
     */
    public JSONSerializer transform( Transformer transformer, String... fields ) {
        for( String field : fields ) {
            if( field.length() == 0 ) {
                transformations.put( new Path(), transformer );
            } else {
                transformations.put( new Path( field.split("\\.") ), transformer );
            }
        }
        return this;
    }

    /**
     * Return the fields included in serialization.  These fields will be in dot notation.
     *
     * @return A List of dot notation fields included in serialization.
     */
    public List<PathExpression> getIncludes() {
        List<PathExpression> expressions = new ArrayList<PathExpression>();
        for( PathExpression expression : pathExpressions ) {
            if( expression.isIncluded() ) {
                expressions.add( expression );
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
        for( PathExpression expression : pathExpressions ) {
            if( !expression.isIncluded() ) {
                excludes.add( expression );
            }
        }
        return excludes;
    }

    /**
     * Sets the fields included in serialization.  These fields must be in dot notation.
     * This is just here so that JSONSerializer can be treated like a bean so it will
     * integrate with Spring or other frameworks.  <strong>This is not ment to be used
     * in code use include method for that.</strong>
     * @param fields the list of fields to be included for serialization.  The fields arg should be a
     * list of strings in dot notation.
     */
    public void setIncludes( List fields ) {
        for( Object field : fields ) {
            pathExpressions.add( new PathExpression( field.toString(), true ) );
        }
    }

    /**
     * Sets the fields excluded in serialization.  These fields must be in dot notation.
     * This is just here so that JSONSerializer can be treated like a bean so it will
     * integrate with Spring or other frameworks.  <strong>This is not ment to be used
     * in code use exclude method for that.</strong>
     * @param fields the list of fields to be excluded for serialization.  The fields arg should be a 
     * list of strings in dot notation.
     */
    public void setExcludes( List fields ) {
        for( Object field : fields ) {
            addExclude( field );
        }
    }

    private void addExclude(Object field) {
        String name = field.toString();
        int index = name.lastIndexOf('.');
        if( index > 0 ) {
            PathExpression expression = new PathExpression( name.substring( 0, index ), true );
            if( !expression.isWildcard() ) {
                pathExpressions.add( expression );
            }
        }
        pathExpressions.add( new PathExpression( name, false ) );
    }

    /**
     * This will do a serialize the target and pretty print the output so it's easier to read.
     *
     * @param target of the serialization.
     * @return the serialized representation of the target in pretty print form.
     */
    public String prettyPrint( Object target ) {
        return new ShallowVisitor( true ).visit( target );
    }

    /**
     * This will do a serialize with root name and pretty print the output so it's easier to read.
     *
     * @param rootName the name of the field to assign the resulting JSON.
     * @param target of the serialization.
     * @return the serialized representation of the target in pretty print form.
     */
    public String prettyPrint( String rootName, Object target ) {
        return new ShallowVisitor( true ).visit( rootName, target );
    }

    private abstract class ObjectVisitor {
        protected StringBuilder builder;
        protected boolean prettyPrint = false;
        private int amount = 0;
        private boolean insideArray = false;
        private Path path;

        protected ObjectVisitor() {
            builder = new StringBuilder();
            path = new Path();
        }

        public ObjectVisitor(boolean prettyPrint) {
            this();
            this.prettyPrint = prettyPrint;
        }

        public String visit( Object target ) {
            json( target );
            return builder.toString();
        }

        public String visit( String rootName, Object target ) {
            beginObject();
            string(rootName);
            add(':');
            json( target );
            endObject();
            return builder.toString();
        }

        private void json(Object object) {
            if (object == null) add("null");
            else if (object instanceof Class)
                string( ((Class)object).getName() );
            else if (object instanceof Boolean)
                bool( ((Boolean) object) );
            else if (object instanceof Number)
                add( doTransform( object ) );
            else if (object instanceof String)
                string(object);
            else if (object instanceof Character)
                string(object);
            else if (object instanceof Map)
                map( (Map)object);
            else if (object.getClass().isArray())
                array( object );
            else if (object instanceof Iterable)
                array(((Iterable) object).iterator() );
            else if( object instanceof Date)
                date( (Date)object );
            else if( object instanceof Enum )
                enumerate( (Enum)object );
            else
                bean( object );
        }

        private void enumerate(Enum value) {
            string( value.name() );
        }

        private void map(Map map) {
            beginObject();
            Iterator it = map.keySet().iterator();
            boolean firstField = true;
            while (it.hasNext()) {
                Object key = it.next();
                int len = builder.length();
                add( key, map.get(key), firstField );
                if( len < builder.length() ) {
                    firstField = false;
                }
            }
            endObject();
        }

        private void array(Iterator it) {
            beginArray();
            while (it.hasNext()) {
                if( prettyPrint ) {
                    addNewline();
                }
                addArrayElement( it.next(), it.hasNext() );
            }
            endArray();
        }

        private void array(Object object) {
            beginArray();
            int length = Array.getLength(object);
            for (int i = 0; i < length; ++i) {
                if( prettyPrint ) {
                    addNewline();
                }
                addArrayElement( Array.get(object, i), i < length - 1 );
            }
            endArray();
        }

        private void addArrayElement(Object object, boolean isLast ) {
            int len = builder.length();
            json( object );
            if( len < builder.length() ) { // make sure we at least added an element.
                if ( isLast ) add(',');
            }
        }

        private void bool(Boolean b) {
            add( b ? "true" : "false" );
        }

        private void string(Object obj) {
            add('"');
            CharacterIterator it = new StringCharacterIterator( doTransform( obj ).toString() );
            for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
                if (c == '"') add("\\\"");
                else if (c == '\\') add("\\\\");
                // else if (c == '/') add("\\/");
                else if (c == '\b') add("\\b");
                else if (c == '\f') add("\\f");
                else if (c == '\n') add("\\n");
                else if (c == '\r') add("\\r");
                else if (c == '\t') add("\\t");
                else if (Character.isISOControl(c)) {
                    unicode(c);
                } else {
                    add(c);
                }
            }
            add('"');
        }

        private void date(Date date) {
            if( transformations.containsKey( path ) ) {
                string( date.getTime() );
            } else {
                builder.append( date.getTime() );
            }
        }

        private ChainedSet visits = new ChainedSet( Collections.EMPTY_SET );

        @SuppressWarnings({"unchecked"})
        protected void bean(Object object) {
            if( !visits.contains( object ) ) {
                visits = new ChainedSet( visits );
                visits.add( object );
                beginObject();
                try {
                    BeanInfo info = Introspector.getBeanInfo( findBeanClass( object ) );
                    PropertyDescriptor[] props = info.getPropertyDescriptors();
                    boolean firstField = true;
                    for (PropertyDescriptor prop : props) {
                        String name = prop.getName();
                        path.enqueue( name );
                        Method accessor = prop.getReadMethod();
                        if (accessor != null && isIncluded( prop ) ) {
                            Object value = accessor.invoke(object, (Object[]) null);
                            if( !visits.contains( value ) ) {
                                add(name, value, firstField);
                                firstField = false;
                            }
                        }
                        path.pop();
                    }
                    for( Class current = object.getClass(); current != null; current = current.getSuperclass() ) {
                        Field[] ff = current.getDeclaredFields();
                        for (Field field : ff) {
                            path.enqueue( field.getName() );
                            if (isValidField(field)) {
                                if( !visits.contains( field.get(object) ) ) {
                                    add(field.getName(), field.get(object), firstField);
                                    firstField = false;
                                }
                            }
                            path.pop();
                        }
                    }
                } catch( JSONException e ) {
                    throw e;
                } catch( Exception e ) {
                    throw new JSONException( "Error trying to serialize path: " + path.toString(), e );
                }
                endObject();
                visits = (ChainedSet) visits.getParent();
            }
        }

        private Object doTransform(Object value) {
            if( transformations.containsKey( path ) ) {
                value = transformations.get( path ).transform( value );
            }
            return value;
        }

        private Class<?> findBeanClass(Object object) {
            try {
                Class[] classes = object.getClass().getInterfaces();
                for( Class clazz : classes ) {
                    if( clazz.getName().equals("org.hibernate.proxy.HibernateProxy") ) {
                        Method method = object.getClass().getMethod("getHibernateLazyInitializer");
                        Object initializer = method.invoke( object );
                        Method pmethod = initializer.getClass().getMethod("getPersistentClass");
                        return pmethod.invoke( initializer ).getClass();
                    }
                }
            } catch (IllegalAccessException e) {
            } catch (NoSuchMethodException e) {
            } catch (InvocationTargetException e) {
            }
            return object.getClass();
        }

        protected abstract boolean isIncluded( PropertyDescriptor prop );

        protected boolean isValidField(Field field) {
            return !Modifier.isStatic( field.getModifiers() ) && Modifier.isPublic( field.getModifiers() ) && !Modifier.isTransient( field.getModifiers() );
        }

        protected boolean addComma(boolean firstField) {
            if ( !firstField ) {
                add(',');
            } else {
                firstField = false;
            }
            return firstField;
        }

        protected void beginObject() {
            if( prettyPrint ) {
                if( insideArray ) {
                    indent( amount );
                }
                amount += 4;
            }
            add( '{' );
        }

        protected void endObject() {
            if( prettyPrint ) {
                addNewline();
                amount -= 4;
                indent( amount );
            }
            add( '}' );
        }

        private void beginArray() {
            if( prettyPrint ) {
                amount += 4;
                insideArray = true;
            }
            add('[');
        }

        private void endArray() {
            if( prettyPrint ) {
                addNewline();
                amount -= 4;
                insideArray = false;
                indent( amount );
            }
            add(']');
        }

        protected void add( char c ) {
            builder.append( c );
        }

        private void indent(int amount) {
            for( int i = 0; i < amount; i++ ) {
                builder.append( " " );
            }
        }

        private void addNewline() {
            builder.append("\n");
        }

        protected void add( Object value ) {
            builder.append( value );
        }

        protected void add(Object key, Object value, boolean prependComma) {
            int start = builder.length();
            addComma( prependComma );
            addAttribute( key );

            int len = builder.length();
            json( value );
            if( len == builder.length() ) {
                builder.delete( start, len ); // erase the attribute key we didn't output anything.
            }
        }

        private void addAttribute(Object key) {
            if( prettyPrint ) {
                addNewline();
                indent( amount );
            }
            builder.append("\"");
            builder.append( key );
            builder.append( "\"" );
            builder.append( ":" );
            if( prettyPrint ) {
                builder.append(" ");
            }
        }

        private void unicode(char c) {
            add("\\u");
            int n = c;
            for (int i = 0; i < 4; ++i) {
                int digit = (n & 0xf000) >> 12;
                add(JSONSerializer.HEX[digit]);
                n <<= 4;
            }
        }

        protected PathExpression matches(PropertyDescriptor prop, List<PathExpression> expressions) {
            for( PathExpression expr : expressions ) {
                if( expr.matches( path ) ) {
                    return expr;
                }
            }
            return null;
        }

    }

    private class ShallowVisitor extends ObjectVisitor {

        public ShallowVisitor() {
            super();
        }

        public ShallowVisitor(boolean prettyPrint) {
            super(prettyPrint);
        }

        protected boolean isIncluded( PropertyDescriptor prop ) {
            PathExpression expression = matches( prop, pathExpressions);
            if( expression != null ) {
                return expression.isIncluded();
            }

            Method accessor = prop.getReadMethod();
            if( accessor.isAnnotationPresent( JSON.class ) ) {
                return accessor.getAnnotation(JSON.class).include();
            }

            Class propType = prop.getPropertyType();
            return !(propType.isArray() || Iterable.class.isAssignableFrom(propType) || Map.class.isAssignableFrom(propType));
        }
    }

    private class DeepVisitor extends ObjectVisitor {

        public DeepVisitor() {
            super();
        }

        public DeepVisitor(boolean prettyPrint) {
            super(prettyPrint);
        }

        protected boolean isIncluded( PropertyDescriptor prop ) {
            PathExpression expression = matches( prop, pathExpressions);
            if( expression != null ) {
                return expression.isIncluded();
            }

            Method accessor = prop.getReadMethod();
            if( accessor.isAnnotationPresent( JSON.class ) ) {
                return accessor.getAnnotation(JSON.class).include();
            }

            return true;
        }
    }
}
