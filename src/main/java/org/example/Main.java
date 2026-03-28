package org.example;

import org.example.client.Client;
import org.example.dto.Request;
import org.example.dto.Response;
import org.example.model.Product;
import org.example.server.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        printHeader();

        boolean running = true;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        Server server = null;

        while (running) {
            printMenu();
            System.out.print("Select option: ");

            try {
                String choice = reader.readLine().trim();

                switch (choice) {
                    case "1":
                        startServerBackground(reader);
                        break;

                    case "2":
                        startClient();
                        break;

                    case "3":
                        startDemoMode();
                        break;

                    case "4":
                        startMultiClientDemo();
                        break;

                    case "0":
                        running = false;
                        System.out.println("\nExiting...");
                        break;

                    default:
                        System.out.println("Invalid option. Please try again.\n");
                }

            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
            }
        }

        if (server != null) {
            server.stop();
        }

        System.out.println("Goodbye!");
    }

    private static void startServerBackground(BufferedReader reader) throws IOException {
        System.out.println("\n>>> Starting SERVER in background...");
        Server server = new Server();
        Thread serverThread = new Thread(server::start);
        serverThread.setDaemon(false);
        serverThread.start();

        System.out.println("Server started in background thread.");
        System.out.println("Now you can start multiple clients (option 2) to test concurrency.");
        System.out.println("Press ENTER to continue...\n");
        reader.readLine();
    }

    private static void startClient() {
        System.out.println("\n>>> Starting CLIENT...");
        try {
            Thread.sleep(500);
            Client client = new Client();
            client.connect();
            client.startConsole();
            client.disconnect();
        } catch (IOException e) {
            System.err.println("Failed to connect to server!");
            System.err.println("Make sure the server is running (option 1).");
            System.err.println("Error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void startDemoMode() {
        System.out.println("\n>>> Starting DEMO MODE (Server + Single Client)...");
        try {
            Server server = new Server();
            Thread serverThread = new Thread(server::start);
            serverThread.start();

            Thread.sleep(2000);

            try {
                Client client = new Client();
                client.connect();
                client.startConsole();
                client.disconnect();
            } catch (IOException e) {
                System.err.println("Client error: " + e.getMessage());
            }

            server.stop();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void startMultiClientDemo() {
        System.out.println("\n>>> Starting MULTI-CLIENT DEMO (Concurrency Test)...");
        System.out.println("This demo shows the server handling multiple clients concurrently.");
        System.out.println();

        try {
            Server server = new Server();
            Thread serverThread = new Thread(server::start);
            serverThread.start();

            Thread.sleep(2000);

            System.out.println("=== Launching 3 concurrent clients ===\n");

            ClientTask clientTask1 = new ClientTask(1, 500);
            ClientTask clientTask2 = new ClientTask(2, 700);
            ClientTask clientTask3 = new ClientTask(3, 900);

            Thread t1 = new Thread(clientTask1, "Client-1-Thread");
            Thread t2 = new Thread(clientTask2, "Client-2-Thread");
            Thread t3 = new Thread(clientTask3, "Client-3-Thread");

            t1.start();
            t2.start();
            t3.start();

            t1.join();
            t2.join();
            t3.join();

            System.out.println("\n=== All clients completed ===");
            System.out.println("This demonstrates concurrent request handling by the server.");
            System.out.println("Each client was processed in a separate thread from the thread pool.");

            Thread.sleep(1000);
            server.stop();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static class ClientTask implements Runnable {
        private final int clientNum;
        private final int delay;

        public ClientTask(int clientNum, int delay) {
            this.clientNum = clientNum;
            this.delay = delay;
        }

        @Override
        public void run() {
            doRun();
        }

        private void doRun() {
            try {
                Thread.sleep(delay);
                System.out.println("[Client " + clientNum + " Thread] Starting...");

                try (Socket socket = new Socket("localhost", 8080);
                     ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

                    System.out.println("[Client " + clientNum + "] Connected - Testing READ_ALL");

                    output.writeObject(new Request(Request.Operation.READ_ALL));
                    output.flush();

                    Response response = (Response) input.readObject();
                    List<Product> products = response.getProducts();
                    System.out.println("[Client " + clientNum + "] Received " +
                        (products != null ? products.size() : 0) + " products");

                    output.writeObject(new Request(Request.Operation.EXIT));
                    output.flush();

                    System.out.println("[Client " + clientNum + "] Completed");

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("[Client " + clientNum + "] Error: " + e.getMessage());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static void printHeader() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║     WAREHOUSE MANAGEMENT SYSTEM - LAB 6                       ║");
        System.out.println("║        Client-Server with Multi-threading Support             ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        System.out.println();
    }

    private static void printMenu() {
        System.out.println("╔═══════════════════════════════════════════════════════════════╗");
        System.out.println("║                         MAIN MENU                             ║");
        System.out.println("╠═══════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. Start Server (background thread)                          ║");
        System.out.println("║  2. Start Client (connect to server)                          ║");
        System.out.println("║  3. Server + Single Client                                    ║");
        System.out.println("║  4. Multi-Client Concurrency Test                             ║");
        System.out.println("║  0. Exit                                                      ║");
        System.out.println("╚═══════════════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
