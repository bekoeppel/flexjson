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

public class TypeContext {

    private BasicType basicType;
    private int count;
    private String propertyName;

    public TypeContext(BasicType basicType) {
        this.basicType = basicType;
        count = 0;
    }

    public BasicType getBasicType() {
        return basicType;
    }

    public void setBasicType(BasicType basicType) {
        this.basicType = basicType;
    }

    public boolean isFirst() {
        return count == 0;
    }

    public void decrement() {
        count = Math.max(count-1, 0);
    }
    
    public void increment() {
        count++;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
