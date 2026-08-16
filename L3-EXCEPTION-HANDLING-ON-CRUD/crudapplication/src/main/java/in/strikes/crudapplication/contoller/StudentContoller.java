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
    @PostMapping
    public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto studentRequestDto){// JSON request data converted into object
        StudentResponseDto createdStudent = studentService.createStudent(studentRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }
    // GET STUDENT BY ID
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> readAllStudents(@PathVariable Long id){
        StudentResponseDto studentResp = studentService.getStudent(id);
        return ResponseEntity.ok(studentResp);
    }
    // GET ALL STUDENT
    @GetMapping
    public ResponseEntity<List<StudentResponseDto>> readAllStudents(){
        List<StudentResponseDto> studentList = studentService.getAllStudent();
        return ResponseEntity.ok(studentList);
    }

    // UPDATE
    @PutMapping
    public ResponseEntity<StudentResponseUpdateDto> updateStudent(@RequestParam Long id, @RequestBody StudentRequestUpdateDto studentReq){
        StudentResponseUpdateDto updatedStudent = studentService.updateStudent(id,studentReq);
        return ResponseEntity.ok(updatedStudent);
    }
    // HARD DELETE
    @DeleteMapping("delete")
    public ResponseEntity<String> deleteStudent(@RequestParam Long id){
        studentService.deleteStudent(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();

    }

    // SOFT DELETE
    @PatchMapping("delete-soft")
    public ResponseEntity<String> deleteStudentSoftly(@RequestParam Long id){
        studentService.deleteStudentSoftly(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }




}
