package Endpoints.product;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Set<String> ALLOWED_PARAMS =
            Set.of("category", "brand", "sort", "page", "size");

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/list")
    public PageResponse<Product> list(@RequestParam Map<String, String> all,
                                      @RequestParam(required = false) List<String> sort,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "20") int size) {
        for (String key : all.keySet()) {
            if (!ALLOWED_PARAMS.contains(key)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unknown filter field: " + key);
            }
        }
        return service.list(all.get("category"), all.get("brand"), sort, page, size);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Product add(@RequestBody Product product) {
        if (product.title() == null || product.title().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "title is required");
        }
        return service.add(product);
    }
}