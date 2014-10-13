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

import flexjson.JSONContext;
import flexjson.JSONException;
import flexjson.Path;
import flexjson.TypeContext;

import java.util.Map;

public class MapTransformer extends AbstractTransformer {

    public void transform(Object object) {
        JSONContext context = getContext();
        Path path = context.getPath();
        Map value = (Map) object;

        try {
            TypeContext typeContext = getContext().writeOpenObject();
            for (Object key : value.keySet()) {

                path.enqueue(key != null ? key.toString() : null);

                if (context.isIncluded(key != null ? key.toString() : null, value.get(key))) {

                    Transformer transformer = context.getTransformer(null, value.get(key));


                    if(!(transformer instanceof Inline) || !((Inline)transformer).isInline()) {
                        if (!typeContext.isFirst()) getContext().writeComma();
                        typeContext.increment();
                        if( key != null ) {
                            getContext().writeName(key.toString());
                        } else {
                            getContext().writeName(null);
                        }
                    }

                    if( key != null ) {
                        typeContext.setPropertyName(key.toString());
                    } else {
                        typeContext.setPropertyName(null);
                    }

                    transformer.transform(value.get(key));

                }

                path.pop();

            }
            getContext().writeCloseObject();
        } catch( Exception ex ) {
            throw new JSONException(String.format("%s: Error while trying to serialize.", path), ex);
        }
    }

}
