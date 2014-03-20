package org.scao.samplejava;

import java.util.List;

/**
 * Date: 3/16/14
 * Time: 8:45 PM
 */
public class Person {
    private String firstName;
    private String lastName;
    private List<String> titles;
    private List<String> jobs;

    public List<String> getJobs() {
        return jobs;
    }

    public void setJobs(List<String> jobs) {
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

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
}
