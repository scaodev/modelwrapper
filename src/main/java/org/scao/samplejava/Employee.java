package org.scao.samplejava;

import java.util.List;

/**
 * Date: 3/20/14
 * Time: 7:26 PM
 */
public class Employee extends Person{
    private String dob;
    private List<String> titles;

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles = titles;
    }
}
