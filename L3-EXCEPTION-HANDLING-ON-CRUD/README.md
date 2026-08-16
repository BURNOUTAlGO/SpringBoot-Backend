# 📘 Spring Boot - Exception Handling (Learning Notes)
---

## 🤔 Exception Handling kyu chahiye? (Pehle problem kya thi)

Pehle jab bhi koi error hoti thi (jaise galat `id` diya, ya duplicate email se create kiya), to Spring apna **default, ugly error response** bhejta tha — jisme stack trace jaisi cheezein hoti hain, jo client ke liye na to readable hai na hi consistent.

**Isme problem kya thi:**
- Har error ka response **alag-alag shape** ka aata tha — koi fixed structure nahi
- Client ko internal Java stack trace dikh jaata tha, jo **security ke liye bhi bura** hai (internal details leak hoti hain)
- Controller khud har jagah `if (student == null) return notFound()` jaisे manual null-checks likhta tha — code messy aur repetitive ho jaata tha
- Validation fail hone pe bhi Spring ka apna default error format aata tha, jisme ye pata nahi chalta tha ki **exactly kaunsi field** galat gayi

**Chahiye kya tha:** Har error ka ek **fixed, predictable JSON shape** — chahe error kahin se bhi aaye (Service layer se, validation se, ya kahin bhi), client ko hamesha same structure mile.

> 💡 Yaad rakhna: Exception Handling ka poora point hi ye hai — **"jahan bhi error aaye, uska response ek jagah se, ek hi format mein nikle."** Isi centralization ko **`@RestControllerAdvice`** enable karta hai.

---

## 🧭 Implementation ka overall flow

```
Service layer mein error hui
        │
        ▼
throw new CustomException("message")   ◄── (jaise ResourceNotFoundException)
        │
        ▼
Spring khud is exception ko "bubble up" hone deta hai
        │
        ▼
GlobalExceptionHandler (@RestControllerAdvice) ise catch karta hai
        │
        ▼
Matching @ExceptionHandler method chalta hai
        │
        ▼
Fixed-shape JSON error response client ko milta hai
```

Ab neeche is poore flow ko step-by-step banate hain.

---

## 🧩 Step 1: Custom Exceptions banayi (`exception` package)

Sabse pehle apni khud ki, **meaningful naam wali** exception classes banayi — generic `RuntimeException` seedha throw karne ki jagah. Dono `RuntimeException` ko **extend** karti hain (matlab **unchecked exceptions** hain, inko har jagah `throws` declare karne ki zarurat nahi):

```java
// jab koi resource (student) DB mein milta hi nahi
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);   // ye message GlobalExceptionHandler tak forward hota hai
    }
}
```

```java
// jab koi resource pehle se hi exist karta hai (jaise duplicate email)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
```

**Yaad rakhne wali baat:** Ye dono classes khud kuch nahi karti — bas ek "named signal" hain. Jab Service layer `throw new ResourceNotFoundException("...")` karta hai, to Spring us exception ko catch karne ke liye **GlobalExceptionHandler** mein dhoondta hai (agar koi matching `@ExceptionHandler` mile to wahi chalega).

> 📝 **Kyu custom exception, sirf `RuntimeException` throw kyu nahi kar diya?** Kyunki agar sab jagah generic `RuntimeException` throw karo, to GlobalExceptionHandler ko pata hi nahi chalega ki error **"not found" type** ki thi ya **"duplicate" type** ki — sab ek hi generic `500` bankar chala jaayega. Named exceptions se hum har error ko **sahi HTTP status code** (404, 409, etc.) de paate hain.

---

## 🧩 Step 2: Error Response DTOs banayi

Do response shapes banayi — ek **normal error** ke liye, ek **validation error** (jisme field-wise errors bhi chahiye) ke liye.

**`ExceptionResponseDto`** — normal errors ke liye (404, 409, 500 etc.):

```java
public class ExceptionResponseDto {
    private LocalDateTime timestamp;
    private int statusCode;
    private String error;
    private String message;
    private String path;
    // constructor + getters/setters
}
```

**`ValidationExceptionResponseDto`** — sirf validation fail hone pe, isme ek extra field hai:

```java
public class ValidationExceptionResponseDto {
    private LocalDateTime timestamp;
    private int statusCode;
    private String error;
    private String message;
    private String path;
    private Map<String, String> fieldErrors;   // 👈 ye extra hai
    // constructor + getters/setters
}
```

**`fieldErrors`** ek `Map<String, String>` hai — jaise:
```json
{
  "name": "Name cannot be null/empty or blank",
  "age": "Age should be atleast 18 years"
}
```
Har field ka apna specific error message, taaki client ko pata chale **kaunsa field** galat gaya, na ki sirf "validation failed".

