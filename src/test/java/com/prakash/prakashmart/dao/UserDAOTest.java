package com.prakash.prakashmart.dao;

import com.prakash.prakashmart.dao.impl.UserDAOImpl;
import com.prakash.prakashmart.exception.DatabaseException;
import com.prakash.prakashmart.model.Role;
import com.prakash.prakashmart.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOTest extends BaseDAOTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        this.userDAO = new UserDAOImpl();
    }

    @Test
    @DisplayName("Should create user and retrieve by email and ID")
    void testSaveAndFindUser() throws DatabaseException {
        User user = new User();
        user.setName("Prakash Test");
        user.setEmail("prakash@test.com");
        user.setPasswordHash("$2a$12$fakepasswordhash12345678901234567890");
        user.setRole(Role.BUYER);

        User saved = userDAO.create(user);
        assertNotNull(saved.getId());

        Optional<User> foundByEmail = userDAO.findByEmail("prakash@test.com");
        assertTrue(foundByEmail.isPresent());
        assertEquals("Prakash Test", foundByEmail.get().getName());
        assertEquals(Role.BUYER, foundByEmail.get().getRole());
        assertTrue(foundByEmail.get().getIsActive());

        Optional<User> foundById = userDAO.findById(saved.getId());
        assertTrue(foundById.isPresent());
        assertEquals("prakash@test.com", foundById.get().getEmail());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void testUniqueEmailConstraint() throws DatabaseException {
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("duplicate@test.com");
        user1.setPasswordHash("hash1");
        user1.setRole(Role.BUYER);
        userDAO.create(user1);

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("duplicate@test.com");
        user2.setPasswordHash("hash2");
        user2.setRole(Role.SELLER);

        assertThrows(DatabaseException.class, () -> userDAO.create(user2));
    }

    @Test
    @DisplayName("Should list all users and update user")
    void testFindAllAndUpdateUser() throws DatabaseException {
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@test.com");
        user1.setPasswordHash("hash1");
        user1.setRole(Role.ADMIN);
        userDAO.create(user1);

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@test.com");
        user2.setPasswordHash("hash2");
        user2.setRole(Role.SELLER);
        userDAO.create(user2);

        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());

        user1.setName("Updated Admin");
        userDAO.update(user1);

        Optional<User> updated = userDAO.findById(user1.getId());
        assertTrue(updated.isPresent());
        assertEquals("Updated Admin", updated.get().getName());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent ID and email")
    void testNonExistentUserLookups() throws DatabaseException {
        Optional<User> nonExistentId = userDAO.findById(999999L);
        assertFalse(nonExistentId.isPresent());

        Optional<User> nonExistentEmail = userDAO.findByEmail("nobody@nowhere.com");
        assertFalse(nonExistentEmail.isPresent());
    }
}
