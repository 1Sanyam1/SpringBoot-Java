package Endpoints.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {

    record ProductsResponse(List<Product> products) {}

    private final RestClient client = RestClient.create("https://dummyjson.com");

    // allowed sort fields -> how to compare them
    private static final Map<String, Comparator<Product>> SORTS = Map.of(
            "title", by(Product::title),
            "category", by(Product::category),
            "brand", by(Product::brand),
            "price", by(Product::price),
            "rating", by(Product::rating),
            "stock", by(Product::stock));

    private static <T extends Comparable<T>> Comparator<Product> by(Function<Product, T> field) {
        return Comparator.comparing(field, Comparator.nullsLast(Comparator.naturalOrder()));
    }

    public PageResponse<Product> list(String category, String brand, List<String> sort,
                                      int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw badRequest("page must be >= 0 and size must be between 1 and 100");
        }

        //fetch all products
        List<Product> products = new ArrayList<>(client.get().uri("/products?limit=0")
                .retrieve().body(ProductsResponse.class).products());

        //filter
        if (category != null) {
            products.removeIf(p -> !category.equalsIgnoreCase(p.category()));
        }
        if (brand != null) {
            products.removeIf(p -> !brand.equalsIgnoreCase(p.brand()));
        }

        //sort
        if (sort != null) {
            Comparator<Product> combined = null;
            for (String item : sort) {
                String[] parts = item.split(",");
                Comparator<Product> c = SORTS.get(parts[0].trim());
                if (c == null) {
                    throw badRequest("Unknown sort field: " + parts[0]);
                }
                String direction = parts.length > 1 ? parts[1].trim().toLowerCase() : "asc";
                if (!direction.equals("asc") && !direction.equals("desc")) {
                    throw badRequest("Sort direction must be asc or desc");
                }
                if (direction.equals("desc")) {
                    c = c.reversed();
                }
                combined = (combined == null) ? c : combined.thenComparing(c);
            }
            if (combined != null) {
                products.sort(combined);
            }
        }

        //page
        int total = products.size();
        int totalPages = (total + size - 1) / size;
        int from = (int) Math.min((long) page * size, total);
        int to = Math.min(from + size, total);

        return new PageResponse<>(products.subList(from, to), page, size,
                total, totalPages, page + 1 < totalPages);
    }

    public Product add(Product product) {
        return client.post().uri("/products/add")
                .contentType(MediaType.APPLICATION_JSON)
                .body(product).retrieve().body(Product.class);
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}