> 📝 **Pattern samjho:** Jaise Request/Response DTO alag-alag rakhte hain, waise hi yahan bhi **error ka "shape" bhi use-case ke hisaab se alag** rakha — normal error vs validation error (jisme field-level detail extra chahiye).

---

## 🧩 Step 3: `GlobalExceptionHandler` — sabka control room

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    ...
}
```

**`@RestControllerAdvice`** = `@ControllerAdvice` + `@ResponseBody` mila ke. Iska matlab:
- Ye class **poore application** ke saare Controllers ko "watch" karti hai (globally applicable hai, kisi ek Controller tak limited nahi)
- Jo bhi method iske andar `@ExceptionHandler` se mark ho, uska return value **seedha JSON response** ban jaata hai (jaise normal `@RestController` mein hota hai)

Har handler method ka pattern same hai — bas exception type aur HTTP status alag:

| Exception | HTTP Status | Kab trigger hoti hai |
|---|---|---|
| `MethodArgumentNotValidException` | `400 BAD_REQUEST` | Jab `@Valid` wali DTO ki koi validation fail ho (jaise `@NotBlank` field khali aayi) |
| `ResourceNotFoundException` | `404 NOT_FOUND` | Jab `id` se student dhoonda aur mila nahi (`orElseThrow(...)`) |
| `DuplicateResourceException` | `409 CONFLICT` | Jab same email se dobara student create karne ki koshish ki |
| `RuntimeException` | `500 INTERNAL_SERVER_ERROR` | Koi bhi generic unchecked exception jo upar wale specific handlers mein nahi aayi |
| `Exception` | `500 INTERNAL_SERVER_ERROR` | Sabse **last fallback** — jo bhi bach gaya, sab yahan aayega |

```java
@ExceptionHandler(ResourceNotFoundException.class)
public ResponseEntity<ExceptionResponseDto> handleResourceNotFoundException(
        ResourceNotFoundException ex, HttpServletRequest request) {

    ExceptionResponseDto exceptionResponse = new ExceptionResponseDto(
            LocalDateTime.now(),
            HttpStatus.NOT_FOUND.value(),
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI()
    );
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
}
```

**`MethodArgumentNotValidException`** wala handler thoda special hai — isme saare field errors ko ek `Map` mein collect karte hain:

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ValidationExceptionResponseDto> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex, HttpServletRequest request) {

    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors()
            .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));

    ValidationExceptionResponseDto exceptionResponse = new ValidationExceptionResponseDto(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation Failed",
            request.getRequestURI(),
            fieldErrors
    );
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
}
```

`ex.getBindingResult().getFieldErrors()` — Spring khud saari fail hui validations ki list de deta hai, hum bas har `error` se **field ka naam** (`error.getField()`) aur **uska message** (`error.getDefaultMessage()`) nikaal ke Map mein daal rahe hain.

**`HttpServletRequest request`** — ye parameter isliye add kiya taaki `request.getRequestURI()` se pata chale error **kis endpoint pe** aayi (jaise `/api/students/get`), aur wo `path` field mein chala jaaye.

---

### ⚠️ Important gotcha: Handler ka "sahi order" match hona

Spring exception ko uske **exact type** ya **closest parent type** wale handler tak route karta hai:

```
ResourceNotFoundException  ──extends──▶  RuntimeException  ──extends──▶  Exception
DuplicateResourceException ──extends──▶  RuntimeException  ──extends──▶  Exception
```

- Agar `ResourceNotFoundException` throw hui → Spring **specific handler** (`handleResourceNotFoundException`) ko hi call karega, `RuntimeException` wale ko nahi (kyunki **most specific match** jeetta hai)
- Agar koi **naya/未-handled** `RuntimeException` (jiska koi specific handler nahi hai) throw hua → tab `handleRuntimeException` (500) chalega
- Agar koi aisi exception aayi jo `RuntimeException` bhi nahi hai (jaise koi checked exception) → tab sabse aakhri **`Exception.class`** wala fallback chalega

> 💡 Yaad rakhna: **Zyada specific `@ExceptionHandler` hamesha jeetta hai** generic wale se — chahe class mein order kuch bhi likha ho. Isliye specific exceptions (`ResourceNotFoundException`, `DuplicateResourceException`) banana hi behtar practice hai generic `RuntimeException` throw karte rehne se — isse client ko sahi HTTP status code (404, 409) milta hai, sirf 500 nahi.

---

## 🧩 Step 4: Service layer mein exceptions throw karna

Ab Service layer ko update kiya — jahan bhi pehle manual null-check hota tha, wahan ab seedha exception throw karte hain:

