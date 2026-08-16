# 📘 Spring Boot - Profiling (Dev / Staging / Prod) - Learning Notes

---

## 🤔 Ye project mein ho kya raha hai? (Problem samjho pehle)

Real-world mein ek application **ek hi codebase** ke saath alag-alag jagah chalti hai:
- **Local/Dev** — apne laptop pe development karte waqt
- **Staging** — testing/QA team ke liye, production jaisa environment
- **Production** — real users jahan app use kar rahe hain

**Har environment ki zarurat alag hoti hai**, jaise:
- Dev mein `port 6060` pe chalao, Prod mein `9090` pe
- Dev mein test/dummy notification bheji jaaye, Prod mein real notification service (jaise actual SMS/Email API) call ho
- Har environment ka apna welcome message ya config value ho, taaki turant pata chal jaaye **kaunsa environment** chal raha hai

**Naive/galat solution kya hota:** Har baar code mein `if(environment == "dev") {...} else if (environment == "prod") {...}` jaisi conditions likhna — ye **messy, error-prone, aur repetitive** hai. Aur agar galti se dev wali config production mein deploy ho gayi, to bada disaster ho sakta hai.

**Spring Boot ka solution — "Profiles":** Har environment ke liye:
1. Alag **config file** (`application-{profile}.properties`) — taaki values (port, message, etc.) alag ho
2. Alag **Bean implementation** (`@Profile("dev")`, `@Profile("prod")`) — taaki behavior (jaise notification kaise bheji jaaye) bhi alag ho, **bina if-else likhe**

> 💡 **Yaad rakhna:** Profiling ka poora point ye hai — **"same code, alag environment mein alag config aur alag behavior — bina code change kiye, sirf ek switch (active profile) badal ke."**

---

## 🧭 Is project ka overall flow

```
Application start hoti hai
        │
        ▼
Spring dekhta hai "active profile" kya set hai (dev / staging / prod)
        │
        ├──▶ Us profile ke matching application-{profile}.properties load hoti hai
        │        (port, welcome message waghera us profile ki values le lega)
        │
        └──▶ Us profile ke matching @Profile("...") wale Beans hi create hote hain
                 (jaise sirf "dev" wali NotificationService activate hogi, baaki teen nahi banengi)
        │
        ▼
Controller ko pata hi nahi chalta ki kaunsi implementation use ho rahi hai —
usko bas interface (NotificationService) mila hai, Spring ne sahi wali choose kar di
```

---

## 🧩 Step 1: `application.properties` (base) + har environment ki alag file

Spring Boot ka naming convention hai: **`application-{profileName}.properties`**

```
src/main/resources/
├── application.properties           ← base/default config (sabke liye common)
├── application-dev.properties        ← sirf "dev" profile active hone pe load hogi
├── application-staging.properties    ← sirf "staging" profile active hone pe load hogi
└── application-prod.properties       ← sirf "prod" profile active hone pe load hogi
```

**`application.properties`** (base — jab koi profile explicitly na diya ho, ya common values):
```properties
spring.application.name=profiling
server.port=8080
app.welcome.message=LOCAL:your application is running in local
```

**`application-dev.properties`**:
```properties
server.port=6060
app.welcome.message=DEV:your application is running in developer
```

**`application-prod.properties`**:
```properties
server.port=9090
app.welcome.message=PROD:your application is running in production
```

**`application-staging.properties`**:
```properties
server.port=8181
app.welcome.message=STAGE:your application is running in staging
```

**Yaad rakhne wali baat:** Jab ek profile active hoti hai (jaise `dev`), to Spring **dono files** merge karta hai — `application.properties` (base) + `application-dev.properties` (profile-specific). Agar same property dono jagah ho (jaise `server.port`), to **profile-specific file jeetti hai** (override kar deti hai base ko).

| File | Kab load hoti hai | `server.port` | `app.welcome.message` |
|---|---|---|---|
| `application.properties` | Hamesha (base) | `8080` | `LOCAL: ...` |
| `application-dev.properties` | `dev` active ho tab | `6060` | `DEV: ...` |
| `application-staging.properties` | `staging` active ho tab | `8181` | `STAGE: ...` |
| `application-prod.properties` | `prod` active ho tab | `9090` | `PROD: ...` |

