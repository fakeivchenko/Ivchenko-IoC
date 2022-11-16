# Ivchenko-IoC
Simple dependency injection framework

---

### Installing
Run following commands:
```sh
git clone https://github.com/fakeivchenko/Ivchenko-IoC.git
cd Ivchenko-IoC
mvn clean install
```
Add dependency to your project:

#### Maven:
```xml
<dependency>
    <groupId>com.ivchenko.ioc</groupId>
    <artifactId>ioc</artifactId>
    <version>1.0</version>
</dependency>
```
#### Gradle:
Add maven local repository:
```gradle
repositories {
    mavenLocal()
}
```
Add dependency:
```gradle
implementation("com.ivchenko.ioc:ioc:1.0")
```
---
### Basic usage
<b>NOTE:</b> Tutorial below uses <a href="https://projectlombok.org/">Lombok</a> library.
#### Simple Application
First, create some model data class. For example, User:
```java
@Data @Builder
public class User {
    private Long id;
    private String firstName;
    private String lastName;
    private LocalDate dob;
}
```

Create a service interface and its implementation.
Annotate implementation with @Component.

Service interface:
```java
public interface UserService {
    List<User> getAllUsers();

    Optional<User> getUserById(Long id);

    void addUser(User user);

    void removeUser(User user);
}
```
Service implementation:
```java
@Component
public class UserServiceImpl implements UserService {
    private final List<User> users;

    public UserServiceImpl() {
        users = new ArrayList<>();
        {{
            // Add some users
        }}
    }

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public void addUser(User user) {
        // User presence check can be implemented
        users.add(user);
    }

    @Override
    public void removeUser(User user) {
        boolean userPresent = users.stream().anyMatch(u -> u.equals(user));
        if (userPresent) {
            users.remove(user);
        } else throw new IllegalStateException("User " + user + " does not exists");
    }
}
```
Then create component class in which UserService will be injected.
Annotate class with @Component and its constructor with @Autowired:

<b>NOTE:</b> If class annotated with @Component does not have
constructor annotated with @Autowired, constructor with no arguments will be used,
otherwise first found constructor will be used.
```java
@Component
public class UserRegistryApplication {
    private UserService userService;

    @Autowired
    public UserRegistryApplication(UserService userService) {
        this.userService = userService;
    }
}
```
Add to this class method which using dependency.
Annotate its method with @PostConstructor.

Method annotated with @PostConstructor will be executed after instance creation.
```java
@PostConstructor
public void displayAllUsers() {
    System.out.println("Original user list: ");
    userService.getAllUsers().forEach(System.out::println);
    System.out.println("--------------------");

    userService.addUser(
            User.builder()
                    .id(3L)
                    .firstName("Paul")
                    .lastName("Strong")
                    .dob(LocalDate.of(2006, 8, 20))
                    .build()
    );

    System.out.println("User list with added user: ");
    userService.getAllUsers().forEach(System.out::println);
    System.out.println("--------------------");


    Optional<User> userById = userService.getUserById(3L);
    userById.ifPresentOrElse(user -> userService.removeUser(user), () -> {
        throw new IllegalStateException("User cannot be found");
    });

    System.out.println("User list with removed user: ");
    userService.getAllUsers().forEach(System.out::println);
    System.out.println("--------------------");
}
```
Write main method,
in which execute Injector.startApplication(Class<?> mainClass):
```java
public static void main(String[] args) {
    Injector.startApplication(UserRegistryApplication.class);
}
```
Run application and you will see something like this:
```bash
Original user list: 
User(id=1, firstName=Anton, lastName=Ivchenko, dob=2002-06-08)
User(id=2, firstName=Tracy, lastName=Penn, dob=1990-02-10)
--------------------
User list with added user: 
User(id=1, firstName=Anton, lastName=Ivchenko, dob=2002-06-08)
User(id=2, firstName=Tracy, lastName=Penn, dob=1990-02-10)
User(id=3, firstName=Paul, lastName=Strong, dob=2006-08-20)
--------------------
User list with removed user: 
User(id=1, firstName=Anton, lastName=Ivchenko, dob=2002-06-08)
User(id=2, firstName=Tracy, lastName=Penn, dob=1990-02-10)
--------------------

Process finished with exit code 0
```
#### Qualifier
If you have multiple implementations of one interface you can
tell injector which one to use with Qualifier annotation.

Simply annotate constructor parameter with this annotation and pass
Simple Name of implementation class as annotation parameter:
```java
@Autowired
public UserRegistryApplication(@Qualifier("UserServiceImpl2") UserService userService) {
    this.userService = userService;
}
```
Now, instance of UserServiceImpl2 will be injected
instead of default first found.

<i>Completed demo of this application can be viewed <a href="https://github.com/fakeivchenko/Ivchenko-IoC/tree/master/demos/UserRegistryDemo">here</a>.</i>

---
### Circular dependencies
Simplest circular dependency occurs when A depends on B, and B depends on A:

A → B → A

This framework can handle only <b>constructor dependency</b>,
so circular dependencies not supported.

If circular dependency occurs framework will detect it and throw an exception:
```
Exception in thread "main" java.lang.IllegalStateException: Requested bean is already in creation: Maybe there an unresolvable circular reference
```
