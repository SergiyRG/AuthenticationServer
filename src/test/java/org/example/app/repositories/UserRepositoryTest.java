package org.example.app.repositories;

import org.example.app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

@SpringBootTest
@TestPropertySource("classpath:database.properties")
public class UserRepositoryTest extends AbstractTestNGSpringContextTests {

    @Autowired
    @Qualifier("userRepository")
    private Repository<User> userRepository;

    @DataProvider
    public static Object[][] user_contains_true() {
        return new Object[][]  {
                {"alice@yandex.ru"}, {"bob@gmail.com"}
        };
    }

    @Test(dataProvider = "user_contains_true")
    public void test_user_contains_true(String email) {
        User user = new User();
        user.setEmail(email);

        assertTrue(userRepository.contains(user));
    }

    @DataProvider
    public static Object[][] user_contains_false() {
        return new Object[][] {
                {"karpov1999@gmail.com"},  {"sergey@yandex.ru"}, {"1999@gmail.com"}
        };
    }

    @Test(dataProvider = "user_contains_false")
    public void test_user_contains_false(String email) {
        User user = new User();
        user.setEmail(email);

        assertFalse(userRepository.contains(user));
    }

    @Test
    public void test_database_is_connected() {
        JdbcOperations jdbcOperations = (JdbcOperations) ReflectionTestUtils.getField(userRepository, "jdbcOperations");
        assertNotNull(jdbcOperations);
    }

    @DataProvider
    public static Object[][] user_insert_true() {
        return new Object[][] {
                {"Ramazan", "gusenov.ramazan@yandex.ru", "111222333"},
                {"Anton", "pripolzin@yandex.ru", "222333acdwa"},
                {"Test", "test@gmail.com", "testpassword"}
        };
    }

    private User getUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setEmail(email);

        return user;
    }

    @AfterTest(groups = "insert")
    public void remove() {
        System.out.println("after insert test");

        Object[][] objects = user_insert_true();
        for (Object[] arr: objects) {
            User user = getUser((String) arr[0], (String) arr[1], (String) arr[2]);

            System.out.println(userRepository.remove(user));
        }
    }

    @Test(groups = {"insert"}, dataProvider = "user_insert_true")
    public void test_user_insert_true(String name, String email, String password) {
        User user = getUser(name, email, password);
        assertTrue(userRepository.insert(user));
    }

    @DataProvider
    public static Object[][] user_insert_sql_exception() {
        return new Object[][] {
                {"Sergei", "sergei.karpov1999@yandex.ru", "12345678"},
                {"Sergei", "SeVKarpov@stud.kpfu.ru", "12345678"}
        };
    }

    @Test(dataProvider = "user_insert_sql_exception", expectedExceptions = DuplicateKeyException.class)
    public void test_user_insert_sql_exception(String name, String email, String password) {
        User user = getUser(name, email, password);
        assertFalse(userRepository.insert(user));
    }
}
