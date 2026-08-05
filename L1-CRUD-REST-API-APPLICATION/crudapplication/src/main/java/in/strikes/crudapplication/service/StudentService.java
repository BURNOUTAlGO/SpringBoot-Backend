package in.strikes.crudapplication.service;
import in.strikes.crudapplication.entity.Student;
import in.strikes.crudapplication.repository.StudentRepository;
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
        studentReq.setDeleted(false);
        Student studentResp = studentRepository.save(studentReq);
        return studentResp;
    }

    // FETCH THE STUDENT FROM THE DATABASE USING REPOSITORY
    public Student getStudent(Long id){
        // bhai jpa apne aap ye findByIdAndDeletedIsFalse(id) method dekh ke samjh jayega ki find student whose id is {id} and deletd is false
        // but we have to declare this function in repository
        Optional<Student> studentResp = studentRepository.findByIdAndDeletedIsFalse(id);
        if(studentResp.isPresent()){
            return studentResp.get();
        }
        return  null;
    }

    // FETCH ALL THE STUDENT FROM THE DATABASE USING REPOSITORY
    public List<Student> getAllStudent(){
        List<Student>studentList = studentRepository.findByDeletedIsFalse();
        return studentList;

    }

    // UPDATE THE STUDENT
    public Student updateStudent(Long id,Student studentReq){
        Optional<Student> existingStudent  = studentRepository.findByIdAndDeletedIsFalse(id);
        if(existingStudent.isEmpty()){
            return null;
        }
        Student studentToSave = existingStudent.get();
        studentToSave.setAge(studentReq.getAge());
        studentToSave.setEmail(studentReq.getEmail());
        studentToSave.setName(studentReq.getName());
        studentToSave.setRollNo(studentReq.getRollNo());
        studentToSave.setSubject(studentReq.getSubject());
        studentToSave.setDeleted(false);

        return studentRepository.save(studentToSave);

    }

    // DELETE THE STUDENT FROM THE DATABASE
    public boolean deleteStudent(Long id){
        boolean isFound = studentRepository.existsById(id);
        if(!isFound) return false;
        studentRepository.deleteById(id);
        return true;
    }

    // SOFT DELETING THE STUDENT DATA
    public boolean deleteStudentSoftly(Long id){
        // get
        // delete =true
        //save
        Optional<Student> existingstudent = studentRepository.findByIdAndDeletedIsFalse(id);
        if(existingstudent.isEmpty()){
            return false;
        }

        Student studentToSave = existingstudent.get();
        studentToSave.setDeleted(true);
        studentRepository.save(studentToSave);
        return true;


    }
}
