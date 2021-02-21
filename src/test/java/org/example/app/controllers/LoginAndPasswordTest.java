package org.example.app.controllers;

import org.example.app.handlers.CachedHandler;
import org.example.app.models.User;
import org.example.app.repositories.Repository;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.util.NestedServletException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletResponse;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.testng.Assert.assertTrue;

@WebMvcTest(AuthenticationController.class)
public class LoginAndPasswordTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Autowired
    @Qualifier("userRepository")
    private Repository<User> repository;

    @MockBean
    @Autowired
    @Qualifier("memCachedHandler")
    private CachedHandler memCachedHandler;

    @BeforeClass(dependsOnMethods = {"springTestContextPrepareTestInstance"})
    public static void init() {
        MockitoAnnotations.initMocks(LoginAndPasswordTest.class);
    }

    @DataProvider
    public static Object[][] email_and_password_ok() {
        return new Object[][] {
                {"alice@yandex.ru", "AlicePassword"},
                {"bob@gmail.com", "BobPassword"},
                {"jack@gmail.com", "JackPassword"}
        };
    }

    @DataProvider
    public static Object[][] email_and_password_illegal_argument_exception() {
        return new Object[][] {
                {"@yandex.ru", "12345678"},
                {"sergey1999@yandex.ru", "1234"},
                {"1999@gmail.com", "a 2 3 4 5 6 7 8"}
        };
    }

    @BeforeMethod
    public void method_settings() {
        when(repository.isAuthorized(any(User.class))).thenReturn(true);
        when(memCachedHandler.contains(anyString())).thenReturn(true);
        when(memCachedHandler.add(anyString(), any(Object.class), anyInt())).thenReturn(true);
    }

    @Test(dataProvider = "email_and_password_ok")
    public void test_login_and_passwords_ok(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(get("/sign")
                .param("email", email)
                .param("password", password))
                .andReturn();

        assertTrue(result.getResponse().getStatus() == HttpServletResponse.SC_OK);
    }

    @Test(dataProvider = "email_and_password_illegal_argument_exception", expectedExceptions = {IllegalArgumentException.class, NestedServletException.class})
    public void test_login_and_passwords_illegal_argument_exception(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(get("/sign")
                .param("email", email)
                .param("password", password))
                .andReturn();
    }
}
