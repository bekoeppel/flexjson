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
        Path path = getContext().getPath();
        ChainedSet visits = getContext().getVisits();
        try {
            if (!visits.contains(object)) {
                getContext().setVisits(new ChainedSet(visits));
                getContext().getVisits().add(object);
                // traverse object
                BeanInfo info = Introspector.getBeanInfo(object.getClass());
                PropertyDescriptor[] props = info.getPropertyDescriptors();
                TypeContext typeContext = new TypeContext(BasicType.OBJECT);
                getContext().pushTypeContext(typeContext);
                getContext().writeOpenObject();
                for (PropertyDescriptor prop : props) {
                    String name = prop.getName();
                    path.enqueue(name);
                    Method accessor = prop.getReadMethod();
                    if (accessor != null && getContext().isIncluded(prop)) {
                        Object value = accessor.invoke(object, (Object[]) null);
                        if (!getContext().getVisits().contains(value)) {
                            if (!typeContext.isFirst()) getContext().writeComma();

                            Transformer transformer = getContext().getTransformer(value);

                            if (transformer instanceof Defer) {
                                Defer d = (Defer) transformer;
                                d.setValues(name);
                                transformer.transform(value);
                            } else {
                                getContext().writeName(name);
                                transformer.transform(value);
                            }

                            typeContext.setFirst(false);
                        }

                    }
                    path.pop();
                }
                for (Class current = object.getClass(); current != null; current = current.getSuperclass()) {
                    Field[] ff = current.getDeclaredFields();
                    for (Field field : ff) {
                        path.enqueue(field.getName());
                        if (getContext().isValidField(field)) {
                            if (!getContext().getVisits().contains(field.get(object))) {
                                if (!typeContext.isFirst()) getContext().writeComma();

                                Object value = field.get(object);
                                Transformer transformer = getContext().getTransformer(value);

                                if (transformer instanceof Defer) {
                                    Defer d = (Defer) transformer;
                                    d.setValues(field.getName());
                                    transformer.transform(value);
                                } else {
                                    getContext().writeName(field.getName());
                                    transformer.transform(value);
                                }

                                typeContext.setFirst(false);
                            }
                        }
                        path.pop();
                    }
                }

                getContext().writeCloseObject();
                getContext().popTypeContext();
                getContext().setVisits((ChainedSet) getContext().getVisits().getParent());

            }
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Error trying to deepSerialize", e);
        }
    }

}
