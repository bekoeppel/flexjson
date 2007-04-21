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

public class JSONSerializer {

    public final static char[] HEX = "0123456789ABCDEF".toCharArray();

    Map excludeFields = new HashMap();
    Map includeFields = new HashMap();

    public JSONSerializer() {
    }

    public String serialize( String rootName, Object target ) {
        return new ObjectVisitor().visit( rootName, target );
    }

    public String serialize( Object target ) {
        return new ObjectVisitor().visit( target );
    }

    public JSONSerializer exclude( String... fields ) {
        addFieldsTo(excludeFields, fields);
        return this;
    }

    public JSONSerializer include( String... fields ) {
        addFieldsTo(includeFields, fields );
        return this;
    }

    public List getIncludes() {
        return renderFields( includeFields );
    }

    public List getExcludes() {
        return renderFields( excludeFields );
    }

    public void setIncludes( List fields ) {
        for( Object field : fields ) {
            addFieldsTo( includeFields, field.toString() );
        }
    }

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
