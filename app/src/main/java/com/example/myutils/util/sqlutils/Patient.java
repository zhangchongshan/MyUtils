package com.example.myutils.util.sqlutils;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhangchongshan on 2017/6/16.
 */

public class Patient extends Object implements Serializable {

    private static final long serialVersionUID = 1L;

    private String Name = new String("");
    private String ID = new String("");
    private Date DOB = new Date();
    private int  Sex = 0;

    public final static int FEMALE = 0;
    public final static int MALE = 1;

    public void setName(String name) {
        Name = name;
    }
    public String getName() {
        return Name;
    }

    public void setID(String id) {
        this.ID = id;
    }
    public String getID() {
        return this.ID;
    }

    public void setDOB(Date dob) {
        this.DOB = dob;
    }

    public Date getDOB() {
        return this.DOB;
    }

    public void setSex(int sex) {
        this.Sex = sex;
    }

    public int getSex() {
        return this.Sex;
    }

}
