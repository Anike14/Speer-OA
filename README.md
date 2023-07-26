Technologies Used:
    Java
    Spring Boot
    Spring Security
    Spring Data JPA (Hibernate)
    PostgreSQL
    Maven
    JUnit 5
    Mockito
    Postman (for API testing)

Setup
    1. Clone the repository to your local machine.
    2. Ensure you have Java and Maven installed on your system.
    3. Set up a PostgreSQL database and update the application.properties file with the database credentials and connection details.

Run the application
    1. IDEA: (Recommended)
       run the application from your IDE by running the main class OaApplication.
    2. Maven Command: (Not verified)
       run the command: mvn spring-boot:run
    The application will be accessible at http://localhost:8080.

Endpoints

    User Endpoints
    POST /auth/signup: Sign up a new user by providing a JSON payload with username and password fields.
    POST /auth/login: Log in a user with existing credentials. Provide a JSON payload with username and password fields.

    Note Endpoints
    POST /notes: Create a new note. Provide a JSON payload with title and content fields.
    GET /notes: Retrieve all notes owned by the authenticated user.
    GET /notes/{noteId}: Retrieve a specific note by its ID.
    PUT /notes/{noteId}: Update a specific note by its ID. Provide a JSON payload with title and content fields.
    DELETE /notes/{noteId}: Delete a specific note by its ID.
    POST /notes/{noteId}/share: Share a specific note with another user. Provide a JSON payload with username field representing the target user.
    GET /notes/search?q=query: Search for notes containing the specified query in their content.

Testing
    The application includes both unit tests and integration tests to ensure its functionality and behavior are thoroughly tested. 
    However, due to time restriction, I cannot finished the integration test by 48 hours since I receive this test.
    The unit tests focus on testing individual components in isolation, while the integration tests cover interactions between various components.

To run the tests, use the following command:
    1. IDE Test: (Recommended)
       You can also run the tests directly from your IDE. Most modern IDEs have built-in support for running JUnit tests. 
       In IntelliJ IDEA, you can right-click on the test class or test method and select "Run" to execute the tests.
    2. Maven Command: (Not verified)
       run mvn test

Security
    The application uses Spring Security for user authentication and authorization. 
    User passwords are securely hashed before storage in the database. Access to certain endpoints is restricted based on user roles, ensuring that only authorized users can perform specific actions.
    Also, a rate limit feature with the Token Bucket strategy is added to the application to throttling the malicious requests.

Data Persistence
    Data persistence is handled using Spring Data JPA (Hibernate), with the application interacting with a PostgreSQL database. 
    The Note and User entities are stored in separate tables in the database. User details are managed by the UserDetailsService, and user authentication is facilitated through JWT-based tokens.
