package Containers;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Container for all employees parsed
 *
 * @author Nicholas Curl
 */
public class Employees {

    /**
     * Map containing all of the employees
     */
    private final Map<String, Employee> employees = new TreeMap<>();

    /**
     * Constructor for the Employees Container
     */
    public Employees() {

    }

    /**
     * Add the employee to the map of employees, if they are not already added
     *
     * @param employeeName The employee name
     * @param employeeCode The employee code
     *
     * @return True if the employee was added to the map, false if it already exists
     */
    public boolean addEmployee(String employeeName, String employeeCode) {
        boolean added = false;
        if (!(employees.containsKey(employeeCode))) {
            employees.put(employeeCode, new Employee(employeeName, employeeCode));
            added = true;
        }
        return added;
    }

    /**
     * Adds a PAF to a specific employee
     *
     * @param employeeCode The employee code to query
     * @param paf          The PAF to add
     */
    public void addPAF(String employeeCode, PAF paf) {
        employees.get(employeeCode).addPAF(paf);
    }

    /**
     * Get the employee associated with the employee code
     *
     * @param employeeCode The employee code to query
     *
     * @return The employee queried
     */
    public Employee getEmployee(String employeeCode) {
        return employees.get(employeeCode);
    }

    /**
     * Get the employee file location for a specific employee
     *
     * @param employeeCode The employee code to query
     *
     * @return The employee file location associated with the employee code
     */
    public Path getEmployeeFileLocation(String employeeCode) {
        return employees.get(employeeCode).getEmployeeFileLocation();
    }

    /**
     * Get the employee name associated with the employee code
     *
     * @param employeeCode The employee code to query
     *
     * @return The employee name associated with the employee code
     */
    public String getEmployeeName(String employeeCode) {
        return employees.get(employeeCode).getName();
    }

    /**
     * Get the employee photo associated with the employee code
     *
     * @param employeeCode The employee code to query
     *
     * @return The employee photo associated with the employee code
     */
    public File getEmployeePhoto(String employeeCode) {
        return employees.get(employeeCode).getEmployeePhoto();
    }

    /**
     * Get the map of the employees
     *
     * @return The map of the employees
     */
    public Map<String, Employee> getEmployees() {
        return employees;
    }

    /**
     * Get the list of PAFs associated with the employee code
     *
     * @param employeeCode The employee code to query
     *
     * @return The list of PAFs associated with the employee code
     */
    public List<PAF> getProfile(String employeeCode) {
        return employees.get(employeeCode).getProfile();
    }

    /**
     * Set the employee file location for the specified employee code
     *
     * @param employeeCode         The employee code to query
     * @param employeeFileLocation the employee file location for the specified employee code
     */
    public void setEmployeeFileLocation(String employeeCode, Path employeeFileLocation) {
        employees.get(employeeCode).setEmployeeFileLocation(employeeFileLocation);
    }

    /**
     * Set the employee photo for the specified employee code
     *
     * @param employeeCode  The employee code to query
     * @param employeePhoto The employee photo for the specified employee code
     */
    public void setEmployeePhoto(String employeeCode, File employeePhoto) {
        employees.get(employeeCode).setEmployeePhoto(employeePhoto);
    }

}
