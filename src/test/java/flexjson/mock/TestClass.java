package flexjson.mock;

import java.util.ArrayList;
import java.util.List;

//import org.apache.commons.lang.builder.ToStringBuilder;

//import com.ri3k.uiNegotiationPrototype.service.ProcessDataService;
//import com.ri3k.uiNegotiationPrototype.service.ProcessDataServiceImpl;

import flexjson.JSON;
import org.junit.Ignore;

@Ignore
public class TestClass{
	private String name="testName";
//	private ProcessDataService service = new ProcessDataServiceImpl();
	private List<TestClass2> testList = new ArrayList<TestClass2>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

//	public ProcessDataService getService() {
//		return service;
//	}
//	public void setService(ProcessDataService service) {
//		this.service = service;
//	}

	@JSON(name = "test_list")
	public List<TestClass2> getTestList() {
		return testList;
	}
	public void setTestList(List<TestClass2> testList) {
		this.testList = testList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((testList == null) ? 0 : testList.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestClass other = (TestClass) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (testList == null) {
			if (other.testList != null)
				return false;
		} else if (!testList.equals(other.testList))
			return false;
		return true;
	}

//	@Override
//	public String toString() {
//		return ToStringBuilder.reflectionToString(this);
//	}

}