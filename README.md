# Seng302 Team 600 Project


### Background

This project is was produced by a team of seven third year university software students.

An application for use in project courses such as SENG302. It allows you to manage upcoming deadline and events and create a portfolio of skills to present and show your growth throughout the course.

#### Key Learnings

 - Understanding and experience of Agile software development and working in a scrum team.
 - Exposure to gRPC and microservice architecture.
 - Learning about server side rendering and how not to write web applications.
 - Understanding of technical debt as this was my first proejct which spanned over a longer period than 8 weeks.

#### Project reflection
This course was one of my best experiences for my software engineering career, as I learned how project development worked as part of an agile software team. However, unfortunately due to the amount I learnt while working on this project I am not particularly proud of the product itself, as it still contains serious technical debt that came about as a result of the teams lack of experience. In saying that this project is living evidence of how far we came.


----

## Project Readme

### Uses external dependencies:
 - `Bootstrap`
 - `jQuery 3.6.0`
 - `FullCalendar`
 - `Spring Boot`
 - `Thymeleaf`


## Basic Project Structure

- `systemd/` - This folder includes the systemd service files that will be present on the VM, these can be safely ignored.
- `runner/` - These are the bash scripts used by the VM to execute the application.
- `shared/` - Contains (initially) some `.proto` contracts that are used to generate Java classes and stubs that the following modules will import and build on.
- `identityprovider/` - The Identity Provider (IdP) is built with Spring Boot, and uses gRPC to communicate with other modules. The IdP is where we will store user information (such as usernames, passwords, names, ids, etc.).
- `portfolio/` - The Portfolio module is another fully fledged Java application running Spring Boot. It also uses gRPC to communicate with other modules.


## How to run

### 1 - Generating Java dependencies from the `shared` class library
The `shared` class library is a dependency of the two main applications, so before you will be able to build either `portfolio` or `identityprovider`, you must make sure the shared library files are available via the local maven repository.

Assuming we start in the project root, the steps are as follows...

On Linux: 
```
cd shared
./gradlew clean
./gradlew publishToMavenLocal
```

On Windows:
```
cd shared
gradlew clean
gradlew publishToMavenLocal
```

*Note: The `gradle clean` step is usually only necessary if there have been changes since the last publishToMavenLocal.*

### 2 - Identity Provider (IdP) Module
Assuming we are starting in the root directory...

On Linux:
```
cd identityprovider
./gradlew bootRun
```

On Windows:
```
cd identityprovider
gradlew bootRun
```

By default, the IdP will run on local port 9002 (`http://localhost:9002`).

### 3 - Portfolio Module
Now that the IdP is up and running, we will be able to use the Portfolio module (note: it is entirely possible to start it up without the IdP running, you just won't be able to get very far).

From the root directory (and likely in a second terminal tab / window)...
On Linux:
```
cd portfolio
./gradlew bootRun
```

On Windows:
```
cd portfolio
gradlew bootRun
```

By default, the Portfolio will run on local port 9000 (`http://localhost:9000`)

## LICENSE

[GNU GENERAL PUBLIC LICENSE](https://www.gnu.org/licenses/gpl-3.0.en.html)
