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

import java.util.Arrays;
import java.util.HashMap;

/**
 * This class is used to lookup type transformers from specific to generic implementation.
 * For example if an ArrayList transformer is provided
 */
public class TypeTransformerMap extends HashMap<Class, Transformer> {

    @SuppressWarnings("unchecked")
    public Transformer get(Object key) {
        return findTransformer(key == null ? void.class : key.getClass());
    }

    private Transformer findTransformer(Class key) {

        // if specific type found
        if (super.containsKey(key)) {
            return super.get(key);
        }

        // handle arrays specially if no specific array type handler
        // Arrays.class is used for this because it would never appear
        // in an object that needs to be serialized.
        if (key.isArray()) {
            return super.get(Arrays.class);
        }

        // check for interface transformer
        for (Class interfaze : key.getInterfaces()) {

            if (super.containsKey(interfaze)) {
                return super.get(interfaze);
            } else {
                for (Class superInterface : interfaze.getInterfaces()) {
                    if (super.containsKey(superInterface))
                        return super.get(superInterface);
                }
            }

        }

        // if no interface transformers then check superclass
        return findTransformer(key.getSuperclass());

    }
}
