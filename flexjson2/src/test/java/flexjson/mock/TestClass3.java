package flexjson.mock;

//import org.apache.commons.lang.builder.ToStringBuilder;

public class TestClass3 {

	private boolean found = false;

	public void setFound(boolean found) {
		this.found = found;
	}

	public boolean isFound() {
		return found;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (found ? 1231 : 1237);
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
		TestClass3 other = (TestClass3) obj;
		if (found != other.found)
			return false;
		return true;
	}

//	@Override
//	public String toString() {
//		return ToStringBuilder.reflectionToString(this);
//	}


}
