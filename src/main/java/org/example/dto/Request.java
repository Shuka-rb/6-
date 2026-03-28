package org.example.dto;

import org.example.model.Product;
import java.io.Serializable;

/**
 * Request DTO for client-server communication.
 * Encapsulates operation type and associated data.
 */
public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Operation {
        CREATE, READ, READ_ALL, UPDATE, DELETE, EXIT
    }

    private Operation operation;
    private Product product;
    private Long productId;

    public Request() {
    }

    public Request(Operation operation) {
        this.operation = operation;
    }

    public Request(Operation operation, Product product) {
        this.operation = operation;
        this.product = product;
    }

    public Request(Operation operation, Long productId) {
        this.operation = operation;
        this.productId = productId;
    }

    public Request(Operation operation, Long productId, Product product) {
        this.operation = operation;
        this.productId = productId;
        this.product = product;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
