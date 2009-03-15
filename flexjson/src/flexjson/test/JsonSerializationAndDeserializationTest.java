package flexjson.test;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

public class JsonSerializationAndDeserializationTest {

	String expectedSerializedObjectString = "{\"name\":\"testName\",\"testList\":[{\"mapOfJustice\":{\"String1\":{\"found\":false}},\"name\":\"testName2\"}]}";
	
	@Test
	public void testCanSerializeAnObjectIntoSomethingSensible() throws Exception {
		TestClass testObject = createTestObject();
		String serializedString = new JSONSerializer().exclude("*.class").serialize(testObject);
		assertEquals(expectedSerializedObjectString, serializedString);
	}
	
	@Test
	public void testCanDeserializeAnObjectIntoSomethingSensible() throws Exception {
		TestClass expectedTestClass = createTestObject();
		
		expectedTestClass.getTestList().get(0).getMapOfJustice().values().iterator().next().setFound(true);
		String nobber = new JSONSerializer().exclude("*.class").serialize(expectedTestClass);
        JSONDeserializer<TestClass> deserializer = new JSONDeserializer<TestClass>().use(null, TestClass.class);
        TestClass deserializedTestClass = deserializer.deserialize(nobber);

		assertEquals(expectedTestClass, deserializedTestClass);
	}

	private TestClass createTestObject() {
		TestClass testObject = new TestClass();
		testObject.setTestList(createSingleTestClass2List());
		return testObject;
	}

	private ArrayList<TestClass2> createSingleTestClass2List() {
		ArrayList<TestClass2> list = new ArrayList<TestClass2>();
		TestClass2 listElement = new TestClass2();
		HashMap<String, TestClass3> map = new HashMap<String, TestClass3>();
		map.put("String1", new TestClass3());
		listElement.setMapOfJustice(map);
		list.add(listElement);
		return list;
	}

}