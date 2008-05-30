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

/**
 * This is a simple interface that can be applied to
 * a transofrmer. It is used in the {@link ObjectTransformer}
 * to determine if the name write should be deferred to the
 * type transformer that will handle its value. This provides
 * the flexibility to flatten out an object heirarcy.
 */
public interface Defer {

    /**
     * receives deferred values from the calling transformer
     * @param values
     */
    public void setValues(Object... values);

    /**
     * resets the deferred values to default when completed
     */
    public void reset();



}
