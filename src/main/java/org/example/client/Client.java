package org.example.client;

import org.example.dto.Request;
import org.example.dto.Response;
import org.example.model.Product;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;


public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private Scanner scanner;
    private volatile boolean connected = false;


    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());
        scanner = new Scanner(System.in);
        connected = true;
        System.out.println("Connected to server at " + HOST + ":" + PORT);
    }


    public void startConsole() {
        boolean running = true;

        System.out.println();
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║           WAREHOUSE MANAGEMENT SYSTEM - CLIENT                ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Create Product                                            ║");
        System.out.println("║  2. Get Product by ID                                         ║");
        System.out.println("║  3. Get All Products                                          ║");
        System.out.println("║  4. Update Product                                            ║");
        System.out.println("║  5. Delete Product                                            ║");
        System.out.println("║  0. Exit                                                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();

        while (running && connected) {
            System.out.print("Enter command: ");
            String command = "";
            try {
                command = scanner.nextLine().trim();
            } catch (Exception e) {
                break;
            }

            try {
                switch (command) {
                    case "1":
                        createProduct();
                        break;
                    case "2":
                        getProductById();
                        break;
                    case "3":
                        getAllProducts();
                        break;
                    case "4":
                        updateProduct();
                        break;
                    case "5":
                        deleteProduct();
                        break;
                    case "0":
                        try {
                            sendRequest(new Request(Request.Operation.EXIT));
                        } catch (Exception e) {
                        }
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid command. Enter 0-5.");
                }
            } catch (IOException e) {
                connected = false;
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void createProduct() throws IOException {
        System.out.println("--- CREATE PRODUCT ---");

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter price: ");
        double price = Double.parseDouble(scanner.nextLine());

        Product product = new Product(name, category, quantity, price);
        Response response = sendRequest(new Request(Request.Operation.CREATE, product));
        printResponse(response);
    }

    private void getProductById() throws IOException {
        System.out.println("--- GET PRODUCT BY ID ---");
        System.out.print("Enter product ID: ");
        long id = Long.parseLong(scanner.nextLine());

        Response response = sendRequest(new Request(Request.Operation.READ, id));
        printResponse(response);
    }

    private void getAllProducts() throws IOException {
        System.out.println("--- GET ALL PRODUCTS ---");
        Response response = sendRequest(new Request(Request.Operation.READ_ALL));

        if (response.getStatus() == Response.Status.SUCCESS && response.getProducts() != null) {
            List<Product> products = response.getProducts();
            if (products.isEmpty()) {
                System.out.println("No products in warehouse.");
            } else {
                System.out.println("┌───────┬───────────────────────────────┬─────────────┬───────────┬─────────────┐");
                System.out.println("│  ID   │  Name                         │  Category   │  Quantity │  Price      │");
                System.out.println("├───────┼───────────────────────────────┼─────────────┼───────────┼─────────────┤");
                for (Product p : products) {
                    System.out.printf("│ %-5d │ %-29s │ %-11s │ %-9d │ $%-10.2f │%n",
                            p.getId(),
                            truncate(p.getName(), 29),
                            truncate(p.getCategory(), 11),
                            p.getQuantity(),
                            p.getPrice());
                }
                System.out.println("└───────┴───────────────────────────────┴─────────────┴───────────┴─────────────┘");
                System.out.println("Total: " + products.size() + " products");
            }
        } else {
            System.out.println("Error: " + response.getMessage());
        }
    }

    private void updateProduct() throws IOException {
        System.out.println("--- UPDATE PRODUCT ---");
        System.out.print("Enter product ID: ");
        long id = Long.parseLong(scanner.nextLine());

        System.out.print("Enter new name: ");
        String name = scanner.nextLine();

        System.out.print("Enter new category: ");
        String category = scanner.nextLine();

        System.out.print("Enter new quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine());

        System.out.print("Enter new price: ");
        double price = Double.parseDouble(scanner.nextLine());

        Product product = new Product(name, category, quantity, price);
        Response response = sendRequest(new Request(Request.Operation.UPDATE, id, product));
        printResponse(response);
    }

    private void deleteProduct() throws IOException {
        System.out.println("--- DELETE PRODUCT ---");
        System.out.print("Enter product ID: ");
        long id = Long.parseLong(scanner.nextLine());

        Response response = sendRequest(new Request(Request.Operation.DELETE, id));
        printResponse(response);

        if (response.getStatus() == Response.Status.SUCCESS) {
            System.out.println("Remaining products renumbered.");
        }
    }

    private Response sendRequest(Request request) throws IOException {
        output.writeObject(request);
        output.flush();
        try {
            return (Response) input.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error reading response", e);
        }
    }

    private void printResponse(Response response) {
        if (response.getStatus() == Response.Status.SUCCESS) {
            System.out.println("✓ Success: " + response.getMessage());
            if (response.getProduct() != null) {
                Product p = response.getProduct();
                System.out.println("  ID: " + p.getId());
                System.out.println("  Name: " + p.getName());
                System.out.println("  Category: " + p.getCategory());
                System.out.println("  Quantity: " + p.getQuantity());
                System.out.println("  Price: $" + p.getPrice());
            }
        } else {
            System.out.println("✗ Error: " + response.getMessage());
        }
        System.out.println();
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 3) + "...";
    }

    public void disconnect() {
        connected = false;

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
        }

        System.out.println("Disconnected from server.");
    }

    public static void main(String[] args) {
        Client client = new Client();
        try {
            client.connect();
            client.startConsole();
        } catch (IOException e) {
            System.err.println("Failed to connect to server. Make sure the server is running.");
        } finally {
            client.disconnect();
        }
    }
}
