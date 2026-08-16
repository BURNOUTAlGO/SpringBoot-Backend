package in.strikes.crudapplication.dto;


// POJO CLASS - Isme hum keval wahi fields rakhenege jo hum client se bhejwana chahte hai.
// entity class se whi fields utha lo jo client bhejega

import jakarta.validation.constraints.*;

public class StudentRequestDto {

    // Validations
    @NotBlank(message = "Name cannot be null/empty or blank")
    @Size(min=2,max = 50,message = "Student name must be within 2 to 50 character")
    private String name ;

    @NotNull(message = "Age is required")
    @Min(value=18,message = "Age should be atleast 18 years")
    private int age;

    @Email(message = "Email should be in proper format")
    private String email;

    @NotNull(message = "Roll no should not be null/empty ")
    private Integer rollNo;

    @NotBlank(message = "Subject should not be null/empty or blank")
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
}
