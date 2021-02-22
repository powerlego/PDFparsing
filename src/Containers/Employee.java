package Containers;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

/**
 * The data structure for an employee
 *
 * @author Nicholas Curl
 */
public class Employee {
    /**
     * The employee name
     */
    private final String name;
    /**
     * The PAFs associated to the employee
     */
    private final List<PAF> profile;
    /**
     * The employee code
     */
    private final String code;
    /**
     * The employee photo
     */
    private File employeePhoto;
    /**
     * The employee's file location
     */
    private Path employeeFileLocation;

    /**
     * The constructor for the employee
     *
     * @param name The employee name
     * @param code The employee code
     */
    public Employee(String name, String code) {
        this.name = name;
        this.profile = new LinkedList<>();
        this.code = code;
    }

    /**
     * Adds a PAF to be associated to the employee
     *
     * @param paf The PAF to add
     */
    public void addPAF(PAF paf) {
        this.profile.add(paf);
    }

    /**
     * Compares the employee name of the specified object if it is an instance of employee
     *
     * @param obj The object to compare
     *
     * @return True if the object is an instance of Employee and the employee names are equal false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Employee) {
            Employee employee = (Employee) obj;
            return this.name.equals(employee.name);
        } else {
            return false;
        }
    }

    /**
     * Get the employee code
     *
     * @return The employee code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the employee file location
     *
     * @return The employee file location
     */
    public Path getEmployeeFileLocation() {
        return employeeFileLocation;
    }

    /**
     * Sets the employee file location
     *
     * @param employeeFileLocation The path to the employee file location
     */
    public void setEmployeeFileLocation(Path employeeFileLocation) {
        this.employeeFileLocation = employeeFileLocation;
    }

    /**
     * Get the employee photo
     *
     * @return The employee photo
     */
    public File getEmployeePhoto() {
        return employeePhoto;
    }

    /**
     * Sets the employee photo
     *
     * @param employeePhoto File of the employee photo
     */
    public void setEmployeePhoto(File employeePhoto) {
        this.employeePhoto = employeePhoto;
    }

    /**
     * Get the employee name
     *
     * @return The employee name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the PAFs associated with the employee
     *
     * @return The PAFs associated with the employee
     */
    public List<PAF> getProfile() {
        return profile;
    }
}
