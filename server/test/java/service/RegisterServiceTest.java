package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.UserData;
import model.AuthData;

public class RegisterServiceTest {

    public void testRegisterSuccess() {
        UserDAO mockUserDAO = new UserDAO();
        AuthDAO mockAuthDAO = new AuthDAO();
        RegisterService registerService = new RegisterService(mockUserDAO, mockAuthDAO);
        UserData newUser = new UserData("testuser", "password123", "testuser@mail.com");
        AuthData result = registerService.register(newUser);
        assert mockUserDAO.getUser(newUser.username()) != null : "User should be successfully registered.";
        assert mockAuthDAO.getAuth(result.authToken()) != null : "Auth token should be generated.";
    }

    public void testRegisterMissingFields() {
        UserDAO mockUserDAO = new UserDAO();
        AuthDAO mockAuthDAO = new AuthDAO();
        RegisterService registerService = new RegisterService(mockUserDAO, mockAuthDAO);
        UserData incompleteUser = new UserData("testuser", null, "testuser@mail.com");
        boolean exceptionThrown = false;
        try {
            registerService.register(incompleteUser);
        } catch (IllegalArgumentException e) {
            exceptionThrown = true;
        }
        assert exceptionThrown : "Exception should be thrown for missing required fields.";
    }

    public static void main(String[] args) {
        RegisterServiceTest test = new RegisterServiceTest();
        test.testRegisterSuccess();
        test.testRegisterMissingFields();
        System.out.println("All tests passed.");
    }
}