---

## 🧩 Step 2: `@Value` se config value ko Java class mein inject kiya

```java
@RestController
@RequestMapping("api/demo")
public class Controller {

    @Value("${app.welcome.message}")
    private String message;

    @GetMapping("/greet")
    public ResponseEntity<String> sendGreet(){
        return ResponseEntity.ok(message);
    }
}
```

**`@Value("${app.welcome.message}")`** — Spring ko batata hai: "properties file mein `app.welcome.message` key ki value uthao aur is `message` variable mein daal do." Ye value **active profile ke hisaab se badal jaati hai** — bina code chhue.

**Yaad rakhne wali baat:** `${...}` syntax **placeholder** hai — Spring startup ke time isko actual value se replace kar deta hai. Isliye ise "**externalized configuration**" kehte hain — value code mein hardcode nahi hai, bahar (properties file mein) rakhi hai.

---

## 🧩 Step 3: Ek common `interface` banaya — behavior ka contract

```java
// Hum chahte hai ki developing, staging, aur production environment mein
// alag alag notification use ho for testing.
public interface NotificationService {
    String send();
}
```

Ye interface sirf ek **contract** define karta hai — "jo bhi is interface ko implement karega, uske paas `send()` method hoga." Iske andar koi logic nahi hai.

> 📝 Ye dhyaan do: **Controller sirf isi interface ko jaanega**, kisi specific class (`Dev`, `Prod`, `Staging`) ko nahi. Ye hi **loose coupling** ka fayda hai — Controller ko fikar hi nahi ki actual mein kaunsi class kaam kar rahi hai.

---

## 🧩 Step 4: Teen alag implementations banayi — har ek apne profile ke saath

### `NotificationforDevelopingUse` — sirf `dev` profile mein active

```java
// ye notification keval developing phase mein chalegi
@Service
@Profile("dev")
public class NotificationforDevelopingUse implements NotificationService {
    @Override
    public String send(){
        return "here is your Notification test in Developing Environment";
    }
}
```

### `NotificationforStagingUse` — sirf `staging` profile mein active

```java
// ye notification keval staging phase mein chalegi
@Service
@Profile("staging")   // ye annotation spring ko batati hai ki staging environment mein is class ka notification chalega
public class NotificationforStagingUse implements NotificationService {
    @Override
    public String send(){
        return "here is your Notification test in Staging Environment";
    }
}
```

### `NotificationforProductionUse` — sirf `prod` profile mein active

```java
// ye notification service keval production mein chalegi
@Service
@Profile("prod")
public class NotificationforProductionUse implements NotificationService {
    @Override
    public String send(){
        return "here is your Notification test in Production Environment";
    }
}
```

**`@Profile("dev")` / `@Profile("staging")` / `@Profile("prod")`** — Spring ko batata hai: "**is class ka Bean tabhi banao** jab wo diya gaya profile active ho." Matlab agar `dev` profile active hai, to sirf `NotificationforDevelopingUse` ka object (Bean) banega — baaki do classes ke Bean **banenge hi nahi**, Spring container mein exist hi nahi karenge.

> 💡 **Ye Strategy Design Pattern jaisa hai** — ek interface (`NotificationService`), multiple implementations, aur runtime pe (yahan "runtime" matlab app-start ke time, active profile ke basis pe) sahi implementation choose ho jaati hai — **bina koi if-else likhe**.

---

## 🧩 Step 5: Controller — sirf interface use karta hai, implementation ki fikar nahi

```java
@RestController
@RequestMapping("api/notification")
public class NotificationController {

    private NotificationService notificationService;

    public NotificationController(NotificationService notificationService){
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<String> sendNotification(){
        String notification = notificationService.send();
        return ResponseEntity.ok(notification);
    }
}
```

Yahan **Constructor Injection** ho rahi hai — Spring khud decide karta hai ki `NotificationService` type ka **kaunsa Bean** yahan inject karna hai, based on **kaunsa profile active hai**. Controller ko pata hi nahi ki uske haath mein Dev wali, Staging wali, ya Prod wali implementation aayi.

