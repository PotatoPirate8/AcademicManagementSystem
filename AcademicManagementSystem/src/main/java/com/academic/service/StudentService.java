package com.academic.service;

import com.academic.dao.StudentDao;
import com.academic.dao.UserDao;
import com.academic.model.Student;
import com.academic.model.User;
import com.academic.util.PasswordUtil;
import com.academic.util.ValidationUtil;

import java.util.List;

/**
 * Service layer for student-related business rules.
 */
public class StudentService {

    private final UserDao userDao;
    private final StudentDao studentDao;

    public StudentService() {
        this.userDao = new UserDao();
        this.studentDao = new StudentDao();
    }

    public List<Student> getAllStudents() {
        return studentDao.findAll();
    }

    public Student findByUserId(int userId) {
        return studentDao.findByUserId(userId);
    }

    public ServiceResult<Void> updateStudent(Student student, String firstName, String lastName,
                                             String email, String studentNumber, String programme) {
        if (student == null) {
            return ServiceResult.failure("Please select a student to update.");
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return ServiceResult.failure("Name fields cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return ServiceResult.failure("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidStudentNumber(studentNumber)) {
            return ServiceResult.failure("Student number must be 4-12 alphanumeric characters.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return ServiceResult.failure("Programme cannot be empty.");
        }

        student.setFirstName(firstName.trim());
        student.setLastName(lastName.trim());
        student.setEmail(email.trim());
        student.setStudentNumber(studentNumber.trim().toUpperCase());
        student.setProgramme(programme.trim());

        if (studentDao.update(student)) {
            return ServiceResult.success("Student updated successfully.");
        }
        return ServiceResult.failure("Failed to update student.");
    }

    public ServiceResult<Void> updateProfile(Student student, String firstName, String lastName,
                                             String email, String programme) {
        if (student == null) {
            return ServiceResult.failure("Student profile not found.");
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return ServiceResult.failure("Name cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return ServiceResult.failure("Please enter a valid email.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return ServiceResult.failure("Programme cannot be empty.");
        }

        student.setFirstName(firstName);
        student.setLastName(lastName);
        student.setEmail(email);
        student.setProgramme(programme);

        if (studentDao.update(student)) {
            return ServiceResult.success("Profile updated successfully.");
        }
        return ServiceResult.failure("Failed to update profile.");
    }

    public ServiceResult<Integer> registerStudentAccount(String username, String password, String firstName,
                                                         String lastName, String email, String studentNumber,
                                                         String programme) {
        if (!ValidationUtil.isValidUsername(username)) {
            return ServiceResult.failure("Username must be 3-20 alphanumeric characters.");
        }
        if (!ValidationUtil.isValidPassword(password)) {
            return ServiceResult.failure("Password must be at least 6 characters.");
        }
        if (ValidationUtil.isNullOrEmpty(firstName) || ValidationUtil.isNullOrEmpty(lastName)) {
            return ServiceResult.failure("Name fields cannot be empty.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            return ServiceResult.failure("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidStudentNumber(studentNumber)) {
            return ServiceResult.failure("Student number must be 4-12 alphanumeric characters.");
        }
        if (ValidationUtil.isNullOrEmpty(programme)) {
            return ServiceResult.failure("Programme cannot be empty.");
        }
        if (userDao.findByUsername(username) != null) {
            return ServiceResult.failure("Username already taken.");
        }

        String hash = PasswordUtil.hashPassword(password);
        User user = new User(username, hash, User.Role.STUDENT);
        int userId = userDao.create(user);
        if (userId == -1) {
            return ServiceResult.failure("Failed to create user account.");
        }

        Student student = new Student(
            userId, firstName.trim(), lastName.trim(),
            email.trim(), studentNumber.trim().toUpperCase(), programme.trim()
        );
        int studentId = studentDao.create(student);
        if (studentId == -1) {
            userDao.delete(userId);
            return ServiceResult.failure("Failed to create student. Student number may already exist.");
        }

        return ServiceResult.success("Student added successfully.", studentId);
    }

    public ServiceResult<Void> deleteStudent(Student student) {
        if (student == null) {
            return ServiceResult.failure("Please select a student to delete.");
        }
        studentDao.delete(student.getId());
        userDao.delete(student.getUserId());
        return ServiceResult.success("Student deleted successfully.");
    }
}
