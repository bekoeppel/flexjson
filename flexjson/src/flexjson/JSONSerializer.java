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
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * JSONSerializer is the main class for performing serialization of Java objects
 * to JSON.  JSONSerializer by default performs a shallow serialization.  While
 * this might seem strange there is a method to this madness.  Shallow serialization
 * allows the developer to control what is serialized out of the object graph.
 * This helps with performance, but more importantly makes good OO possible, fixes
 * the circular reference problem, and doesn't require boiler plate translation code.
 * You don't have to change your object model to make JSON work so it reduces your
 * work load, and keeps you
 * <a href="http://en.wikipedia.org/wiki/Don't_repeat_yourself">DRY</a>.
 *
 * Let's go through a simple example:
 *
 * <pre>
 *    JSONSerializer serializer = new JSONSerializer();
 *    return serializer.serialize( person );
 *
 * </pre>
 *
 * What this statement does is output the json from the instance of person.  So
 * the JSON we might see for this could look like:
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
 *
 * In this case it's look like it's pretty standard stuff.  But, let's say
 * Person had many hobbies (i.e. Person.hobbies is a java.util.List).  In
 * this case if we executed the code above we'd still get the same output.
 * This is a very important feature of flexjson, and that is any instance
 * variable that is a Collection, Map, or Object reference won't be serialized
 * by default.  This is what gives flexjson the shallow serialization.
 *
 * How would we include the <em>hobbies</em> field?  Using the {@link flexjson.JSONSerializer#include}
 * method allows us to include these fields in the serialization process.  Here is
 * how we'd do that:
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
 * If the <em>hobbies</em> field contained objects, say Hobby instances, then a
 * shallow copy of those objects would be performed.  Let's go further and say
 * <em>hobbies</em> had a List of all the people who enjoyed this hobby.
 * This would create a circular reference between Person and Hobby.  Since the
 * shallow copy is being performed on Hobby JSONSerialize won't serialize the people
 * field when serializing Hobby instances thus breaking the chain of circular references.
 *
 * But, for the sake of argument and illustration let's say we wanted to send the
 * <em>people</em> field in Hobby.  We can do the following:
 *
 * <pre>
 *    return new JSONSerializer().include("hobbies.people").serialize( person );
 *
 * </pre>
 *
 * JSONSerializer is smart enough to know that you want <em>hobbies</em> field included and
 * the <em>people</em> field inside hobbies' instances too.  The dot notation allows you
 * do traverse the object graph specifying instance fields.  But, remember a shallow copy
 * will stop the code from getting into an infinte loop.
 *
 * You can also use the exclude method to exclude fields that would be included.  Say
 * we have a User object.  It would be a serious security risk if we sent the password
 * over the network.  We can use the exclude method to prevent the password field from
 * being sent.
 *
 * <pre>
 *   return new JSONSerialize().exclude("password").serialize(user);
 *
 * </pre>
 *
 * JSONSerializer will also pay attention to any method or field annotated by
 * {@link flexjson.JSON}.  You can include and exclude fields permenantly using the
 * annotation.  This is good like in the case of User.password which should never
 * ever be sent through JSON.  However, fields like <em>hobbies</em> or
 * <em>favoriteMovies</em> depends on the situation so it's best NOT to annotate
 * those fields, and use the {@link flexjson.JSONSerializer#include} method.
 *
 * In a shallow copy only these types of instance fields will be sent:
 * <strong>String</strong>, <strong>Date</strong>, <strong>Number</strong>,
 * <strong>Boolean</strong>, <strong>Character</strong>, <strong>Enum</strong>, and
 * <strong>null</strong>.  All types will be excluded by default.  Fields marked
 * static or transient are not serialized.
 *
 * JSONSerializer is safe to use the serialize() methods from two seperate
 * threads.  It is NOT safe to use combination of {@link flexjson.JSONSerializer#include(String[])}
 * and {@link flexjson.JSONSerializer#exclude(String[])} from multiple threads at the same time.
 * It is also NOT safe to use {@link flexjson.JSONSerializer#serialize(String, Object)} and
 * include/exclude from multiple threads.  The reason for not making them more thread safe is
 * to boost performance.  Typical use case won't call for two threads to modify the
 * JSONSerializer at the same type it's trying to serialize.
 */
public class JSONSerializer {

    public final static char[] HEX = "0123456789ABCDEF".toCharArray();

    Map excludeFields = new HashMap();
    Map includeFields = new HashMap();

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
        return new ObjectVisitor().visit( rootName, target );
    }

    /**
     * This performs a shallow serialization of the target instance.
     * @param target the instance to serialize to JSON
     * @return the JSON representing the target instance.
     */
    public String serialize( Object target ) {
        return new ObjectVisitor().visit( target );
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
     * anyway since it's a Collection of Person objects.
     *
     * @param fields one or more field expressions to exclude.
     * @return this instance for method chaining.
     */
    public JSONSerializer exclude( String... fields ) {
        addFieldsTo(excludeFields, fields);
        return this;
    }

    /**
     * This takes in a dot expression representing fields to
     * include when serialize method is called.  You can hand
     * it one or more fields.  Examples are: "hobbies",
     * "hobbies.people", "people.emails", or "character.inventory".
     * When using dot notation each field between the dots will
     * be included in the serialization process.
     *
     * @param fields one or more field expressions to include.
     * @return this instance for method chaining.
     */
    public JSONSerializer include( String... fields ) {
        addFieldsTo(includeFields, fields );
        return this;
    }

    /**
     * Return the fields included in serialization.  These fields will be in dot notation.
     *
     * @return A List of dot notation fields included in serialization.
     */
    public List getIncludes() {
        return renderFields( includeFields );
    }

    /**
     * Return the fields excluded from serialization.  These fields will be in dot notation.
     *
     * @return A List of dot notation fields excluded from serialization.
     */
    public List getExcludes() {
        return renderFields( excludeFields );
    }

    /**
     * Sets the fields included in serialization.  These fields must be in dot notation.
     * This is just here so that JSONSerializer can be treated like a bean so it will
     * integrate with Spring or other frameworks.  <strong>This is not ment to be used
     * in code use include method for that.</strong>
     */
    public void setIncludes( List fields ) {
        for( Object field : fields ) {
            addFieldsTo( includeFields, field.toString() );
        }
    }

    /**
     * Sets the fields excluded in serialization.  These fields must be in dot notation.
     * This is just here so that JSONSerializer can be treated like a bean so it will
     * integrate with Spring or other frameworks.  <strong>This is not ment to be used
     * in code use exclude method for that.</strong>
     */
    public void setExcludes( List fields ) {
        for( Object field : fields ) {
            addFieldsTo( excludeFields, field.toString() );
        }
    }

    private void addFieldsTo(Map root, String... fields) {
        for (String field : fields) {
            Map current = root;
            String[] paths = field.split("\\.");
            for( int i = 0; i < paths.length; i++ ) {
                String path = paths[i];
                if( !current.containsKey( path ) || current.get(path) == null ) {
                    current.put( path, i + 1 < paths.length ? new HashMap() : null );
                }
                current = (Map)current.get( path );
            }
        }
    }

    private List renderFields(Map includeFields) {
        List fields = new ArrayList( includeFields.size() );
        for( Object key : includeFields.keySet() ) {
            Map children = (Map) includeFields.get( key );
            if( children != null ) {
                List childrenFields = renderFields(children);
                for( Iterator i = childrenFields.iterator(); i.hasNext(); ) {
                    Object child = i.next();
                    fields.add( key + "." + child );
                }
            } else {
                fields.add( key.toString() );
            }
        }
        return fields;
    }

    private class ObjectVisitor {

        private StringBuilder builder;

        public ObjectVisitor() {
            builder = new StringBuilder();
        }

        public String visit( Object target ) {
            json( target, includeFields, excludeFields );
            return builder.toString();
        }

        public String visit( String rootName, Object target ) {
            add( '{' );
            string(rootName);
            add(':');
            json( target, includeFields, excludeFields );
            add( '}' );
            return builder.toString();
        }

        private void json(Object object, Map includes, Map excludes) {
            if (object == null) add("null");
            else if (object instanceof Class)
                string( ((Class)object).getName() );
            else if (object instanceof Boolean)
                bool( ((Boolean) object) );
            else if (object instanceof Number)
                add(object);
            else if (object instanceof String)
                string(object);
            else if (object instanceof Character)
                string(object);
            else if (object instanceof Map)
                map( (Map)object, includes, excludes);
            else if (object.getClass().isArray())
                array( object, includes, excludes );
            else if (object instanceof Iterable)
                array(((Iterable) object).iterator(), includes, excludes );
            else if( object instanceof Date )
                date( (Date)object );
            else if( object instanceof Enum )
                enumerate( (Enum)object );
            else
                bean(object, includes, excludes );
        }

        private void enumerate(Enum value) {
            string( value.name() );
        }

        private void map(Map map, Map includes, Map excludes) {
            add('{');
            Iterator it = map.keySet().iterator();
            while (it.hasNext()) {
                Object key = it.next();
                add( key, map.get(key), includes, excludes );
                if (it.hasNext()) add(',');
            }
            add('}');
        }

        private void array(Iterator it, Map includes, Map excludes) {
            add('[');
            while (it.hasNext()) {
                json( it.next(), includes, excludes );
                if (it.hasNext()) add(',');
            }
            add(']');
        }

        private void array(Object object, Map includes, Map excludes) {
            add('[');
            int length = Array.getLength(object);
            for (int i = 0; i < length; ++i) {
                json( Array.get(object, i), includes, excludes );
                if (i < length - 1) add(',');
            }
            add(']');
        }

        private void bool(Boolean b) {
            add( b ? "true" : "false" );
        }

        private void string(Object obj) {
            add('"');
            CharacterIterator it = new StringCharacterIterator(obj.toString());
            for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
                if (c == '"') add("\\\"");
                else if (c == '\\') add("\\\\");
                else if (c == '/') add("\\/");
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
            builder.append( "new Date( " );
            builder.append( date.getTime() );
            builder.append( ")" );
        }

        private void bean(Object object, Map includes, Map excludes) {
            add('{');
            try {
                BeanInfo info = Introspector.getBeanInfo(object.getClass());
                PropertyDescriptor[] props = info.getPropertyDescriptors();
                boolean firstField = true;
                for (PropertyDescriptor prop : props) {
                    String name = prop.getName();
                    Method accessor = prop.getReadMethod();
                    if (accessor != null && isIncluded( prop, includes, excludes ) ) {
                        Object value = accessor.invoke(object, (Object[]) null);
                        firstField = addComma(firstField);
                        add(name, value, includes, excludes);
                    }
                }
                Field[] ff = object.getClass().getDeclaredFields();
                for (Field field : ff) {
                    if (isValidField(field)) {
                        firstField = addComma(firstField);
                        add(field.getName(), field.get(object), includes, excludes);
                    }
                }
            } catch( Exception e ) {
                throw new JSONException( e );
            }
            add('}');
        }

        private boolean isIncluded(PropertyDescriptor prop, Map includes, Map excludes ) {
            if( includes.containsKey( prop.getName() ) ) {
                return true;
            }

            if( excludes.containsKey( prop.getName() ) ) {
                // This is sort of unique, and up for some interpretation to best behavior.
                // Right now it assumes if you specifiy a nested exclude that means you want to
                // include the parent object because if you don't then this exclude is meaningless.
                // EX: .exclude( "parent.name" ) means that the field named parent has to be included
                // in order for you to exclude the name field.
                return excludes.get( prop.getName() ) != null;
            }

            Method accessor = prop.getReadMethod();
            if( accessor.isAnnotationPresent( JSON.class ) ) {
                return accessor.getAnnotation(JSON.class).include();
            }

            Class propType = prop.getPropertyType();
            return !(propType.isArray() || Iterable.class.isAssignableFrom(propType) || Map.class.isAssignableFrom(propType));
        }

        private boolean isValidField(Field field) {
            return !Modifier.isStatic( field.getModifiers() ) && Modifier.isPublic( field.getModifiers() ) && !Modifier.isTransient( field.getModifiers() );
        }

        private boolean addComma(boolean firstField) {
            if ( !firstField ) {
                add(',');
            } else {
                firstField = false;
            }
            return firstField;
        }

        private void add( char c ) {
            builder.append( c );
        }

        private void add( Object value ) {
            builder.append( value );
        }

        private void add(Object key, Object value, Map includes, Map excludes) {
            builder.append("\"");
            builder.append( key );
            builder.append( "\"" );
            builder.append( ": " );

            Map nextIncludes = includes.containsKey( key ) && includes.get( key ) != null ? (Map)includes.get( key ) : Collections.EMPTY_MAP;
            Map nextExcludes = excludes.containsKey( key ) && excludes.get( key ) != null ? (Map)excludes.get( key ) : Collections.EMPTY_MAP;

            json( value, nextIncludes, nextExcludes );
        }

        private void unicode(char c) {
            add("\\u");
            int n = c;
            for (int i = 0; i < 4; ++i) {
                int digit = (n & 0xf000) >> 12;
                add(HEX[digit]);
                n <<= 4;
            }
        }
    }
}