**Yaad rakhne wali baat:** Agar `dev` profile active hai, `notificationService.send()` call karne pe **automatically** `NotificationforDevelopingUse.send()` hi chalega — Controller mein iske liye koi check nahi likha.

---

## 🧩 Step 6: Profile ko **activate** kaise karein (asli implementation ka last step)

Sirf classes aur properties files bana dene se kaam nahi chalega — Spring ko batana padega ki **konsa profile abhi "active"** hai. Ye kaise set karte hain, iske multiple tarike hain:

| Tarika | Kaise |
|---|---|
| **`application.properties` mein** | `spring.profiles.active=dev` line daal do (base file mein) |
| **Command line se (jar run karte waqt)** | `java -jar app.jar --spring.profiles.active=prod` |
| **Maven se run karte waqt** | `mvn spring-boot:run -Dspring-boot.run.profiles=staging` |
| **Environment variable se** | `SPRING_PROFILES_ACTIVE=prod` (Docker/deployment mein ye sabse common tarika hai) |
| **IntelliJ/IDE Run Configuration mein** | "Active profiles" field mein `dev` likh do |

```properties
# application.properties mein — sabse simple tarika (local testing ke liye)
spring.profiles.active=dev
```

> ⚠️ **Real-world mein IMPORTANT:** `application.properties` ke andar `spring.profiles.active` **hardcode nahi karte production ke liye** — kyunki tab har environment ke liye code/jar alag banana padega. Isliye actual deployment mein ye value **environment variable** (`SPRING_PROFILES_ACTIVE`) ya **command-line argument** se di jaati hai, taaki **same JAR file** har environment mein chal sake, bas activate command alag ho.

---

## 🧪 Testing kaise karein — end-to-end

1. `application.properties` mein (ya command line se) `spring.profiles.active=dev` set karo
2. App run karo — dekhoge app **port 6060** pe start hogi (base ka `8080` override ho gaya)
3. `GET /api/demo/greet` call karo → response milega: `"DEV:your application is running in developer"`
4. `POST /api/notification` call karo → response milega: `"here is your Notification test in Developing Environment"`
5. Ab `spring.profiles.active=prod` kar do, app restart karo — same endpoints ab **port 9090** pe milenge, aur responses automatically **Production wale** aa jaayenge — **code mein ek line bhi nahi badli**

---

## 📄 Bonus: Isko `.yml` mein kaise likhte (YAML equivalent)

Tumne `.properties` use kiya hai, lekin `.yml` (YAML) bhi same kaam karta hai — bas **format** alag hota hai (indentation-based, nested structure). Real projects mein `.yml` zyada popular hai kyunki **nested/related configs ek saath group** ho jaate hain aur readable lagte hain.

**`.properties` (flat, dot-separated):**
```properties
server.port=6060
app.welcome.message=DEV:your application is running in developer
```

**Same cheez `.yml` mein (nested):**
```yaml
server:
  port: 6060

app:
  welcome:
    message: "DEV:your application is running in developer"
```

**Sabse bada fayda `.yml` ka — ek hi file mein saare profiles!** `application.yml` ke andar `---` (three dashes) se profile-wise sections separate kar sakte hain — alag-alag files banane ki zarurat hi nahi:

```yaml
# base/common config
spring:
  application:
    name: profiling
server:
  port: 8080
app:
  welcome:
    message: "LOCAL:your application is running in local"

---
spring:
  config:
    activate:
      on-profile: dev
server:
  port: 6060
app:
  welcome:
    message: "DEV:your application is running in developer"

---
spring:
  config:
    activate:
      on-profile: staging
server:
  port: 8181
app:
  welcome:
    message: "STAGE:your application is running in staging"

---
spring:
  config:
    activate:
      on-profile: prod
server:
  port: 9090
app:
  welcome:
    message: "PROD:your application is running in production"
```

