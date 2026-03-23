package com.example.bai2;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@ActiveProfiles("test")
class SecurityAccessTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void customerCanLoginWithConfiguredCredentials() throws Exception {
        mockMvc.perform(formLogin("/login").user("customer").password("customer123"))
                .andExpect(authenticated().withUsername("customer"))
                .andExpect(redirectedUrl("/products"));
    }

    @Test
    void loginPageRenders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Đăng nhập")))
                .andExpect(content().string(containsString("Nhập tài khoản và mật khẩu để tiếp tục.")));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void customerCanOnlyViewProductsPage() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/home"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/prices"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void customerCannotAccessAdminPages() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/prices/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void customerForbiddenPageRenders() throws Exception {
        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tài khoản customer chỉ được xem trang sản phẩm và giỏ hàng.")));
    }

    @Test
    @WithMockUser(username = "staff", roles = "STAFF")
    void staffPagesRender() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tổng quan")));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Danh mục")));

        mockMvc.perform(get("/prices"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Bảng giá")));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN", "STAFF"})
    void adminFormPagesRender() throws Exception {
        mockMvc.perform(get("/products/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thông tin sản phẩm")));

        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thông tin danh mục")));

        mockMvc.perform(get("/prices/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Thông tin dòng bảng giá")));
    }
}
