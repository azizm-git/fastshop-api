package com.fastshop.api;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Map<String, Object>> listProducts() {
        return List.of(
            Map.of("id", 1, "name", "T-shirt ISOSET",   "price", 19.99),
            Map.of("id", 2, "name", "Mug DevOps",       "price",  9.90),
            Map.of("id", 3, "name", "Sticker Jenkins",  "price",  2.50)
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "app", "fastshop-api");
    }
}
