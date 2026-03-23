package com.example.bai2;

import com.example.bai2.dto.CartView;
import com.example.bai2.dto.ProductCatalogPage;
import com.example.bai2.model.Category;
import com.example.bai2.model.Order;
import com.example.bai2.model.Product;
import com.example.bai2.repository.CategoryRepository;
import com.example.bai2.repository.OrderRepository;
import com.example.bai2.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles("test")
class ShoppingFlowTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(this.context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void searchByKeywordReturnsMatchingProducts() throws Exception {
        MvcResult result = mockMvc.perform(get("/products").param("keyword", "iPhone"))
                .andExpect(status().isOk())
                .andReturn();

        ProductCatalogPage catalogPage = (ProductCatalogPage) result.getModelAndView().getModel().get("catalogPage");
        assertEquals(1, catalogPage.getItems().size());
        assertEquals("iPhone 15 128GB", catalogPage.getItems().get(0).getName());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void paginationShowsFiveItemsPerPage() throws Exception {
        MvcResult pageOneResult = mockMvc.perform(get("/products").param("page", "1"))
                .andExpect(status().isOk())
                .andReturn();
        ProductCatalogPage pageOne = (ProductCatalogPage) pageOneResult.getModelAndView().getModel().get("catalogPage");

        MvcResult pageTwoResult = mockMvc.perform(get("/products").param("page", "2"))
                .andExpect(status().isOk())
                .andReturn();
        ProductCatalogPage pageTwo = (ProductCatalogPage) pageTwoResult.getModelAndView().getModel().get("catalogPage");

        assertEquals(5, pageOne.getItems().size());
        assertEquals(4, pageTwo.getItems().size());
        assertTrue(pageOne.isHasNext());
        assertTrue(pageTwo.isHasPrevious());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void sortByPriceAscAndDescWorks() throws Exception {
        MvcResult ascResult = mockMvc.perform(get("/products").param("sort", "priceAsc"))
                .andExpect(status().isOk())
                .andReturn();
        ProductCatalogPage ascPage = (ProductCatalogPage) ascResult.getModelAndView().getModel().get("catalogPage");

        MvcResult descResult = mockMvc.perform(get("/products").param("sort", "priceDesc"))
                .andExpect(status().isOk())
                .andReturn();
        ProductCatalogPage descPage = (ProductCatalogPage) descResult.getModelAndView().getModel().get("catalogPage");

        assertEquals("Máy pha cà phê mini", ascPage.getItems().get(0).getName());
        assertEquals("iPhone 15 128GB", descPage.getItems().get(0).getName());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void filterByCategoryReturnsMatchingProducts() throws Exception {
        Category home = categoryRepository.findAllByOrderByNameAsc().stream()
                .filter(category -> "Gia dụng".equals(category.getName()))
                .findFirst()
                .orElseThrow();

        MvcResult result = mockMvc.perform(get("/products").param("categoryId", home.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();

        ProductCatalogPage catalogPage = (ProductCatalogPage) result.getModelAndView().getModel().get("catalogPage");
        assertFalse(catalogPage.getItems().isEmpty());
        assertTrue(catalogPage.getItems().stream().allMatch(item -> "Gia dụng".equals(item.getCategoryName())));
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void addToCartStoresQuantityInSession() throws Exception {
        Product iphone = productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc("iPhone").stream()
                .findFirst()
                .orElseThrow();

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/cart/add")
                        .session(session)
                        .with(csrf())
                        .param("productId", iphone.getId().toString())
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection());

        MvcResult cartResult = mockMvc.perform(get("/cart").session(session))
                .andExpect(status().isOk())
                .andReturn();

        CartView cart = (CartView) cartResult.getModelAndView().getModel().get("cart");
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(1, cart.getItems().size());
        assertEquals("iPhone 15 128GB", cart.getItems().get(0).getProductName());
    }

    @Test
    @WithMockUser(username = "customer", roles = "CUSTOMER")
    void checkoutCreatesOrderAndOrderDetails() throws Exception {
        Product iphone = productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc("iPhone").stream()
                .findFirst()
                .orElseThrow();
        Product coffee = productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc("cà phê").stream()
                .findFirst()
                .orElseThrow();

        MockHttpSession session = new MockHttpSession();
        mockMvc.perform(post("/cart/add")
                        .session(session)
                        .with(csrf())
                        .param("productId", iphone.getId().toString())
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(post("/cart/add")
                        .session(session)
                        .with(csrf())
                        .param("productId", coffee.getId().toString())
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        long beforeCount = orderRepository.count();
        MvcResult checkoutResult = mockMvc.perform(post("/cart/checkout")
                        .session(session)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/cart/success?orderId=*"))
                .andReturn();

        assertEquals(beforeCount + 1, orderRepository.count());

        String redirectUrl = checkoutResult.getResponse().getRedirectedUrl();
        Long orderId = Long.valueOf(redirectUrl.substring(redirectUrl.lastIndexOf('=') + 1));
        Order order = orderRepository.findDetailedById(orderId).orElseThrow();

        assertEquals("customer", order.getAccountUsername());
        assertEquals(2, order.getOrderDetails().size());
        assertEquals(new BigDecimal("45670000.00"), order.getTotalAmount());

        mockMvc.perform(get("/cart/success").param("orderId", orderId.toString()))
                .andExpect(status().isOk());
    }
}
