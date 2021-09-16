# bookstore-backend

Bookstore is an e-commerce application with possibility to buy, comment and rate books. Additionaly, applications includes options available only for logged users e.g. users's panel offering options of user's personal data editiion, resetting password and reviewing user's order summaries.
Part of application's options is available only for users with role of Moderator or Admin. These functionalities include managament of users accounts manegement (CRUD), books list management (CRUD + books activation/book search)
 
You can use REST or dedicated front-end application: [GitHub](https://github.com/Radek87o/bookstore-frontend) - deployed to https://bookstore-rornat.herokuapp.com/

## Tech/frameworks used ##

<img src="https://spring.io/images/spring-logo-9146a4d3298760c2e7e49595184e1975.svg" width="200"><img src="https://hibernate.org/images/hibernate-logo.svg" width="200"><img src="https://upload.wikimedia.org/wikipedia/commons/thumb/2/29/Postgresql_elephant.svg/1200px-Postgresql_elephant.svg.png" width="100"><img src="https://girishkumarshakya.files.wordpress.com/2015/10/java-mail-send-01.jpg" width="150"><img src="https://ubiq.co/tech-blog/wp-content/uploads/2020/07/increase-request-timeout-apache.png" width="200"><img src="https://miro.medium.com/max/1400/1*WjmDb7LvLRBbWsp6x7Gakw.jpeg" width="150"><img src="https://raw.githubusercontent.com/thymeleaf/thymeleaf-org/main/artwork/thymeleaf%202016/thymeleaf_logo_white.png" width="200"><img src="https://junit.org/junit4/images/junit5-banner.png" width="200"><img src="https://raw.githubusercontent.com/mockito/mockito/main/src/javadoc/org/mockito/logo.png" width="200"><img src="https://upload.wikimedia.org/wikipedia/commons/thumb/5/52/Apache_Maven_logo.svg/2560px-Apache_Maven_logo.svg.png" width="200"><img src="https://www.javanibble.com/assets/images/feature-images/feature-image-lombok.png" width="100">

## Instalation ##

* JDK 11
* Apache Maven 3.x

## API & Security ##

Application is available on localhost:8080. In order to test - use Postman or another http client. 

Access to majority of endpoints is limited only to authenticated users (JWT Token).

In order to use app locally

1. Register new user:
  ```
  Endpoint: POST http://localhost:8080/api/users/signup
  Body (accept: JSON) :
  {
    "email":"<Your email>",
    "password":"<Your password*>",
    "firstName":"<First Name>",
    "lastName":"<Last name>",
    "username":"<Your username - is optional>"
  }
  *Password must contain at least 8 characters including at least 1 uppercase letter, 1 lowercase letter, 1 special character, and 1 digit 
  
  Expected response with status 201
  ```
2. Activate Your account:
  ```
  Endpoint: GET http://localhost:8080/api/users/activate?userId={{YOUR_USER_ID*}}
  
  *YOUR_USER_ID - use value of field "userId" returned when signup method was correctly proceeded
  
  Expected response with status 200
  ```
3. Login to Your account:
  ```
  Endpoint: POST http://localhost:8080/api/users/signin
  Body (accept JSON) :
  {
     "username": "<Your email or username>",
     "password": "<Your password>"
  }
   
  Expected response with status 200
   
  Please find and copy from response headers value of 'Jwt-Token'
  ```
4. Calling endpoints secured with Jwt-Token
  ```
  Please add to the request headers of the method a key "Authorization" with value: "Bearer [YOUR_JWT_TOKEN]"
  ```

## For an end User ##

App is deployed to https://bookstore-rornat-dev.herokuapp.com/ - which is exposed to frontend app: https://bookstore-rornat.herokuapp.com/