**Yaad rakhne wali baat:**
- `.properties` mein har profile ke liye **alag file** banti hai (`application-dev.properties`)
- `.yml` mein **ek hi file** mein `---` se sections divide karke sab profiles rakh sakte ho (`spring.config.activate.on-profile: dev`)
- Dono ka kaam same hai — bas `.yml` thoda zyada **organized/compact** dikhta hai jab profiles zyada hon
- `@Value("${app.welcome.message}")` **dono mein exactly same tarike se kaam karega** — Java code ko farak nahi padta config `.properties` mein hai ya `.yml` mein

---

## ⚠️ Gotchas (ye cheezein bhool mat jaana revise karte waqt)

1. **Koi profile active na ho to kya hoga?**
   Agar `spring.profiles.active` set hi nahi kiya, to Spring **`default` profile** use karta hai — us case mein sirf `application.properties` (base) ki values load hongi, aur `@Profile("dev")`/`@Profile("staging")`/`@Profile("prod")` teenon mein se **koi bhi Bean nahi banega**. Agar `NotificationService` ka koi Bean hi nahi mila, to app **start hote hi crash ho jaayegi** (`NoSuchBeanDefinitionException`) — kyunki Controller ko constructor mein `NotificationService` chahiye hi.

2. **Do profiles ek saath active ho sakte hain?**
   Haan — `spring.profiles.active=dev,staging` jaisa comma-separated bhi likh sakte ho. Lekin isse **do `@Service` Beans** ek saath ban jaayenge same `NotificationService` type ke — jab Controller inject karega, to Spring confuse ho jaayega **kaunsa use karu** (`NoUniqueBeanDefinitionException`), jab tak `@Primary` jaisa kuch na lagao. Normally **ek waqt mein ek hi profile** active rakhte hain.

3. **File naming exact match honi chahiye:** `application-dev.properties` mein `-dev` wahi naam hona chahiye jo `@Profile("dev")` aur `spring.profiles.active=dev` mein likha hai — case-sensitive match hota hai.

---

## 📝 Aage kya seekhna hai (Next Steps)

- [ ] `.properties` se `.yml` mein poora project migrate karke try karna (upar wala bonus section practically implement karna)
- [ ] `@Profile` ko sirf class pe nahi, **individual `@Bean` methods** pe bhi laga ke dekhna (Configuration class ke andar)
- [ ] `@ActiveProfiles("dev")` use karke **JUnit tests** likhna — taaki test ke time bhi specific profile force ho
- [ ] Profile **groups** ke baare mein padhna (`spring.profiles.group.production=prod,logging,monitoring` jaisa — multiple profiles ek naam ke peeche combine karna)
- [ ] `@Primary` annotation seekhna — jab multiple Beans ho to default kaunsa use ho, ye specify karne ke liye

---

## ✅ Quick Revision Summary

> **Profiling ka core idea:** Same code, alag environment (dev/staging/prod) mein alag **config** (port, messages) aur alag **behavior** (Bean implementation) — bina if-else likhe, sirf "active profile" switch karke.
>
> **Config alag karne ka tarika:** `application-{profile}.properties` (ya `.yml` mein `---` se divided sections) — active profile ke hisaab se base `application.properties` ko override karti hai.
>
> **Behavior alag karne ka tarika:** Common `interface` (`NotificationService`) + multiple `@Service` implementations, har ek pe `@Profile("dev"/"staging"/"prod")` — sirf active profile wali class ka Bean banta hai.
>
> **Activate kaise karein:** `spring.profiles.active=dev` (properties mein), ya command-line/env-variable se (production mein yahi preferred hai, taaki same JAR har jagah chale).
>
> **`@Value("${key}")`** — properties/yml file ki value ko seedha Java field mein inject karta hai — "externalized config."
>
> **Controller/consumer class** sirf **interface** pe depend karta hai, kabhi specific implementation pe nahi — isi wajah se Spring runtime pe sahi wali "plug-in" kar paata hai (Dependency Injection ka real fayda yahi hai).
>
> **Naye annotations/concepts:** `@Profile`, `@Value`, `application-{profile}.properties`, `spring.profiles.active`, YAML `---` multi-document profiles, `spring.config.activate.on-profile`