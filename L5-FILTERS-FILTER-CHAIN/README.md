# 🔐 Filters & Filter Chain — Spring Boot Revision Notes


## 1️⃣ Problem kya thi? (Why Filters?)

Socho tumhare paas ek `StudentController` hai jisme `createStudent` API hai. Ab tumhe har request pe ye cheezein chahiye:

- Request authenticated hai ya nahi (token check)
- Har request ka log rakhna hai (method, URI)
- Response ka status track karna hai
- Ek unique request-id generate karna hai debugging ke liye

**Bina Filter ke problem kya hoti:**

```java
@PostMapping
public ResponseEntity<String> createStudent(@RequestBody Student student){
    // token check yaha likhna padega
    // logging yaha likhna padega
    // ye same code HAR controller ke HAR method me likhna padega 😩
    studentService.createStudent(student);
    return ResponseEntity.ok("done");
}
```

Ye **repetitive code** ban jaata — har controller me copy-paste. Aur agar ek jagah bug fix karna hai to sab jagah karna padega.

**Solution = Filter**

Filter ek aisi cheez hai jo Controller tak request pahunchne se **PEHLE** aur response client tak jaane se **BAAD** me intercept kar leta hai — matlab beech me ghus jaata hai. Isse cross-cutting concerns (auth, logging, etc.) ek hi jagah likh sakte hain, sabke liye common.

> 💡 **Yaad rakhne wali baat:** Filter = "Security guard / Reception desk" jo Controller (office) tak pahunchne se pehle har request ko check karta hai.

---

## 2️⃣ Filter kya hota hai?

- `jakarta.servlet.Filter` interface ka implementation hota hai
- Spring ka nahi, ye **Servlet-level** concept hai (Spring se pehle bhi tha, Spring ne sirf integrate kiya)
- DispatcherServlet (jo request ko Controller tak route karta hai) tak pahunchne se **pehle** run hota hai

```
Client Request
      |
      v
  [ Filter 1 ]
      |
      v
  [ Filter 2 ]
      |
      v
DispatcherServlet → Controller → Service → Repository
      |
      v
  [ Filter 2 ] (response wapas jaate waqt)
      |
      v
  [ Filter 1 ]
      |
      v
Client Response
```

> 💡 Filter **request** aur **response** dono direction me kaam kar sakta hai — jaane waqt bhi, aane waqt bhi.

---

## 3️⃣ Filter Chain kya hota hai?

Jab ek se zyada Filters hote hain, wo ek **chain (zanjeer)** bana lete hain. Har filter apna kaam karke agle filter ko `chain.doFilter()` call karke pass kar deta hai.

```
Request → AuthenticationFilter → LoggingFilter → Controller
```

Agar koi filter `chain.doFilter()` **call hi nahi karta**, to request wahin ruk jayegi — aage Controller tak pahunchegi hi nahi!

> ⚠️ **Yaad rakhne wali baat:** Filter me `chain.doFilter(request, response)` bhoolna sabse common bug hai. Agar ye call nahi hui, request stuck ho jaayegi (jaise humare `AuthenticationFilter` me token galat hone pe hum `return` kar dete hain bina `chain.doFilter()` call kiye — jaan-bujh kar, kyunki hum aage jaane hi nahi dena chahte).

---

## 4️⃣ Diye gaye code ka breakdown

### a) `AuthenticationFilter` (Order = 1, sabse pehle chalega)

```java
@Component
@Order(1)
public class AuthenticationFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String token = httpRequest.getHeader("token");

        if(token == null || !token.equals("12345")){
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            return; // yaha chain.doFilter() NAHI call hua -> request yahi ruk gayi
        }

        chain.doFilter(servletRequest, servletResponse); // aage bhejo
    }
}
```

**Kya ho raha hai:**
1. `ServletRequest` ko `HttpServletRequest` me **cast** kiya — kyunki humein `getHeader()` jaisa HTTP-specific method chahiye, jo generic `ServletRequest` me nahi hota
2. Header se `token` nikala
3. Agar token galat/missing → `401 Unauthorized` bhej ke turant return (chain aage nahi badhega)
4. Agar sahi hai → `chain.doFilter()` se agle filter (`LoggingFilter`) ko pass

> 💡 **Casting kyu?** `Filter` interface generic hai (HTTP, HTTPS, dusre protocols ke liye bhi kaam aa sakta hai), isliye base type `ServletRequest`/`ServletResponse` deta hai. Web app me hume HTTP-specific methods (`getHeader`, `setStatus`) chahiye hote hain, isliye cast karna padta hai.

### b) `LoggingFilter` (Order = 2, dusra chalega)

```java
@Component
@Order(2)
public class LoggingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        String requestId = UUID.randomUUID().toString();
        httpResponse.setHeader("REQUEST-ID", requestId);

        System.out.println("Incoming Request: " + httpRequest.getMethod() + " " + httpRequest.getRequestURI());

        try {
            chain.doFilter(servletRequest, servletResponse); // Controller tak jaayega
        } finally {
            System.out.println("Response Status : " + httpResponse.getStatus()); // Controller se wapas aane ke baad
        }
    }
}
```

