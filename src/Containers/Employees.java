package Containers;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Nicholas Curl
 */
public class Employees {

    private Map<String, Employee> employees = new TreeMap<>();

    public Employees() {

    }

    public boolean addEmployee(String employeeName, String employeeCode) {
        boolean added = false;
        if (!(employees.containsKey(employeeCode))) {
            employees.put(employeeCode, new Employee(employeeName, employeeCode));
            added = true;
        }
        return added;
    }

    public void addPAF(String employeeCode, PAF paf) {
        employees.get(employeeCode).addPAF(paf);
    }

    public Employee getEmployee(String employeeCode) {
        return employees.get(employeeCode);
    }

    public Path getEmployeeFileLocation(String employeeCode) {
        return employees.get(employeeCode).getEmployeeFileLocation();
    }

    public String getEmployeeName(String employeeCode) {
        return employees.get(employeeCode).getName();
    }

    public File getEmployeePhoto(String employeeCode) {
        return employees.get(employeeCode).getEmployeePhoto();
    }

    public Map<String, Employee> getEmployees() {
        return employees;
    }

    public List<PAF> getProfile(String employeeCode) {
        return employees.get(employeeCode).getProfile();
    }

    public void setEmployeeFileLocation(String employeeCode, Path employeeFileLocation) {
        employees.get(employeeCode).setEmployeeFileLocation(employeeFileLocation);
    }

    public void setEmployeePhoto(String employeeCode, File employeePhoto) {
        employees.get(employeeCode).setEmployeePhoto(employeePhoto);
    }

}
