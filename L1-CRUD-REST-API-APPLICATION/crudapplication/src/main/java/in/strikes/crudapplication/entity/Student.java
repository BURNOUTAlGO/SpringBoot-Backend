package in.strikes.crudapplication.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

//THIS LAYER -
// 1. I WANT TO IN REQUEST THE DATA SHOULD BE MAPPED TO THIS CLASS
@Entity // ye batata hai ki ye  class entity hai aur iska object humare database mein jake mapped ho
public class Student {

    @Id // ye batati hai ki ye field primary key hai DATABASE MEIN.
    private Long id;

    private String name ;
    private String email;
    private int age;
    private int rollNo;
    private String subject;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getRollNo() {
        return rollNo;
    }

    public void setRollNo(int rollNo) {
        this.rollNo = rollNo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
