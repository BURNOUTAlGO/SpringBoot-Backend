package in.strikes.crudapplication.service;
import in.strikes.crudapplication.dto.StudentRequestDto;
import in.strikes.crudapplication.dto.StudentRequestUpdateDto;
import in.strikes.crudapplication.dto.StudentResponseDto;
import in.strikes.crudapplication.dto.StudentResponseUpdateDto;
import in.strikes.crudapplication.entity.Student;
import in.strikes.crudapplication.exception.DuplicateResourceException;
import in.strikes.crudapplication.exception.ResourceNotFoundException;
import in.strikes.crudapplication.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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




    // YE METHOD REQUEST DTO KO POORI ENTITY CLASS MEIN MAP KARDEGA
    private Student mapToEntity(StudentRequestDto studentRequestDto){
        Student student = new Student();

        student.setName(studentRequestDto.getName());
        student.setAge(studentRequestDto.getAge());
        student.setRollNo(studentRequestDto.getRollNo());
        student.setEmail(studentRequestDto.getEmail());
        student.setSubject(studentRequestDto.getSubject());
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());


        student.setDeleted(false);
        return student;

        // later we use Builder Design pattern  - because we if we missed any fields

    }
    // YE METHOD POORE ENTITY KO DTO RESPONSE MEIN MAPPE KAR DEGA
    public StudentResponseDto mapToDto(Student student){
        StudentResponseDto studentRespDto = new StudentResponseDto();
        studentRespDto.setId(student.getId());
        studentRespDto.setName(student.getName());
        studentRespDto.setAge(student.getAge());
        studentRespDto.setEmail(student.getEmail());
        studentRespDto.setMessage(student.getEmail());
        studentRespDto.setRollNo(student.getRollNo());
        studentRespDto.setSubject(student.getSubject());
        studentRespDto.setMessage("Student saved successfully...");
        studentRespDto.setCreatedAt(student.getCreatedAt());
        studentRespDto.setUpdatedAt(student.getUpdatedAt());
        return studentRespDto;

    }

    // STORE THE STUDENT IN THE DATABASE USING REPOSITORY || here we are expecting the request dto fields.  but we want to store the full entity
    public StudentResponseDto createStudent(StudentRequestDto studentReqDto){
        // dto request fields map to entity
        Student student = mapToEntity(studentReqDto);

        if(emailExist(student)){
            throw new DuplicateResourceException("Student with this email "+student.getEmail()+"already exist");
        }
        // client fields +  remaing entity fields = stored in database
        Student  studentResp = studentRepository.save(student);
        // response( all fields ) mapped to response (fields) dto which i want to send to client .
        return mapToDto(studentResp);

    }

    private boolean emailExist(Student student){
        return studentRepository.existsByEmail(student.getEmail());

    }

    // FETCH THE STUDENT FROM THE DATABASE USING REPOSITORY
    public StudentResponseDto getStudent(Long id){
        // bhai jpa apne aap ye findByIdAndDeletedIsFalse(id) method dekh ke samjh jayega ki find student whose id is {id} and deletd is false
        // but we have to declare this function in repository
        Student studentResp = studentRepository.findByIdAndDeletedIsFalse(id).orElseThrow(()-> new ResourceNotFoundException("Student with id : "+id+"Resource not found.."));


        //agar koi runtime exception mili to ye request jayegi globalexceptionhandler ke pass.
        return mapToDto(studentResp);
    }

    // FETCH ALL THE STUDENT FROM THE DATABASE USING REPOSITORY
    public List<StudentResponseDto> getAllStudent(){
        List<Student>studentList = studentRepository.findByDeletedIsFalse();
        return studentList.stream().map(this::mapToDto).toList();

    }

    // UPDATE STUDENT DTO

    private StudentResponseUpdateDto mapToUpdateDto(Student savedStudent) {
        StudentResponseUpdateDto studentResponseUpdateDto = new StudentResponseUpdateDto();

        studentResponseUpdateDto.setAge(savedStudent.getAge());
        studentResponseUpdateDto.setEmail(savedStudent.getEmail());
        studentResponseUpdateDto.setName(savedStudent.getName());
        studentResponseUpdateDto.setId(savedStudent.getId());
        studentResponseUpdateDto.setSubject(savedStudent.getSubject());
        studentResponseUpdateDto.setUpdatedAt(savedStudent.getUpdatedAt());
        studentResponseUpdateDto.setMessage("Student Updated Successfully...");

        return studentResponseUpdateDto;

    }


    // UPDATE THE STUDENT
    public StudentResponseUpdateDto updateStudent(Long id, StudentRequestUpdateDto studentReq){
        Student existingStudent  = studentRepository.findByIdAndDeletedIsFalse(id).orElseThrow(()->new ResourceNotFoundException("The Student with this id- "+id+" Not found"));

        existingStudent.setAge(studentReq.getAge());
        existingStudent.setName(studentReq.getName());
        existingStudent.setRollNo(studentReq.getRollNo());
        existingStudent.setSubject(studentReq.getSubject());
        existingStudent.setUpdatedAt(LocalDateTime.now());

        Student savedStudent = studentRepository.save(existingStudent);
        //now we have to map this full student entity into  responseupdate dto
        StudentResponseUpdateDto updatedStudentResp = mapToUpdateDto(savedStudent);
        return updatedStudentResp;


    }

    // DELETE THE STUDENT FROM THE DATABASE
    public void deleteStudent(Long id){
        Student studentToBeDeleted = studentRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("The Student with this id-"+id+" Not found"));
        studentRepository.delete(studentToBeDeleted);
    }

    // SOFT DELETING THE STUDENT DATA
    public void deleteStudentSoftly(Long id){
        // get
        // delete =true
        //save
        Student studentToBeDeleted = studentRepository.findByIdAndDeletedIsFalse(id).orElseThrow(()->new ResourceNotFoundException("The Student with this id-"+id+" Not found"));
        studentRepository.delete(studentToBeDeleted);
        studentToBeDeleted.setDeleted(true);
        studentRepository.save(studentToBeDeleted);
    }
}
