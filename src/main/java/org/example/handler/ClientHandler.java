package org.example.handler;

import org.example.dto.Request;
import org.example.dto.Response;
import org.example.model.Product;
import org.example.repository.ProductRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;


public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final ProductRepository repository;
    private final int clientId;

    public ClientHandler(Socket clientSocket, ProductRepository repository, int clientId) {
        this.clientSocket = clientSocket;
        this.repository = repository;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        ObjectInputStream input = null;
        ObjectOutputStream output = null;

        try {
            input = new ObjectInputStream(clientSocket.getInputStream());
            output = new ObjectOutputStream(clientSocket.getOutputStream());

            System.out.println("[Client " + clientId + "] Connected from " + 
                clientSocket.getInetAddress().getHostAddress());

            boolean clientRunning = true;
            while (clientRunning) {
                try {
                    Request request = (Request) input.readObject();
                    Response response = processRequest(request);
                    output.writeObject(response);
                    output.flush();

                    if (request.getOperation() == Request.Operation.EXIT) {
                        System.out.println("[Client " + clientId + "] Disconnected");
                        clientRunning = false;
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("[Client " + clientId + "] Unknown class received: " + e.getMessage());
                    break;
                } catch (IOException e) {
                    System.out.println("[Client " + clientId + "] Connection lost");
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("[Client " + clientId + "] Error initializing streams: " + e.getMessage());
        } finally {
            closeResources(output, input, clientSocket);
        }
    }


    private Response processRequest(Request request) {
        switch (request.getOperation()) {
            case CREATE:
                return createProduct(request.getProduct());
            case READ:
                return readProduct(request.getProductId());
            case READ_ALL:
                return readAllProducts();
            case UPDATE:
                return updateProduct(request.getProductId(), request.getProduct());
            case DELETE:
                return deleteProduct(request.getProductId());
            case EXIT:
                return new Response(Response.Status.SUCCESS, "Goodbye!");
            default:
                return new Response(Response.Status.ERROR, "Unknown operation");
        }
    }

    private Response createProduct(Product product) {
        if (product == null || product.getName() == null || product.getName().trim().isEmpty()) {
            return new Response(Response.Status.ERROR, "Invalid product data");
        }
        Product created = repository.create(product);
        System.out.println("[Client " + clientId + "] CREATE: " + created);
        return new Response(Response.Status.SUCCESS, "Product created successfully", created);
    }

    private Response readProduct(Long id) {
        if (id == null) {
            return new Response(Response.Status.ERROR, "Product ID is required");
        }
        return repository.read(id)
                .map(product -> new Response(Response.Status.SUCCESS, "Product found", product))
                .orElse(new Response(Response.Status.ERROR, "Product not found"));
    }

    private Response readAllProducts() {
        List<Product> products = repository.readAll();
        System.out.println("[Client " + clientId + "] READ_ALL: Found " + products.size() + " products");
        return new Response(Response.Status.SUCCESS, "Products retrieved", products);
    }

    private Response updateProduct(Long id, Product product) {
        if (id == null || product == null) {
            return new Response(Response.Status.ERROR, "Invalid data");
        }
        return repository.update(id, product)
                .map(updated -> {
                    System.out.println("[Client " + clientId + "] UPDATE: " + updated);
                    return new Response(Response.Status.SUCCESS, "Product updated successfully", updated);
                })
                .orElse(new Response(Response.Status.ERROR, "Product not found"));
    }

    private Response deleteProduct(Long id) {
        if (id == null) {
            return new Response(Response.Status.ERROR, "Product ID is required");
        }
        boolean deleted = repository.delete(id);
        if (deleted) {
            System.out.println("[Client " + clientId + "] DELETE: Product with ID " + id);
            return new Response(Response.Status.SUCCESS, "Product deleted successfully");
        }
        return new Response(Response.Status.ERROR, "Product not found");
    }


    private void closeResources(ObjectOutputStream output, ObjectInputStream input, Socket socket) {
        try {
            if (output != null) output.close();
        } catch (IOException e) {
        }
        try {
            if (input != null) input.close();
        } catch (IOException e) {
        }
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
        }
    }
}
