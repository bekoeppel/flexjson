package flexjson;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import flexjson.factories.BooleanAsStringObjectFactory;
import flexjson.mock.*;
import flexjson.transformer.BooleanAsStringTransformer;
import junit.framework.TestCase;

import org.junit.Test;

public class JsonSerializationAndDeserializationTest extends TestCase {

	String expectedSerializedObjectString = "{\"name\":\"testName\",\"test_list\":[{\"mapOfJustice\":{\"String1\":{\"category\":null,\"found\":false,\"name\":null}}}]}";
	
    String expectedSerializedObjectVisitedInCollectionString = "{\"name\":\"SubTask 2\",\"task\":{\"name\":\"Sample task with subTasks\",\"subTasks\":[{\"name\":\"SubTask 1\"}]}}";
	
    String expectedSerializedObjectFirstVisitedInCollectionString = "{\"name\":\"SubTask 1\",\"task\":{\"name\":\"Sample task with subTasks\",\"subTasks\":[{\"name\":\"SubTask 2\"}]}}";

	public void testJsonNameAndIncludes() throws Exception {
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
    
    @Test
    public void testObjectVisitedInCollection() {

    	Task task = new Task();
    	task.setName("Sample task with subTasks");
    	
    	SubTask subTask1 = new SubTask();
    	subTask1.setName("SubTask 1");
    	subTask1.setTask(task);
    	
    	SubTask subTask2 = new SubTask();
    	subTask2.setName("SubTask 2");
    	subTask2.setTask(task);
    	
    	task.setSubTasks(Arrays.asList(subTask1, subTask2));
    	
        String json = new JSONSerializer().exclude("*.class").deepSerialize( subTask2 );

        assertEquals(expectedSerializedObjectVisitedInCollectionString, json);
    	
	}
    
    @Test
    public void testObjectFirstVisitedInCollection() {

    	Task task = new Task();
    	task.setName("Sample task with subTasks");
    	
    	SubTask subTask1 = new SubTask();
    	subTask1.setName("SubTask 1");
    	subTask1.setTask(task);
    	
    	SubTask subTask2 = new SubTask();
    	subTask2.setName("SubTask 2");
    	subTask2.setTask(task);
    	
    	task.setSubTasks(Arrays.asList(subTask1, subTask2));
    	
        String json = new JSONSerializer().exclude("*.class").deepSerialize( subTask1 );
        
        assertEquals(expectedSerializedObjectFirstVisitedInCollectionString, json);
    	
	}

    @Test
    public void testAlternateBooleanTransformer() {
        String expectedYes = "{\"class\":\"flexjson.mock.Spiderman\",\"spideySense\":\"yes\",\"superpower\":\"Creates web\"}";
        String expectedNo = "{\"class\":\"flexjson.mock.Spiderman\",\"spideySense\":\"no\",\"superpower\":\"Creates web\"}";

        Spiderman spiderman = new Spiderman();
        spiderman.spideySense = true;
        String json = new JSONSerializer().transform( new BooleanAsStringTransformer("yes", "no"), Boolean.class ).serialize(spiderman);
        assertEquals( expectedYes, json );
        spiderman.spideySense = false;
        json = new JSONSerializer().transform( new BooleanAsStringTransformer("yes", "no"), Boolean.class ).serialize(spiderman);
        assertEquals( expectedNo, json );

        spiderman = new JSONDeserializer<Spiderman>().use(Boolean.class, new BooleanAsStringObjectFactory("yes", "no")).deserialize(expectedYes, Spiderman.class);
        assertTrue( "Assert it deserialized yes into true", spiderman.spideySense );
        spiderman = new JSONDeserializer<Spiderman>().use(Boolean.class, new BooleanAsStringObjectFactory("yes", "no")).deserialize( expectedNo, Spiderman.class );
        assertFalse("Assert it deserialized no into false", spiderman.spideySense);
    }

    @Test
    public void testSimpleMapDeserialization() {
        MapNoTyping demo = new MapNoTyping();
        demo.getData().put("key1", "value1");
        demo.getData().put("key2", "value2");
        demo.getData().put("key3", new TestClass3("charlie", "tatters", false) );

        JSONSerializer jsonSerializer = new JSONSerializer();

        String json = jsonSerializer.deepSerialize( demo );
        JSONDeserializer<MapNoTyping> jsonDeserializer = new JSONDeserializer<MapNoTyping>();
        MapNoTyping result = jsonDeserializer.deserialize( json );

        assertTrue( result.getData().containsKey("key1") );
        assertTrue(result.getData().containsKey("key2"));
        assertTrue(result.getData().containsKey("key3"));

        assertEquals( "value1", result.getData().get("key1") );
        assertEquals("value2", result.getData().get("key2"));
        assertEquals( demo.getData().get("key3"), result.getData().get("key3") );
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
