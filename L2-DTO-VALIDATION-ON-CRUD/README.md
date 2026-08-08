# 📘 Spring Boot CRUD - Student (Learning Notes)

Ye repo maine Spring Boot seekhte hue banaya hai. Isme **Student CRUD application** banayi hai — jisme student ka `name`, `email`, `age`, `subject`, `rollNo` store hota hai, aur `id` primary key hai.

Ye README ek **revision guide** hai — jab bhi ye repo dobara kholoon, isse pura flow aur concepts yaad aa jaaye. 🚀

---

## 🎯 Project kya karta hai?

Student ka data database (MySQL) mein **Create, Read, Update, Delete** karne ke liye ek REST API banayi hai.

**Package:** `in.strikes.crudapplication`

---

## 🏗️ Project ka Flow (Architecture)

```
Client (Postman/Frontend)
        ↓
   Controller   →  Request receive karta hai, response bhejta hai
        ↓
    Service     →  Business logic yahan likhi jaati hai
        ↓
  Repository    →  Database se baat karta hai (JPA ke through)
        ↓
   MySQL DB (student_crud)
```

**Simple language mein samjho:**
- **Controller** (`controller` package) = Reception desk (request leta hai, kaam service ko de deta hai)
- **Service** (`service` package) = Manager (logic decide karta hai, kya karna hai)
- **Repository** (`repository` package) = Worker jo directly database se data la ke deta hai
- **Entity** (`entity` package) = Database table ka Java version (blueprint)

Isko hamesha yaad rakhna: **Controller → Service → Repository → Database**

---

## ⚙️ Project Setup - Step by Step

Project banate waqt (Spring Initializr se) ye 3 dependencies daali thi:

1. **Spring Web** → REST API banane ke liye (controllers, endpoints)
2. **Spring Data JPA** → Database ke saath easily kaam karne ke liye (bina raw SQL likhe)
3. **MySQL Driver** → Java application ko MySQL database se connect karne ke liye

> 💡 Yaad rakhna: JPA driver ke bina bhi kaam nahi karega, aur driver ke bina JPA MySQL se connect nahi ho payega. Dono chahiye.

### Database Config (`application.properties`)

```properties
spring.application.name=crudapplication

# MYSQL CONNECTION WITH THAT DATABASE
spring.datasource.url=jdbc:mysql://localhost:3306/student_crud
spring.datasource.username=root
spring.datasource.password=1234

# YE JPA- ENTITY CLASS KE OBJECT KA TABLE AUTOMATIC BANA DETI HAI
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

`ddl-auto=update` ki wajah se **table khud ban jaata hai** entity class ke basis pe — humein khud CREATE TABLE query nahi likhni padti.

---

## 🧩 4 Main Files/Layers jo banaye

### 1️⃣ Entity — `Student.java` (`entity` package)

Ye class database ki **table** ko represent karti hai. Har field ek column hai. Request se aane wala JSON data isi class ke object mein map hota hai.

```java
@Entity // ye batata hai ki ye class entity hai aur iska object database mein jake mapped ho
public class Student {

    @Id // ye batati hai ki ye field primary key hai DATABASE MEIN.
    private Long id;

    private String name;
    private String email;
    private int age;
    private int rollNo;
    private String subject;

    // getters and setters
}
```

**Yaad rakhne wali baat:** `@Entity` class ko table bana deta hai, aur `@Id` batata hai ki kaunsa field primary key hai. `id` ke liye abhi `@GeneratedValue` nahi lagaya — matlab **id manually request mein bhejni padegi**, auto-increment nahi ho raha.

> 📝 Note to self: Agar auto-generated ID chahiye (aur usually chahiye hoti hai), to `@GeneratedValue(strategy = GenerationType.IDENTITY)` add karna hoga `@Id` ke saath.

---

### 2️⃣ Repository — `StudentRepository.java` (`repository` package)

Ye interface hai jo `JpaRepository` ko extend karta hai. Isme humein khud CRUD methods likhne ki zarurat nahi — Spring Data JPA already de deta hai (`save()`, `findById()`, `findAll()`, `deleteById()`, `existsById()` etc.)

```java
// <Student, Long> → <Entity, Primary Key Type>
public interface StudentRepository extends JpaRepository<Student, Long> {
}
```

**Yaad rakhne wali baat:** Iske upar koi annotation (`@Repository` waghera) lagane ki zarurat nahi, kyunki ye ek **interface** hai — Spring ka IoC container interfaces ka bean nahi banata, JPA khud iska implementation runtime pe generate kar deta hai.

---

### 3️⃣ Service — `StudentService.java` (`service` package)

Ye layer business logic ke liye hai. Controller seedha repository ko call nahi karta — beech mein service aati hai.

```java
@Service
public class StudentService {
    private StudentRepository studentRepository;

