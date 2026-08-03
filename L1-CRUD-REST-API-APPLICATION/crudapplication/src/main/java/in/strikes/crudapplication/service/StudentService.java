package in.strikes.crudapplication.service;


import in.strikes.crudapplication.entity.Student;
import in.strikes.crudapplication.repository.StudentRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


//LAYER 2 PASS IT TO REPOSITORY CLASS
@Service
public class StudentService {
    private StudentRepository studentRepository;

    //DEPENDENCY INJECTION
    public StudentService(StudentRepository studentRepository){
        this.studentRepository = studentRepository;
    }

    // STORE THE STUDENT IN THE DATABASE USING REPOSITORY
    public Student createStudent(Student studentReq){
        Student studentResp =studentRepository.save(studentReq);
        return studentResp;
    }

    // FETCH THE STUDENT FROM THE DATABASE USING REPOSITORY
    public Student getStudent(Long id){
        Optional<Student> studentResp = studentRepository.findById(id);
        if(studentResp.isPresent()){
            return studentResp.get();
        }
        return  null;
    }

    // FETCH ALL THE STUDENT FROM THE DATABASE USING REPOSITORY
    public List<Student> getAllStudent(){
        List<Student>studentList = studentRepository.findAll();
        return studentList;

    }

    // UPDATE THE STUDENT
    public Student updateStudent(Long id,Student studentReq){
        Optional<Student> existingStudent  = studentRepository.findById(id);
        if(existingStudent.isEmpty()){
            return null;
        }
        Student studentToSave = existingStudent.get();
        studentToSave.setAge(studentReq.getAge());
        studentToSave.setEmail(studentReq.getEmail());
        studentToSave.setName(studentReq.getName());
        studentToSave.setRollNo(studentReq.getRollNo());
        studentToSave.setSubject(studentReq.getSubject());

        return studentRepository.save(studentToSave);

    }

    // DELETE THE STUDENT FROM THE DATABASE
    public boolean deleteStudent(Long id){
        boolean isFound = studentRepository.existsById(id);
        if(!isFound) return false;
        studentRepository.deleteById(id);
        return true;
    }
}
