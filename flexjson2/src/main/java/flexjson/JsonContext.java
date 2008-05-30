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

import flexjson.transformer.*;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.*;

public class JsonContext {

    private static ThreadLocal<JsonContext> context = new ThreadLocal<JsonContext>() {
        protected JsonContext initialValue() {
            return new JsonContext();
        }
    };

    private OutputHandler out;
    private boolean prettyPrint = false;
    private Stack<TypeContext> typeContextStack = new Stack<TypeContext>();

    private int indent = 0;
    private TypeTransformerMap typeTransformerMap = new TypeTransformerMap();
    private Map<Path, Transformer> pathTransformerMap = new HashMap<Path, Transformer>();

    private List<PathExpression> pathExpressions = new ArrayList<PathExpression>();

    private SerializationType serializationType = SerializationType.SHALLOW;

    private ChainedSet visits = new ChainedSet(Collections.EMPTY_SET);

    private Path path = new Path();


    public JsonContext() {
        // define all standard type transformers
        Transformer transformer = new NullTransformer();
        typeTransformerMap.put(null, transformer);

        transformer = new ObjectTransformer();
        typeTransformerMap.put(Object.class, transformer);

        transformer = new ClassTransformer();
        typeTransformerMap.put(Class.class, transformer);

        transformer = new BooleanTransformer();
        typeTransformerMap.put(boolean.class, transformer);
        typeTransformerMap.put(Boolean.class, transformer);

        transformer = new NumberTransformer();
        typeTransformerMap.put(Number.class, transformer);

        typeTransformerMap.put(Integer.class, transformer);
        typeTransformerMap.put(int.class, transformer);

        typeTransformerMap.put(Long.class, transformer);
        typeTransformerMap.put(long.class, transformer);

        typeTransformerMap.put(Double.class, transformer);
        typeTransformerMap.put(double.class, transformer);

        typeTransformerMap.put(Float.class, transformer);
        typeTransformerMap.put(float.class, transformer);

        typeTransformerMap.put(BigDecimal.class, transformer);
        typeTransformerMap.put(BigInteger.class, transformer);

        transformer = new StringTransformer();
        typeTransformerMap.put(String.class, transformer);

        transformer = new CharacterTransformer();
        typeTransformerMap.put(Character.class, transformer);
        typeTransformerMap.put(char.class, transformer);

        transformer = new BasicDateTransformer();
        typeTransformerMap.put(Date.class, transformer);

        transformer = new EnumTransformer();
        typeTransformerMap.put(Enum.class, transformer);

        transformer = new IterableTransformer();
        typeTransformerMap.put(Iterable.class, transformer);

        transformer = new MapTransformer();
        typeTransformerMap.put(Map.class, transformer);

        transformer = new NullTransformer();
        typeTransformerMap.put(void.class, transformer);

        transformer = new ArrayTransformer();
        typeTransformerMap.put(Arrays.class, transformer);

    }

    // CONFIGURE SERIALIZATION
    public void serializationType(SerializationType serializationType) {
        this.serializationType = serializationType;
    }

    // CONFIGURE TRANSFORMERS

    /**
     * Run a transformer on the provided object
     *
     * @param object
     * @return
     */
    public void transform(Object object) {

        Transformer transformer = getPathTransformer(object);

        if (transformer == null) {
            transformer = getTypeTransformer(object);
        }

        transformer.transform(object);

    }

    /**
     * Retrieves a transformer for the provided object
     *
     * @param object
     * @return
     */
    public Transformer getTransformer(Object object) {

        Transformer transformer = getPathTransformer(object);

        if (transformer == null) {
            transformer = getTypeTransformer(object);
        }

        return transformer;

    }

    private Transformer getPathTransformer(Object object) {
        if (null == object) return getTypeTransformer(object);
        return pathTransformerMap.get(path);
    }

    private Transformer getTypeTransformer(Object object) {
        return typeTransformerMap.get(object);
    }

    /**
     * used to pass in configured transformers from the JsonSerializer
     *
     * @param typeTransformerMap
     */
    public void addTypeTransformers(TypeTransformerMap typeTransformerMap) {
        this.typeTransformerMap.putAll(typeTransformerMap);
    }

    /**
     * used to pass in configured transformers from the JsonSerializer
     *
     * @param pathTransformerMap
     */
    public void addPathTransformers(Map<Path, Transformer> pathTransformerMap) {
        this.pathTransformerMap.putAll(pathTransformerMap);
    }

    // OUTPUT

