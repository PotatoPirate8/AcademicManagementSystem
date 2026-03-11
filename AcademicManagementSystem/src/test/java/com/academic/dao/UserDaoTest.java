package com.academic.dao;

import com.academic.model.User;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserDao using an in-memory SQLite database.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDaoTest {

    private static UserDao userDao;

    @BeforeAll
    static void setUp() {
        // Use in-memory database so tests don't affect production data
        DatabaseManager.initializeWithUrl("jdbc:sqlite::memory:");
        userDao = new UserDao();
    }

    @AfterAll
    static void tearDown() {
        DatabaseManager.resetInstance();
    }

    @Test
    @Order(1)
    void testDefaultAdminExists() {
        User admin = userDao.findByUsername("admin");
        assertNotNull(admin, "Default admin user should exist after schema initialization");
        assertEquals(User.Role.ADMIN, admin.getRole());
    }

    @Test
    @Order(2)
    void testCreateUser() {
        User user = new User("testuser", "testhash", User.Role.STUDENT);
        int id = userDao.create(user);
        assertTrue(id > 0, "Created user should have a positive ID");
    }

    @Test
    @Order(3)
    void testFindByUsername() {
        User user = userDao.findByUsername("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals(User.Role.STUDENT, user.getRole());
    }

    @Test
    @Order(4)
    void testFindById() {
        User user = userDao.findByUsername("testuser");
        assertNotNull(user);
        User found = userDao.findById(user.getId());
        assertNotNull(found);
        assertEquals(user.getUsername(), found.getUsername());
    }

    @Test
    @Order(5)
    void testAuthenticateSuccess() {
        User user = userDao.authenticate("testuser", "testhash");
        assertNotNull(user, "Authentication should succeed with correct credentials");
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @Order(6)
    void testAuthenticateFailure() {
        User user = userDao.authenticate("testuser", "wronghash");
        assertNull(user, "Authentication should fail with wrong password");
    }

    @Test
    @Order(7)
    void testAuthenticateNonexistentUser() {
        User user = userDao.authenticate("nonexistent", "hash");
        assertNull(user, "Authentication should fail for non-existent user");
    }

    @Test
    @Order(8)
    void testFindAll() {
        assertFalse(userDao.findAll().isEmpty(), "Should return at least the admin and test user");
    }

    @Test
    @Order(9)
    void testUpdatePassword() {
        User user = userDao.findByUsername("testuser");
        assertNotNull(user);
        assertTrue(userDao.updatePassword(user.getId(), "newhash"));

        // Verify new password works
        assertNotNull(userDao.authenticate("testuser", "newhash"));
        // Verify old password no longer works
        assertNull(userDao.authenticate("testuser", "testhash"));
    }

    @Test
    @Order(10)
    void testDeleteUser() {
        User user = userDao.findByUsername("testuser");
        assertNotNull(user);
        assertTrue(userDao.delete(user.getId()));
        assertNull(userDao.findByUsername("testuser"), "Deleted user should no longer be found");
    }

    @Test
    @Order(11)
    void testDeleteNonexistentUser() {
        assertFalse(userDao.delete(99999), "Deleting non-existent user should return false");
    }
}
