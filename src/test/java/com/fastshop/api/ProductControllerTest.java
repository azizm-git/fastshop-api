package com.fastshop.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ProductControllerTest {

    @Test
    void health_returns_UP() {
        ProductController ctrl = new ProductController();
        assertEquals("UP", ctrl.health().get("status"));
    }

    @Test
    void list_returns_three_products() {
        ProductController ctrl = new ProductController();
        assertEquals(3, ctrl.listProducts().size());
    }
}
