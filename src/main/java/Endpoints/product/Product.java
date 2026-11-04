package Endpoints.product;

public record Product(
    Long id, 
    String title, 
    String category, 
    String brand,
    Double price, 
    Double rating, 
    Integer stock) {
}