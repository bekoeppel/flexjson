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
package flexjson.transformer;

import flexjson.*;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectTransformer extends AbstractTransformer {

    public void transform(Object object) {
        JsonContext context = getContext();
        Path path = context.getPath();
        ChainedSet visits = context.getVisits();
        try {
            if (!visits.contains(object)) {
                context.setVisits(new ChainedSet(visits));
                context.getVisits().add(object);
                // traverse object
                BeanInfo info = Introspector.getBeanInfo( resolveClass(object) );
                PropertyDescriptor[] props = info.getPropertyDescriptors();
                TypeContext typeContext = context.writeOpenObject();
                for (PropertyDescriptor prop : props) {
                    String name = prop.getName();
                    path.enqueue(name);
                    Method accessor = prop.getReadMethod();
                    if (accessor != null && context.isIncluded(prop)) {
                        Object value = accessor.invoke(object, (Object[]) null);
                        if (!context.getVisits().contains(value)) {

                            Transformer transformer = context.getTransformer(value);

                            if (transformer instanceof Defer) {
                                typeContext.setPropertyName(name);
                                transformer.transform(value);
                            } else {
                                if (!typeContext.isFirst()) context.writeComma();
                                typeContext.setFirst(false);
                                context.writeName(name);
                                transformer.transform(value);
                            }

                        }

                    }
                    path.pop();
                }
                for (Class current = object.getClass(); current != null; current = current.getSuperclass()) {
                    Field[] ff = current.getDeclaredFields();
                    for (Field field : ff) {
                        path.enqueue(field.getName());
                        if (context.isValidField(field) && context.isIncluded( field ) ) {
                            if (!context.getVisits().contains(field.get(object))) {

                                Object value = field.get(object);
                                Transformer transformer = context.getTransformer(value);

                                if (transformer instanceof Defer) {
                                    typeContext.setPropertyName(field.getName());
                                    transformer.transform(value);
                                } else {
                                    if (!typeContext.isFirst()) context.writeComma();
                                    typeContext.setFirst(false);
                                    context.writeName(field.getName());
                                    transformer.transform(value);
                                }

                            }
                        }
                        path.pop();
                    }
                }

                context.writeCloseObject();
                context.setVisits((ChainedSet) context.getVisits().getParent());

            }
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Error trying to deepSerialize", e);
        }
    }

    protected Class resolveClass( Object obj ) {
        return obj.getClass();
    }

}
