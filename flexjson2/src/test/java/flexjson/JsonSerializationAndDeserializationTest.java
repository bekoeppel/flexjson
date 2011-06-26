package flexjson;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import flexjson.mock.TestClass;
import flexjson.mock.TestClass2;
import flexjson.mock.TestClass3;
import junit.framework.TestCase;

public class JsonSerializationAndDeserializationTest extends TestCase {

	String expectedSerializedObjectString = "{\"name\":\"testName\",\"testList\":[{\"mapOfJustice\":{\"String1\":{\"category\":null,\"found\":false,\"name\":null}}}]}";
	
	public void testCanSerializeAnObjectIntoSomethingSensible() throws Exception {
		TestClass testObject = createTestObject();
		String serializedString = new JSONSerializer().include("testList.mapOfJustice").exclude("*.class").serialize(testObject);
		assertEquals(expectedSerializedObjectString, serializedString);
	}
	
	public void testCanDeserializeAnObjectIntoSomethingSensible() throws Exception {
		TestClass expectedTestClass = createTestObject();
		
		expectedTestClass.getTestList().get(0).getMapOfJustice().values().iterator().next().setFound(true);
		String nobber = new JSONSerializer().include("testList.mapOfJustice").exclude("*.class").serialize(expectedTestClass);
        JSONDeserializer<TestClass> deserializer = new JSONDeserializer<TestClass>().use(null, TestClass.class);
        TestClass deserializedTestClass = deserializer.deserialize(nobber);

		assertEquals(expectedTestClass, deserializedTestClass);
	}

    public void testUseandRootDeserialization() {
        String json = "{\"foo\":\"bar\", \"class\":\"java.lang.Integer\"}";
        Map<String,String> useMap = new JSONDeserializer<Map<String,String>>().use(null, HashMap.class).deserialize( json );
        Map<String,String> rootMap = new JSONDeserializer<Map<String,String>>().deserialize( json, HashMap.class );

        assertEquals( rootMap.size(), useMap.size() );
        assertEquals( "bar", useMap.get("foo") );
        assertEquals( "bar", rootMap.get("foo") );
        assertEquals( "java.lang.Integer", useMap.get("class") );
        assertEquals( "java.lang.Integer", rootMap.get("class") );
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