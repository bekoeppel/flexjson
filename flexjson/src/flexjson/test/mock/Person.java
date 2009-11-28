/**
 * Copyright 2007 Charlie Hubbard
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
package flexjson.test.mock;

import flexjson.JSON;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class Person {

    private String firstname;
    private String lastname;
    private Date birthdate;
    private Address home;
    private Address work;
    private List phones = new ArrayList();
    private List hobbies = new ArrayList();

    public Person() {
    }

    public Person(String firstname, String lastname, Date birthdate, Address home, Address work) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.birthdate = birthdate;
        setHome( home );
        setWork( work );
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate = birthdate;
    }

    public Address getHome() {
        return home;
    }

    public void setHome(Address home) {
        this.home = home;
        if( home != null ) this.home.setPerson( this );
    }

    public Address getWork() {
        return work;
    }

    public void setWork(Address work) {
        this.work = work;
        if( work != null ) this.work.setPerson( this );
    }

    public List getPhones() {
        return phones;
    }

    public void setPhones(List phones) {
        this.phones = phones;
    }

    @JSON(include = false)
    public List getHobbies() {
        return hobbies;
    }

    public void setHobbies(List hobbies) {
        this.hobbies = hobbies;
    }
}
