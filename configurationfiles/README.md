# Spring Boot Configuration — `application.properties`, `@Value`, `@ConfigurationProperties`

Is lesson mein humne seekha ki Spring Boot application ke **external configuration values** ko kaise manage karte hain — bina hardcoding ke, aur bina rebuild kiye.

---

## 1. `application.properties` File

Yeh Spring Boot ki **default configuration file** hoti hai, jo `src/main/resources/` folder mein hoti hai.

Isme hum apni application ke saare configurable values define karte hain — jaise server port, database credentials, app-specific custom values, etc.

```properties
# Server config
server.port=8081

# Custom app values
app.name=TicketLi
app.version=1.0.0
app.support-email=support@ticketli.com

# Database config
spring.datasource.url=jdbc:mysql://localhost:3306/ticketli
spring.datasource.username=root
spring.datasource.password=root123
```

**Kyu use karte hain?**
- Code aur configuration ko **alag** rakhte hain (separation of concerns).
- Environment badalne par (dev/test/prod) sirf properties file change karni padti hai, code nahi.
- Sensitive values (jaise DB password) ko easily externalize kar sakte hain.

> `application.yml` bhi same kaam karta hai, bas YAML format mein — dono mein se koi ek use hota hai.

---

## 2. `@Value` Annotation

`@Value` ek **field-level annotation** hai jo `application.properties` se ek **single value** ko directly Java field mein inject karta hai.

```java
@Component
public class AppInfo {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version}")
    private String appVersion;

    @Value("${app.support-email:default@example.com}")  // default value bhi de sakte hain
    private String supportEmail;

    public void printInfo() {
        System.out.println(appName + " v" + appVersion + " | " + supportEmail);
    }
}
```

**Key Points:**
- Syntax: `@Value("${property.key}")`
- Agar property file mein key nahi mili aur default value bhi nahi di, to app **startup par fail** ho jaayegi.
- Default value dene ka syntax: `@Value("${key:defaultValue}")`
- Achha hai jab **1-2 values** chahiye ho, kisi bhi class mein.

**Limitation:**
- Bahut saari related properties (jaise 5-6 DB configs) ke liye `@Value` baar baar likhna padta hai — code messy ho jaata hai.
- Type-safety kam hoti hai, aur properties ko group karke manage karna mushkil hota hai.

---

## 3. `@ConfigurationProperties` Annotation

Jab humare paas **multiple related properties** ho (ek hi prefix ke andar), to unhe ek POJO class mein **group** karke bind karne ke liye `@ConfigurationProperties` use karte hain.

### Step 1: Properties define karo (common prefix ke saath)

```properties
app.name=TicketLi
app.version=1.0.0
app.support-email=support@ticketli.com
```

### Step 2: A POJO class banao

```java
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String version;
    private String supportEmail;

    // Getters and Setters (required for binding!)

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getSupportEmail() { return supportEmail; }
    public void setSupportEmail(String supportEmail) { this.supportEmail = supportEmail; }
}
```

### Step 3: Use kar lo kahin bhi

```java
@RestController
public class InfoController {

    private final AppProperties appProperties;

    public InfoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping("/info")
    public String getInfo() {
        return appProperties.getName() + " - " + appProperties.getVersion();
    }
}
```

**Key Points:**
- `prefix = "app"` bolta hai Spring ko ki `app.` se start hone wali saari properties is class ke fields mein map ho jaayein (naming: `app.support-email` → `supportEmail` field, kebab-case se camelCase auto-convert hota hai).
- Class ko `@Component` (ya `@ConfigurationProperties` ke saath `@EnableConfigurationProperties`) se Spring container mein register karna padta hai.
- **Type-safe** — agar property `int` honi chahiye but string di, to Spring startup par error dega.
- Nested objects bhi support karta hai (jaise `app.mail.host`, `app.mail.port` → ek `Mail` nested class).

---

## 4. `@Value` vs `@ConfigurationProperties` — Kab Kya Use Karein?

