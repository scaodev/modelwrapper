package org.scao.samplejava;

import java.util.Map;
import java.util.Set;

/**
 * Date: 3/16/14
 * Time: 8:45 PM
 */
public class Person {
    private String firstName;
    private String lastName;
    private String[] jobs;
    Map<String, Double> locations;
    Set<String> skills;

    public Set<String> getSkills() {
        return skills;
    }

    public void setSkills(Set<String> skills) {
        this.skills = skills;
    }

    public String[] getJobs() {
        return jobs;
    }

    public void setJobs(String[] jobs) {
        this.jobs = jobs;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Map<String, Double> getLocations() {
        return locations;
    }

    public void setLocations(Map<String, Double> locations) {
        this.locations = locations;
    }
}
