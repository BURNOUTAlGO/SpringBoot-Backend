# 📘 Spring Boot - DTO + Validation (Learning Notes)
---

## 🤔 DTO kyu chahiye? (Pehle problem kya thi)

Pehle Controller seedha `Student` **Entity** ko hi request body aur response dono mein use kar raha tha.

**Isme problem kya thi:**
- Client ko wahi fields milte the jo database table mein hain — chahe wo expose karne layak ho ya na ho
- Client `id`, `createdAt` jaisi cheezein bhi bhej sakta tha jo usko nahi bhejni chahiye
- Create aur Update mein alag-alag fields chahiye hoti hain (jaise update mein `email` change nahi karne dena), lekin same Entity use karne se ye control nahi ho pata
- Validation lagani ho to Entity pe lagani padti, jo database mapping wali class hai — usko validation logic se mix karna sahi practice nahi hai

**DTO (Data Transfer Object)** ek alag POJO class hoti hai jo sirf **client ke saath data transfer** karne ke liye banti hai — Entity se alag. Isse Entity aur "API contract" dono independent reh jaate hain.

> 💡 Yaad rakhna: **Entity = Database ka shape**, **DTO = API ka shape**. Dono alag hone chahiye.

---

## ⚙️ Step 1: Validation Dependency Install ki

Naya dependency add kiya (Spring Initializr ya `pom.xml` mein):

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

Isse `jakarta.validation.constraints.*` ke annotations (`@NotNull`, `@NotBlank`, `@Email` etc.) use kar paate hain.

---

## 🧩 Step 2: DTO Classes banayi (`dto` package)

Ek hi Entity ke liye **4 alag DTOs** banayi — kyunki har operation (create/update) ki request/response ki zarurat alag hai.

### 1️⃣ `StudentRequestDto` — Create ke liye (validation ke saath)

Ye wahi fields rakhti hai jo client **create karte waqt bhejega**, aur har field pe validation lagayi:

```java
public class StudentRequestDto {

    @NotBlank(message = "Name cannot be null/empty or blank")
    @Size(min = 2, max = 50, message = "Student name must be within 2 to 50 character")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age should be atleast 18 years")
    private int age;

    @Email(message = "Email should be in proper format")
    private String email;

    @NotNull(message = "Roll no should not be null/empty")
    private Integer rollNo;

    @NotBlank(message = "Subject should not be null/empty or blank")
    private String subject;

    // getters and setters
}
```

**Yaad rakhne wali baat:** Isme `id` field hi nahi hai — kyunki create karte waqt client `id` nahi bhejta, database khud generate karega.

### 2️⃣ `StudentRequestUpdateDto` — Update ke liye (validation nahi, kam fields)

```java
public class StudentRequestUpdateDto {
    private String name;
    private int age;
    private int rollNo;
    private String subject;
    // getters and setters
}
```

**Yaad rakhne wali baat:** Isme `email` field hi nahi rakhi — matlab **update ke waqt email change nahi ki ja sakti**, DTO ke level pe hi restrict kar diya. Ye DTO ka sabse bada fayda hai — har operation ke liye alag "shape" bana sakte hain.

### 3️⃣ `StudentResponseDto` — Create/Get ka response

```java
public class StudentResponseDto {
    private Long id;
    private String name;
    private String email;
    private int age;
    private int rollNo;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // getters and setters
}
```

Client ko response mein sirf ye fields dikhte hain — `message` jaisa extra field bhi add kar diya (jo Entity mein nahi hota), taaki client ko friendly confirmation mile jaise `"Student saved successfully..."`.

### 4️⃣ `StudentResponseUpdateDto` — Update ka response

Same tarah ka, update operation ke response ke liye alag se banaya.

> 📝 **Pattern samjho:** Har operation (Create, Update) ke liye do DTO — ek **Request** (client se aane wala), ek **Response** (client ko jaane wala). Isse Entity kabhi bhi seedha expose nahi hoti.

---

## ✅ Step 3: Validation Annotations jo seekhi

| Annotation | Kaam kya karta hai |
|---|---|
| `@NotBlank` | String field `null`, empty (`""`), ya sirf spaces wali nahi honi chahiye |
| `@NotNull` | Field `null` nahi honi chahiye (empty string chalega, bas null nahi) |
| `@Size(min, max)` | String ki length ek range ke andar honi chahiye |
| `@Min(value)` | Number ki minimum value check karta hai |
| `@Email` | Field ek valid email format mein honi chahiye |
| `@Valid` | Controller mein `@RequestBody` ke saath lagate hain — isse Spring us DTO ke andar likhi saari validations ko **trigger** kar deta hai |