| Feature | `@Value` | `@ConfigurationProperties` |
|---|---|---|
| Use case | Ek-do standalone values | Multiple related grouped values |
| Type safety | Kam | Zyada (compile-time checked via POJO) |
| SpEL support | Haan | Nahi |
| Relaxed binding (kebab-case, camelCase) | Nahi | Haan |
| Validation (`@Validated`) | Nahi | Haan |
| Nested properties | Manual | Automatic (nested POJOs) |
| Readability (bahut saari properties ke liye) | Kam | Zyada |

**Rule of thumb:**
- 1-2 simple values chahiye → `@Value`
- Ek module/feature ki saari related settings ek jagah chahiye (jaise mail config, JWT config, rail-orange theme config) → `@ConfigurationProperties`

---

## 5. `ApplicationRunner` aur `CommandLineRunner`

Ye dono **functional interfaces** hain jo Spring Boot application **fully start hone ke turant baad** kuch custom code run karne ke liye use hote hain — jaise startup par database seed karna, cache warm-up karna, ya kisi initial check ko run karna.

Dono ka kaam same hai, bas method ka **input parameter type** alag hota hai.

### a) `CommandLineRunner`

```java
@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        System.out.println("App started! Raw args: " + Arrays.toString(args));
    }
}
```

- `run(String... args)` — command line arguments ko **raw String array** ke form mein deta hai.
- Jaise agar app run kiya `java -jar app.jar --server.port=9090 hello`, to yahan `args = ["--server.port=9090", "hello"]` (bina parse kiye, raw).

### b) `ApplicationRunner`

```java
@Component
public class MyAppRunner implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("App started!");
        System.out.println("Option names: " + args.getOptionNames());
        System.out.println("Source args: " + Arrays.toString(args.getSourceArgs()));
    }
}
```

- `run(ApplicationArguments args)` — arguments ko **parsed form** mein deta hai (`ApplicationArguments` object).
- Isse hum `--key=value` type args ko easily option name/value ke roop mein nikaal sakte hain:
  ```java
  args.getOptionValues("server.port"); // ["9090"]
  args.containsOption("debug");        // true/false
  ```

### Key Points

- Dono interfaces ka `run()` method Spring `ApplicationContext` fully load hone ke **baad** automatically call hota hai.
- Class ko `@Component` se register karna zaroori hai taaki Spring isse pick kare.
- Ek application mein **multiple runners** ho sakte hain — unka execution order control karne ke liye `@Order(n)` annotation use karte hain:
  ```java
  @Component
  @Order(1)
  public class FirstRunner implements CommandLineRunner { ... }

  @Component
  @Order(2)
  public class SecondRunner implements ApplicationRunner { ... }
  ```

### `CommandLineRunner` vs `ApplicationRunner`

| Feature | `CommandLineRunner` | `ApplicationRunner` |
|---|---|---|
| Method | `run(String... args)` | `run(ApplicationArguments args)` |
| Args format | Raw String array | Parsed (`--key=value` easily readable) |
| Use case | Simple/raw args ke liye | Structured option-based args ke liye |
| Common use | Startup logging, quick tasks | Argument-driven startup logic |

**Rule of thumb:** Agar sirf startup par kuch chalana hai bina args ki zaroorat ke, dono me se koi bhi chalega. Agar command-line se `--key=value` type structured options padhne hain, to `ApplicationRunner` zyada convenient hai.

---

## 6. Quick Recap

- `application.properties` → saari external configuration values yahin define hoti hain.
- `@Value("${key}")` → single property ko directly field mein inject karta hai.
- `@ConfigurationProperties(prefix = "...")` → related properties ko ek type-safe POJO class mein group karke bind karta hai.
- Dono ka combination bhi use ho sakta hai — chhoti values ke liye `@Value`, aur bade config blocks ke liye `@ConfigurationProperties`.
- `CommandLineRunner` / `ApplicationRunner` → application fully start hone ke baad ek baar custom logic run karne ke liye (raw args vs parsed args), multiple runners ka order `@Order` se control hota hai.