```java
Student existingStudent = studentRepository.findByIdAndDeletedIsFalse(id)
        .orElseThrow(() -> new ResourceNotFoundException("The Student with this id- " + id + " Not found"));
```

**`orElseThrow(...)`** — `Optional` ka ek method jo agar value present hai to wahi return karta hai, aur agar **empty** hai to andar diya gaya **Supplier** (`() -> new ResourceNotFoundException(...)`) call karke wo exception throw kar deta hai. Isse `if (optional.isEmpty()) { throw ... }` jaisa manual check nahi likhna padta — ek line mein kaam ho jaata hai.

Duplicate check ke liye:

```java
if (emailExist(student)) {
    throw new DuplicateResourceException("Student with this email " + student.getEmail() + " already exist");
}
```

> 📝 **Design change (pehle vs ab):** Pehle Controller khud `if(student == null) return notFound()` check karta tha. Ab wo responsibility Service layer ko de di — Service ab seedha exception **throw** karta hai, aur Controller ko koi null-check likhne ki zarurat nahi. Exception khud-ba-khud **GlobalExceptionHandler** tak "bubble up" ho jaati hai. Isi wajah se Controller methods ab chhote aur clean dikhte hain.

**Kahan-kahan use hui:**

| Service Method | Exception | Kab throw hoti hai |
|---|---|---|
| `createStudent()` | `DuplicateResourceException` | Agar `emailExist()` true return kare |
| `getStudent(id)` | `ResourceNotFoundException` | Agar `findByIdAndDeletedIsFalse(id)` empty `Optional` de |
| `updateStudent(id, ...)` | `ResourceNotFoundException` | Agar update karne se pehle student mila hi nahi |
| `deleteStudent(id)` | `ResourceNotFoundException` | Agar delete karne se pehle student mila hi nahi |
| `deleteStudentSoftly(id)` | `ResourceNotFoundException` | Agar soft-delete karne se pehle student mila hi nahi |

---

## 🔗 API Error Response Examples

### Validation fail (400)
```json
{
  "timestamp": "2026-08-16T10:15:30",
  "statusCode": 400,
  "error": "Bad Request",
  "message": "Validation Failed",
  "path": "/api/students/create",
  "fieldErrors": {
    "name": "Name cannot be null/empty or blank",
    "age": "Age should be atleast 18 years"
  }
}
```

### Student nahi mila (404)
```json
{
  "timestamp": "2026-08-16T10:16:02",
  "statusCode": 404,
  "error": "Not Found",
  "message": "The Student with this id- 55 Not found",
  "path": "/api/students/get"
}
```

### Duplicate email (409)
```json
{
  "timestamp": "2026-08-16T10:17:45",
  "statusCode": 409,
  "error": "Conflict",
  "message": "Student with this email abhinav@example.com already exist",
  "path": "/api/students/create"
}
```

---

## 📝 Aage kya seekhna hai (Next Steps)

- [ ] `@ExceptionHandler(Exception.class)` ke case mein `ex.getMessage()` seedha client ko dikha rahe hain — production mein **stack trace/internal details client ko leak** ho sakte hain; wahan generic message ("Something went wrong") dena aur `ex` ko sirf **server logs** mein likhna better practice hai
- [ ] Custom exceptions ke liye ek common **`error code`** field add karna (jaise `STUDENT_NOT_FOUND`) taaki frontend easily switch-case kar sake
- [ ] Zyada bade project mein exceptions ko `enum`-based error codes ke saath aur structured banana

---

## ✅ Quick Revision Summary

> **Exception Handling** = `@RestControllerAdvice` class = **ek control room** jahan poore app ki saari exceptions ka response centrally banta hai
>
> **Custom Exceptions** (`ResourceNotFoundException`, `DuplicateResourceException`) `RuntimeException` extend karti hain → Service layer se `throw new XyzException("message")` karo → Spring automatically usko matching `@ExceptionHandler` tak route kar deta hai
>
> **Specific handler > Generic handler** — jitni specific exception, utna precise HTTP status code (`404`, `409`), warna sab kuch generic `500` ban jaayega
>
> **`orElseThrow(() -> new Xyz(...))`** — `Optional` empty hone pe custom exception throw karne ka one-liner tarika
>
> **`MethodArgumentNotValidException`** — jab `@Valid` DTO ki koi field fail ho, iska `getBindingResult().getFieldErrors()` se saari field-wise error list milti hai
>
> **Naye annotations/concepts:** `@RestControllerAdvice`, `@ExceptionHandler`, `HttpServletRequest`, `Optional.orElseThrow()`, custom `RuntimeException` subclasses