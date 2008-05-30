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

import flexjson.ChainedSet;
import flexjson.JsonException;
import flexjson.Path;
import flexjson.PrettyPrintContext;

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
                boolean isFirst = true;
                getContext().pushPrettyPrintContext(PrettyPrintContext.OBJECT);
                getContext().writeOpenObject();
                for (PropertyDescriptor prop : props) {
                    String name = prop.getName();
                    path.enqueue(name);
                    Method accessor = prop.getReadMethod();
                    if (accessor != null && getContext().isIncluded(prop)) {
                        Object value = accessor.invoke(object, (Object[]) null);
                        if (!getContext().getVisits().contains(value)) {
                            if (!isFirst) getContext().writeComma();
                            getContext().writeName(name);
                            getContext().transform(value);
                            isFirst = false;
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
                                if (!isFirst) getContext().writeComma();
                                getContext().writeName(field.getName());
                                Object value = field.get(object);
                                getContext().transform(value);
                                isFirst = false;
                            }
                        }
                        path.pop();
                    }
                }

                getContext().writeCloseObject();
                getContext().popPrettyPrintContext();
                getContext().setVisits((ChainedSet) getContext().getVisits().getParent());

            }
        } catch (JsonException e) {
            throw e;
        } catch (Exception e) {
            throw new JsonException("Error trying to deepSerialize", e);
        }
    }

}
