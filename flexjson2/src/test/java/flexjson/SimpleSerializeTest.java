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

import flexjson.model.*;
import flexjson.transformer.FlatDateTransformer;
import flexjson.transformer.StateTransformer;
import flexjson.transformer.StringArrayTransformer;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;

public class SimpleSerializeTest extends TestCase {

    final Logger logger = LoggerFactory.getLogger(SimpleSerializeTest.class);

    public void testDeepSerializePerson() {
        Person person = buildPerson1();

        JsonSerializer serializer = new JsonSerializer();
        serializer.deepSerialize(person);
        String string = serializer.getJson();
        logger.info(string);
        assertTrue(string.startsWith("{"));
        assertTrue(string.endsWith("}"));

    }

    public void testSerializePerson() {
        Person person = buildPerson1();

        JsonSerializer serializer = new JsonSerializer();
        serializer.serialize(person);
        String string = serializer.getJson();
        logger.info(string);
        assertTrue(string.startsWith("{\""));
        assertTrue(string.endsWith("}"));

    }

    public void testDeepSerializePersonWithRootName() {
        Person person = buildPerson1();

        JsonSerializer serializer = new JsonSerializer()
                .transform(new StateTransformer(), State.class)
                .transform(new StringArrayTransformer(), String[].class)
                .rootName("myRootName");

        serializer.deepSerialize(person);
        String string = serializer.getJson();
        logger.info(string);
        assertTrue(string.contains("{\"myRootName\":"));
        assertTrue(string.contains("\"state\":\"Nevada\","));

    }

    public void testDeepSerializePersonWithWriter() {
        Person person = buildPerson1();

        StringWriter writer = new StringWriter();


        JsonSerializer serializer =
                new JsonSerializer()
                        .transform(new StateTransformer(), State.class)
                        .transform(new StringArrayTransformer(), String[].class)
                        .rootName("myRootName")
                        .out(writer);

        serializer.deepSerialize(person);
        String string = serializer.getJson();
        logger.info(string);
        assertTrue(string.contains("{\"myRootName\":"));
        assertTrue(string.contains("\"state\":\"Nevada\","));

    }

    public void testDeepSerializePersonWithWriterPretty() {
        Person person = buildPerson1();

        StringWriter writer = new StringWriter();


        JsonSerializer serializer =
                new JsonSerializer()
                        .transform(new StateTransformer(), State.class)
                        .transform(new StringArrayTransformer(), String[].class)
                        .rootName("myRootName")
                        .prettyPrint(true)
                        .out(writer);

        serializer.deepSerialize(person);
        String string = serializer.getJson();
        logger.info(string);

    }

    public void testSerializeAddressList() {
        Person person = buildPerson1();

        JsonSerializer serializer =
                new JsonSerializer().prettyPrint(true);
        serializer.serialize(person.getAddresses());

        String string = serializer.getJson();
        logger.info(string);

    }

    public void testSerializeAccountsMap() {
        Person person = buildPerson1();

        JsonSerializer serializer =
                new JsonSerializer().prettyPrint(true);
        serializer.serialize(person.getAccounts());

        String string = serializer.getJson();
        logger.info(string);
    }

    public void testDeferOnDate() {
        Date birthDate = buildPerson1().getBirthDate();
        JsonSerializer serializer = new JsonSerializer();
        serializer.transform(new FlatDateTransformer(), Date.class);
        String json = serializer.serialize(birthDate);
        logger.info(json);
        assertEquals(json, "{\"month\":11,\"day\":13,\"year\":2007}");
    }

    public void testDeferOnPersonWithFlatDate() {
        Person person = buildPerson1();
        JsonSerializer serializer = new JsonSerializer();
        serializer.transform(new FlatDateTransformer(), Date.class);
        String json = serializer.serialize(person);
        logger.info(json);
        assertTrue(json.contains("\"birthDateMonth\":11,\"birthDateDay\":13,\"birthDateYear\":2007"));
    }

    public Person buildPerson1() {

        Calendar c = Calendar.getInstance();
        c.set(2007, 11, 13);

        // basic person
        Person person = new Person();
        person.setId(1);
        person.setFirstName("Joe");
        person.setLastName("Blow");
        person.setBirthDate(c.getTime());

        person.setFavoriteFoods(new String[]{"Ice Cream", "Burritos"});
        person.setLuckyNumbers(new Integer[]{13, 23, 73});

        person.setPastLottoPicks(new Integer[][]{{12, 13, 14, 15, 16, 17}, {18, 19, 20, 21, 22, 23}, {24, 25, 26, 27, 28, 29, 30}});

        // add addresses
        State mt = new State();
        mt.setId(1);
        mt.setAbbrev("MT");
        mt.setName("Montana");

        State nv = new State();
        mt.setId(2);
        mt.setAbbrev("NV");
        mt.setName("Nevada");

        Address address1 = new Address();
        address1.setId(1);
        address1.setStreet1("123 Joke Way");
        address1.setStreet2("Apt 25");
        address1.setCity("Laughville");
        address1.setState(mt);
        address1.setPostal("01234");

        Address address2 = new Address();
        address2.setId(2);
        address2.setStreet1("456 Serious Way");
        address2.setStreet2("Apt 50");
        address2.setCity("Deadpanville");
        address2.setState(nv);
        address2.setPostal("56789");

        List<Address> addresses = new ArrayList<Address>();
        addresses.add(address1);
        addresses.add(address2);

        person.setAddresses(addresses);

        // add accounts
        Map<String, Account> accounts = new HashMap<String, Account>();

        Account account1 = new Account();
        account1.setId(1);
        account1.setName("Joe Checking");
        account1.setAccountType(AccountType.Checking);
        account1.setAccountNumber("00001234567");
        account1.setBalance(new BigDecimal("123.40"));
        accounts.put(account1.getAccountNumber(), account1);

        Account account2 = new Account();
        account2.setId(2);
        account2.setName("Joe Savings");
        account2.setAccountType(AccountType.Savings);
        account2.setAccountNumber("00007654321");
        account2.setBalance(new BigDecimal("800.20"));
        accounts.put(account2.getAccountNumber(), account2);

        person.setAccounts(accounts);

        return person;
    }

}
