package flexjson.test.mock;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: charlie
 * Date: Jun 24, 2007
 * Time: 5:54:54 PM
 */
public class Employee extends Person {

    String company;

    public Employee() {
    }

    public Employee(String firstname, String lastname, Date birthdate, Address home, Address work, String company) {
        super(firstname, lastname, birthdate, home, work);
        this.company = company;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }
}