**Yaad rakhne wali baat:**
- `@NotBlank` sirf `String` ke liye hai (empty/blank check karta hai)
- `@NotNull` kisi bhi type ke liye chalta hai (sirf null check karta hai, empty nahi)
- `@Valid` na lagao to DTO ke andar ki saari `@NotBlank`/`@Email` waghera annotations **kaam hi nahi karengi** — validation trigger karne ka kaam `@Valid` ka hi hai

```java
@PostMapping("/create")
public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto studentRequestDto) {
    ...
}
```

---

## 🔄 Step 4: Service Layer mein Entity ↔ DTO Mapping

Service ka naya kaam ye bhi hai ki **DTO ko Entity mein convert kare, aur Entity ko wapas DTO mein convert kare**. Do helper methods banaye:

```java
// DTO → Entity (request aane pe)
private Student mapToEntity(StudentRequestDto studentRequestDto) {
    Student student = new Student();
    student.setName(studentRequestDto.getName());
    student.setAge(studentRequestDto.getAge());
    student.setRollNo(studentRequestDto.getRollNo());
    student.setEmail(studentRequestDto.getEmail());
    student.setSubject(studentRequestDto.getSubject());
    student.setDeleted(false);
    return student;
}

// Entity → DTO (response bhejte waqt)
public StudentResponseDto mapToDto(Student student) {
    StudentResponseDto dto = new StudentResponseDto();
    dto.setId(student.getId());
    dto.setName(student.getName());
    dto.setAge(student.getAge());
    dto.setEmail(student.getEmail());
    dto.setRollNo(student.getRollNo());
    dto.setSubject(student.getSubject());
    dto.setMessage("Student saved successfully...");
    dto.setCreatedAt(student.getCreatedAt());
    dto.setUpdatedAt(student.getUpdatedAt());
    return dto;
}
```

Flow ab aisa ban gaya:

```
Client → RequestDto (validated) → mapToEntity() → Entity → DB save
DB se Entity aayi → mapToDto() → ResponseDto → Client
```

> 📝 Note to self: Abhi manually field-by-field map kar raha hoon. Aage jaake **Builder Design Pattern** ya **MapStruct/ModelMapper** library use kar sakta hoon, taaki fields miss na hon aur code chota ho.

---

## 🕓 Step 5: Entity mein naye fields add kiye (auditing + soft delete)

DTO mein `createdAt`/`updatedAt` bhej rahe the, isliye `Student` Entity mein bhi ye fields add karne pade, aur ek `deleted` flag bhi:

```java
private boolean deleted;
private LocalDateTime createdAt;
private LocalDateTime updatedAt;
```

- `createdAt` / `updatedAt` → Service mein manually set kiye (`LocalDateTime.now()`) create aur update ke waqt
- `deleted` → **Soft Delete** ke liye — record ko database se actually delete nahi karte, bas `deleted = true` kar dete hain

---

## 🗑️ Step 6: Soft Delete ka naya concept seekha

Pehle sirf **Hard Delete** tha (`deleteById()` se record permanently gone). Ab **Soft Delete** bhi add kiya:

```java
public boolean deleteStudentSoftly(Long id) {
    Optional<Student> existingStudent = studentRepository.findByIdAndDeletedIsFalse(id);
    if (existingStudent.isEmpty()) {
        return false;
    }
    Student studentToSave = existingStudent.get();
    studentToSave.setDeleted(true);
    studentRepository.save(studentToSave);
    return true;
}
```

**Hard Delete vs Soft Delete:**

| | Hard Delete | Soft Delete |
|---|---|---|
| Kya hota hai | Row database se **permanently** hat jaati hai | Row rehti hai, bas `deleted = true` flag set ho jaata hai |
| Data wapas mil sakta hai? | ❌ Nahi | ✅ Haan, database mein hai hi |
| Use case | Jab data ki zarurat kabhi nahi | Jab audit/history rakhni ho (real-world apps mein zyada common) |

Isi wajah se Repository mein naye custom methods bhi likhne pade (Spring Data JPA method naming se khud query bana leta hai):

```java
Optional<Student> findByIdAndDeletedIsFalse(Long id);
List<Student> findByDeletedIsFalse();
```

**Yaad rakhne wali baat:** `getStudent()` aur `getAllStudent()` ab in naye methods ko call karte hain — taaki jo student **soft-delete** ho chuka hai, wo GET requests mein wapas na aaye, jaise wo exist hi nahi karta.

---

## 🔗 Step 7: Controller update kiya (DTO + Validation use karne ke liye)

