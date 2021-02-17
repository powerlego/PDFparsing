package backend;

import technology.tabula.Table;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nicholas Curl
 */
public class Employee {

    private String name;
    private Table top;
    private Table middle;
    private Table bottom;
    private List<Table> profile;

    public Employee(String name, Table top, Table middle, Table bottom) {
        this.name = name;
        this.top = top;
        this.middle = middle;
        this.bottom = bottom;
        this.profile = new LinkedList<>();
        this.profile.add(top);
        this.profile.add(middle);
        this.profile.add(bottom);
    }

    public String getName() {
        return name;
    }

    public Table getTop() {
        return this.top;
    }

    public Table getMiddle() {
        return middle;
    }

    public Table getBottom() {
        return bottom;
    }

    public List<Table> getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Employee) {
            Employee employee = (Employee) obj;
            return this.name.equals(employee.name);
        } else {
            return false;
        }
    }
}