    /**
     * configures the context to output JSON with new lines and indentations
     *
     * @param prettyPrint
     */
    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }

    public void pushTypeContext(TypeContext contextEnum) {
        typeContextStack.push(contextEnum);
    }

    public void popTypeContext() {
        typeContextStack.pop();
    }

    public TypeContext peekTypeContext() {
        if(typeContextStack.size() > 0) {
            return typeContextStack.peek();
        } else {
            return null;
        }
    }

    /**
     * Set the output handler.
     *
     * @param out
     */
    public void setOut(OutputHandler out) {
        this.out = out;
    }

    /**
     * write a simple non-quoted value to output
     *
     * @param value
     */
    public void write(String value) {
        out.write(value);
    }

    public void writeOpenObject() {
        write("{");
        if (prettyPrint) {
            write("\n");
            indent += 4;
        }
    }

    public void writeCloseObject() {
        if (prettyPrint) {
            write("\n");
            indent -= 4;
            if (prettyPrint) writeIndent();
        }
        write("}");
    }

    public void writeName(String name) {
        if (prettyPrint) writeIndent();
        writeQuoted(name);
        write(":");
        if (prettyPrint) write(" ");
    }

    public void writeComma() {
        write(",");
        if (prettyPrint && peekTypeContext().getBasicType() != BasicType.ARRAY) {
            write("\n");
        }
    }

    public void writeOpenArray() {
        write("[");
    }

    public void writeCloseArray() {
        write("]");
    }

    public void writeIndent() {
        for (int i = 0; i < indent; i++) {
            write(" ");
        }
    }

    /**
     * write a quoted and escaped value to the output
     *
     * @param value
     */
    public void writeQuoted(String value) {
        out.write("\"");
        CharacterIterator it = new StringCharacterIterator(value);
        for (char c = it.first(); c != CharacterIterator.DONE; c = it.next()) {
            if (c == '"') out.write("\\\"");
            else if (c == '\\') out.write("\\\\");
            else if (c == '\b') out.write("\\b");
            else if (c == '\f') out.write("\\f");
            else if (c == '\n') out.write("\\n");
            else if (c == '\r') out.write("\\r");
            else if (c == '\t') out.write("\\t");
            else if (Character.isISOControl(c)) {
                unicode(c);
            } else {
                out.write(String.valueOf(c));
            }
        }
        out.write("\"");

    }

    private void unicode(char c) {
        out.write("\\u");
        int n = c;
        for (int i = 0; i < 4; ++i) {
            int digit = (n & 0xf000) >> 12;
            out.write(String.valueOf(JsonSerializer.HEX[digit]));
            n <<= 4;
        }
    }

    // MANAGE CONTEXT

    /**
     * static method to get the context for this thread
     *
     * @return
     */
    public static JsonContext get() {
        return context.get();
    }

    /**
     * static moethod to clean up thread when serialization is complete
     */
    public static void cleanup() {
        context.remove();
    }

    // INCLUDE/EXCLUDE METHODS

    public ChainedSet getVisits() {
        return visits;
    }

    public void setVisits(ChainedSet visits) {
        this.visits = visits;
    }

    public Path getPath() {
        return this.path;
    }

    public void addPathExpressions(List<PathExpression> pathExpressions) {
        this.pathExpressions.addAll(pathExpressions);
    }

    public boolean isIncluded(PropertyDescriptor prop) {
        if (serializationType == SerializationType.SHALLOW) {

            PathExpression expression = matches(prop, pathExpressions);
            if (expression != null) {
                return expression.isIncluded();
            }

            Method accessor = prop.getReadMethod();
            if (accessor.isAnnotationPresent(JSON.class)) {
                return accessor.getAnnotation(JSON.class).include();
            }

            Class propType = prop.getPropertyType();
            return !(propType.isArray() || Iterable.class.isAssignableFrom(propType) || Map.class.isAssignableFrom(propType));

        } else {

            PathExpression expression = matches(prop, pathExpressions);
            if (expression != null) {
                return expression.isIncluded();
            }

            Method accessor = prop.getReadMethod();
            if (accessor.isAnnotationPresent(JSON.class)) {
                return accessor.getAnnotation(JSON.class).include();
            }

            return true;
        }

    }

    public boolean isValidField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers());
    }

    protected PathExpression matches(PropertyDescriptor prop, List<PathExpression> expressions) {
        for (PathExpression expr : expressions) {
            if (expr.matches(path)) {
                return expr;
            }
        }
        return null;
    }

}
