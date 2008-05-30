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

import flexjson.BasicType;
import flexjson.TypeContext;

import java.util.Map;

public class MapTransformer extends AbstractTransformer {

    public void transform(Object object) {
        Map value = (Map) object;
        TypeContext typeContext = getContext().writeOpenObject();
        for (Object key : value.keySet()) {
            
            Transformer transformer = getContext().getTransformer(value.get(key));

            if (transformer instanceof Defer) {
                typeContext.setPropertyName(key.toString());
                transformer.transform(value.get(key));
            } else {
                if (!typeContext.isFirst()) getContext().writeComma();
                typeContext.setFirst(false);
                getContext().writeName(key.toString());
                transformer.transform(value.get(key));
            }


        }
        getContext().writeCloseObject();
    }

}