    // Constructor-based Dependency Injection
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student studentReq) {
        return studentRepository.save(studentReq);
    }

    public Student getStudent(Long id) {
        Optional<Student> studentResp = studentRepository.findById(id);
        return studentResp.orElse(null);
    }

    public List<Student> getAllStudent() {
        return studentRepository.findAll();
    }

    public Student updateStudent(Long id, Student studentReq) {
        Optional<Student> existingStudent = studentRepository.findById(id);
        if (existingStudent.isEmpty()) {
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

    public boolean deleteStudent(Long id) {
        boolean isFound = studentRepository.existsById(id);
        if (!isFound) return false;
        studentRepository.deleteById(id);
        return true;
    }
}
```

**Yaad rakhne wali baat:**
- `@Service` batata hai ki ye class Spring ke liye ek "service bean" hai — business logic yahan likhi jaati hai.
- Dependency Injection **constructor ke through** ki hai (`@Autowired` field pe nahi laga), jo ki best practice mani jaati hai — Spring khud detect kar leta hai jab sirf ek constructor ho.
- `Optional<Student>` ka use kiya hai `findById()` se — kyunki student mil bhi sakta hai, nahi bhi. `isEmpty()` / `orElse(null)` se safely check karte hain, taaki `NullPointerException` na aaye.
- Update mein pehle existing student ko DB se nikala, fir uske fields naye data se update kiye, tab jaake dobara `save()` kiya — isse hi update ho jaata hai (kyunki same `id` hai).

---

### 4️⃣ Controller — `StudentContoller.java` (`controller` package)

Ye layer HTTP requests ko handle karta hai — jo bhi Postman ya frontend se request aayegi, wo yahin pehle aayegi.

```java
@RestController
@RequestMapping("/api/students")
public class StudentContoller {

    private StudentService studentService;

    // Constructor-based Dependency Injection
    public StudentContoller(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/create")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Student> readAllStudents(@PathVariable Long id) {
        Student studentResp = studentService.getStudent(id);
        if (studentResp == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentResp);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Student>> readAllStudents() {
        List<Student> studentList = studentService.getAllStudent();
        if (studentList.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(studentList);
    }

    @PutMapping("update/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentReq) {
        Student updatedStudent = studentService.updateStudent(id, studentReq);
        if (updatedStudent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("delete/{id}")
    public ResponseEntity<String> deleteStudent(@PathVariable Long id) {
        Boolean isDeleted = studentService.deleteStudent(id);
        if (!isDeleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("The Record Is Being Deleted");
    }
}
```

> ⚠️ **Note to self:** Class ka naam `StudentContoller` hai (typo — "Controller" nahi likha gaya, "n" reh gaya). Kaam pe koi fark nahi padta, lekin agar clean karna ho to rename kar dena.

---

## 🔑 Annotations jo seekhi (revision ke liye)

| Annotation | Kaam kya karta hai |
|---|---|
| `@Entity` | Class ko database table bana deta hai |
| `@Id` | Batata hai ki kaunsa field primary key hai |
| `@Service` | Class ko "business logic" wali bean bana deta hai, taaki Spring use manage kar sake |
| `@RequestBody` | Jab client JSON data bhejta hai (POST/PUT mein), usko Java object mein convert karta hai |
| `@RestController` | Controller class ko REST API controller banata hai (JSON response deta hai) |
| `@RequestMapping` | Base URL define karta hai (jaise `/api/students`) |
| `@PathVariable` | URL ke andar se value nikalta hai (jaise `/get/{id}` mein se `id`) |
| `@PostMapping` / `@GetMapping` / `@PutMapping` / `@DeleteMapping` | HTTP method (POST/GET/PUT/DELETE) ko specific method se map karta hai |

---

## 💡 `ResponseEntity` - Important Concept

`ResponseEntity` ka use response ko **fully control** karne ke liye hota hai — sirf data hi nahi, balki:
- **HTTP status code** (200 OK, 201 Created, 404 Not Found, etc.)
- **Headers**
- **Body** (actual data)

Is project mein use kiye gaye tarike:

```java
ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);  // 201 - create ke liye
ResponseEntity.ok(studentResp);                                   // 200 - success ke liye
ResponseEntity.notFound().build();                                // 404 - data na milne pe
```

**Yaad rakhne wali baat:** `ResponseEntity` na use karte to sirf object return hota (`Student`, `List<Student>` etc.), aur status code hamesha `200 OK` hi jaata — chahe student mila ho ya na mila ho. Isse frontend ko galat signal milta. `ResponseEntity` se hum khud decide karte hain ki kaunsa status code kab jaana chahiye.

---

## 🔗 API Endpoints (Actual, is project ke)

| Method | URL | Kaam |
|---|---|---|
| POST | `/api/students/create` | Naya student add karna |
| GET | `/api/students/get/{id}` | Ek student ko ID se dekhna |
| GET | `/api/students/getAll` | Saare students dekhna |
| PUT | `/api/students/update/{id}` | Student ka data update karna |
| DELETE | `/api/students/delete/{id}` | Student delete karna |

### Sample Request Body (POST/PUT)

```json
{
  "id": 1,
  "name": "Abhinav Sharma",
  "email": "abhinav@example.com",
  "age": 21,
  "subject": "Computer Science",
  "rollNo": 101
}
```

> ⚠️ Abhi `id` bhi manually bhejni padegi request mein (kyunki `@GeneratedValue` nahi laga), warna `id = null` chala jayega.

---

## 📝 Aage kya seekhna/fix karna hai (Next Steps)

- [ ] `@GeneratedValue(strategy = GenerationType.IDENTITY)` add karna `@Id` ke saath, taaki id auto-increment ho
- [ ] DTO pattern (Entity ko directly expose na karna request/response mein)
- [ ] Validation (`@Valid`, `@NotNull`, `@Email`)
- [ ] Exception Handling (`@ControllerAdvice`, `@ExceptionHandler`) — abhi sirf `null`/`notFound()` check ho raha hai
- [ ] Lombok (getters/setters khud likhne se bachne ke liye)
- [ ] Pagination aur Sorting `getAll` endpoint mein
- [ ] `StudentContoller` class ka naam fix karna (typo)

---

## ✅ Quick Revision Summary

> Client request → **Controller** (`@RestController`, `@RequestBody`, `ResponseEntity`) → **Service** (`@Service`, business logic) → **Repository** (`JpaRepository`, database calls) → **MySQL** (`student_crud` DB)
>
> Entity = Table ka Java version (`@Entity`, `@Id`)
>
> Dependency Injection is project mein **constructor ke through** ki gayi hai, field-level `@Autowired` nahi use hua.
