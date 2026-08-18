package in.learning.service;

import in.learning.modal.Student;
import org.springframework.stereotype.Service;

@Service
public class StudentService {

    public void createStudent(Student student){
        System.out.println("Student Saved...");
        System.out.println(student.getAge());
        System.out.println(student.getEmail());
        System.out.println(student.getName());
    }

}