**Kya ho raha hai:**
1. Response me `REQUEST-ID` header set kiya (unique UUID) — debugging/tracing ke liye useful
2. Request aane se pehle method + URI print (log) kiya
3. `try-finally` use kiya taaki chahe Controller me exception aaye ya na aaye, **response status hamesha print ho** (finally block guarantee deta hai)
4. `chain.doFilter()` ke baad wala code = **response wapas aate waqt** chalta hai (isliye status yaha print ho pa raha hai, request se pehle nahi)

> 💡 **Yaad rakhne wali baat:** `chain.doFilter()` ke **pehle ka code** = request phase me chalta hai. `chain.doFilter()` ke **baad ka code** = response phase me chalta hai (jab Controller apna kaam kar chuka hota hai).

### c) `@Order` annotation kyu?

`@Order(1)` aur `@Order(2)` batate hain ki filter chain me kaun **pehle** chalega. Chota number = pehle priority.

```
Order(1) AuthenticationFilter  → pehle chalega (auth check)
Order(2) LoggingFilter         → uske baad chalega (agar auth pass ho gaya)
```

Isiliye pehle Authentication check hota hai, tabhi Logging hoti hai — logical order matters (auth fail hone pe logging bhi skip ho jaati hai, kyunki chain wahin ruk gayi).

---

## 5️⃣ Filter integrate karne ke steps (from scratch)

Agar naya filter banana ho, to ye steps follow karo:

| Step | Kya karna hai |
|------|----------------|
| 1 | `Filter` interface (`jakarta.servlet.Filter`) implement karo |
| 2 | `doFilter(ServletRequest, ServletResponse, FilterChain)` method override karo |
| 3 | Request/Response ko `HttpServletRequest`/`HttpServletResponse` me cast karo (agar HTTP-specific methods chahiye) |
| 4 | Apna logic likho (auth check, logging, header set, etc.) |
| 5 | Kaam ke end me `chain.doFilter(servletRequest, servletResponse)` call karna **mat bhoolo** (warna request block ho jaayegi) |
| 6 | Class pe `@Component` lagao — taaki Spring isse bean bana ke register kare |
| 7 | `@Order(n)` lagao — taaki multiple filters ka execution sequence fix ho |

```java
@Component
@Order(3)
public class MyNewFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        // request phase logic yaha

        chain.doFilter(req, res); // aage bhejna zaruri hai

        // response phase logic yaha (optional)
    }
}
```

**Bas itna hi!** `@Component` + `Filter` interface implement karte hi Spring Boot **automatically** isse Filter Chain me register kar deta hai — koi extra config file nahi chahiye (jab tak specific URL pattern ya conditional registration na chahiye ho).

> 💡 Agar sirf kuch specific URLs pe filter chalana ho (sab pe nahi), to `FilterRegistrationBean` use karke manually configure karna padta hai `addUrlPatterns()` ke saath. Simple case me `@Component` + `@Order` hi kaafi hai.

---

## 6️⃣ Quick Revision Table

| Concept | Matlab |
|---|---|
| Filter | Controller tak pahunchne se pehle/baad me request-response intercept karne wala component |
| Filter Chain | Multiple filters ka sequence jisme har ek agle ko `chain.doFilter()` se pass karta hai |
| `@Order(n)` | Chain me filter ka execution sequence decide karta hai (chota number = pehle) |
| `chain.doFilter()` na call karna | Request ko wahi rok dega — Controller tak nahi jaayegi |
| Code before `chain.doFilter()` | Request phase me chalta hai |
| Code after `chain.doFilter()` | Response phase me chalta hai (Controller ke baad) |
| Casting to Http types | HTTP-specific methods (`getHeader`, `setStatus`) use karne ke liye zaruri |
| `@Component` | Spring ko batata hai isse bean bana ke Filter Chain me daalo |

---

## 7️⃣ Is example ka poora flow (real request pe)

```
POST /api/students  (Header: token=12345)
        |
        v
AuthenticationFilter → token check pass → chain.doFilter()
        |
        v
LoggingFilter → REQUEST-ID header set, log print → chain.doFilter()
        |
        v
StudentController.createStudent() → StudentService.createStudent() → data print
        |
        v
(response wapas aata hai)
        |
        v
LoggingFilter → finally block → Response Status print
        |
        v
AuthenticationFilter (koi after-code nahi, seedha return ho gaya)
        |
        v
Client ko response mile: "done" + REQUEST-ID header
```

Agar token galat ho:

```
POST /api/students  (Header: token=wrong / missing)
        |
        v
AuthenticationFilter → token check FAIL → 401 set → return (chain.doFilter() call hi nahi hua)
        |
        v
Client ko seedha 401 Unauthorized mil jaata hai
(LoggingFilter, Controller — kuch bhi nahi chalta)
```