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

import flexjson.PrettyPrintContext;

import java.util.Map;

public class MapTransformer extends AbstractTransformer {

    public void transform(Object object) {
        Map value = (Map) object;
        boolean isFirst = true;
        getContext().pushPrettyPrintContext(PrettyPrintContext.OBJECT);
        getContext().writeOpenObject();
        for (Object key : value.keySet()) {
            if (!isFirst) getContext().writeComma();
            getContext().writeName(key.toString());
            getContext().transform(value.get(key));
            isFirst = false;
        }
        getContext().writeCloseObject();
        getContext().popPrettyPrintContext();
    }

}
