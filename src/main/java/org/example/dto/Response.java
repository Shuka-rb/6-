package org.example.dto;

import org.example.model.Product;
import java.io.Serializable;
import java.util.List;

/**
 * Response DTO for client-server communication.
 * Encapsulates operation status, message, and optional data.
 */
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status {
        SUCCESS, ERROR
    }

    private Status status;
    private String message;
    private Product product;
    private List<Product> products;

    public Response() {
    }

    public Response(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response(Status status, String message, Product product) {
        this.status = status;
        this.message = message;
        this.product = product;
    }

    public Response(Status status, String message, List<Product> products) {
        this.status = status;
        this.message = message;
        this.products = products;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
}
