package in.strikes.crudapplication.contoller;

//1ST layer

import in.strikes.crudapplication.dto.StudentRequestDto;
import in.strikes.crudapplication.dto.StudentRequestUpdateDto;
import in.strikes.crudapplication.dto.StudentResponseDto;
import in.strikes.crudapplication.dto.StudentResponseUpdateDto;
import in.strikes.crudapplication.entity.Student;
import in.strikes.crudapplication.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//LAYER 1 PASS IT TO SERVICE
@RestController
@RequestMapping("/api/students")
public class StudentContoller {


    //DEPENDENCY INJECTION

    private StudentService studentService;

    public StudentContoller(StudentService studentService){
        this.studentService = studentService;
    }


    // VALIDATIONS - INSTALL DEPENDENCY - SPRING VALIDATION

    // CREATE STUDENT
    @PostMapping("/create")
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto studentRequestDto){// JSON request data converted into object
        StudentResponseDto createdStudent = studentService.createStudent(studentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }
    // GET STUDENT BY ID
    @GetMapping("/get")
    public ResponseEntity<StudentResponseDto> readAllStudents(@RequestParam Long id){
        StudentResponseDto studentResp = studentService.getStudent(id);

        if(studentResp==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentResp);
    }
    // GET ALL STUDENT
    @GetMapping("/getAll")
    public ResponseEntity<List<StudentResponseDto>> readAllStudents(){
        List<StudentResponseDto> studentList = studentService.getAllStudent();
        if(studentList.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentList);
    }

    // UPDATE
    @PutMapping("update")
    public ResponseEntity<StudentResponseUpdateDto> updateStudent(@RequestParam Long id, @RequestBody StudentRequestUpdateDto studentReq){
        StudentResponseUpdateDto updatedStudent = studentService.updateStudent(id,studentReq);
        if(updatedStudent==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedStudent);
    }
    // HARD DELETE
    @DeleteMapping("delete")
    public ResponseEntity<String> deleteStudent(@RequestParam Long id){
        Boolean isDeleted = studentService.deleteStudent(id);
        if(!isDeleted){
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok("The Record Is Being Deleted");

    }

    // SOFT DELETE
    @PatchMapping("delete-soft")
    public ResponseEntity<String> deleteStudentSoftly(@RequestParam Long id){
        Boolean isDeleted = studentService.deleteStudentSoftly(id);
        if(!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Student Deleted Softly");
    }




}
