package Containers;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Nicholas Curl
 */
public class Employee {

    private String name;
    private List<PAF> profile;
    private String code;

    public Employee(String name, String code) {
        this.name = name;
        this.profile = new LinkedList<>();
        this.code = code;
    }

    public void addPAF(PAF paf) {
        this.profile.add(paf);
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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public List<PAF> getProfile() {
        return profile;
    }
}