```java
// CREATE - ab validation ke saath
@PostMapping("/create")
public ResponseEntity<StudentResponseDto> createStudent(@Valid @RequestBody StudentRequestDto studentRequestDto) {
    StudentResponseDto createdStudent = studentService.createStudent(studentRequestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
}

// GET BY ID - ab @PathVariable ki jagah @RequestParam use kiya
@GetMapping("/get")
public ResponseEntity<StudentResponseDto> readAllStudents(@RequestParam Long id) {
    StudentResponseDto studentResp = studentService.getStudent(id);
    if (studentResp == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(studentResp);
}

// UPDATE
@PutMapping("update")
public ResponseEntity<StudentResponseUpdateDto> updateStudent(@RequestParam Long id, @RequestBody StudentRequestUpdateDto studentReq) {
    StudentResponseUpdateDto updatedStudent = studentService.updateStudent(id, studentReq);
    if (updatedStudent == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(updatedStudent);
}

// HARD DELETE
@DeleteMapping("delete")
public ResponseEntity<String> deleteStudent(@RequestParam Long id) {
    Boolean isDeleted = studentService.deleteStudent(id);
    if (!isDeleted) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok("The Record Is Being Deleted");
}

// SOFT DELETE - naya endpoint, naya HTTP method bhi seekha
@PatchMapping("delete-soft")
public ResponseEntity<String> deleteStudentSoftly(@RequestParam Long id) {
    Boolean isDeleted = studentService.deleteStudentSoftly(id);
    if (!isDeleted) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok("Student Deleted Softly");
}
```

**Do naye cheezein is layer mein seekhi:**

1. **`@PathVariable` → `@RequestParam` mein switch kiya** GET/UPDATE/DELETE endpoints mein — ab URL `/get/{id}` ki jagah `/get?id=1` ban gaya. Dono valid tarike hain, bas ab query parameter use kar rahe hain path variable ki jagah.
2. **`@PatchMapping`** naya seekha — **PATCH** method **partial update** ke liye hota hai (yahan soft-delete ke liye use kiya, sirf ek field `deleted` change ho rahi hai, poora record nahi).

---

## 🔗 API Endpoints (updated)

| Method | URL | Kaam |
|---|---|---|
| POST | `/api/students/create` | Naya student add karna (validated `StudentRequestDto`) |
| GET | `/api/students/get?id={id}` | Ek student ko ID se dekhna (sirf non-deleted) |
| GET | `/api/students/getAll` | Saare (non-deleted) students dekhna |
| PUT | `/api/students/update?id={id}` | Student ka data update karna |
| DELETE | `/api/students/delete?id={id}` | Student ko **hard delete** karna |
| PATCH | `/api/students/delete-soft?id={id}` | Student ko **soft delete** karna (`deleted = true`) |

### Sample Create Request (validation ke saath)

```json
{
  "name": "Abhinav Sharma",
  "age": 21,
  "email": "abhinav@example.com",
  "rollNo": 101,
  "subject": "Computer Science"
}
```

Agar `name` empty bheja, ya `age` 18 se kam, ya `email` galat format mein — to Spring khud `400 Bad Request` return karega, validation `message` ke saath (kyunki `@Valid` lagaya hai).

---

## 📝 Aage kya seekhna hai (Next Steps)

- [ ] Validation errors ko **properly format** karna — abhi Spring ka default error response aa raha hai, `@ControllerAdvice` + `@ExceptionHandler(MethodArgumentNotValidException.class)` bana ke clean JSON error response dena
- [ ] `mapToEntity` / `mapToDto` manual mapping ki jagah **MapStruct** ya **Builder Pattern** try karna
- [ ] `StudentResponseDto` mein `message` field thoda ajeeb hai (response object mein status-jaisa message) — isko `ApiResponse<T>` wrapper class mein daalna better practice hai
- [ ] Soft-deleted students ke liye ek separate admin endpoint bana sakte hain jo unko bhi dikhaye

---

## ✅ Quick Revision Summary

> **DTO** = API ka apna shape, Entity se alag → Create/Update ke liye alag Request DTO, Response ke liye alag Response DTO
>
> **Validation** = `spring-boot-starter-validation` dependency + `jakarta.validation.constraints` annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Email`) DTO ke fields pe, aur Controller mein `@Valid` lagana zaroori — warna validation trigger hi nahi hogi
>
> **Soft Delete** = Row delete nahi hoti, bas `deleted = true` flag set hota hai; GET methods mein `findByDeletedIsFalse()` use karke deleted records ko chhupaya
>
> **Naye annotations/concepts:** `@Valid`, `@NotBlank`, `@NotNull`, `@Min`, `@Email`, `@RequestParam`, `@PatchMapping`