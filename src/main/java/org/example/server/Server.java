package org.example.server;

import org.example.handler.ClientHandler;
import org.example.model.Product;
import org.example.repository.ProductRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {

    private static final int PORT = 8080;
    private static final int THREAD_POOL_SIZE = 10;

    private final ProductRepository repository = new ProductRepository();
    private ServerSocket serverSocket;
    private ExecutorService threadPool;
    private final AtomicInteger clientIdCounter = new AtomicInteger(0);
    private volatile boolean running = true;


    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            System.out.println("╔═══════════════════════════════════════════════════════════════╗");
            System.out.println("║     WAREHOUSE MANAGEMENT SYSTEM - MULTI-THREADED SERVER       ║");
            System.out.println("╠═══════════════════════════════════════════════════════════════╣");
            System.out.println("║  Port: " + PORT);
            System.out.println("║  Thread Pool Size: " + THREAD_POOL_SIZE);
            System.out.println("║  Repository: Thread-safe with ReentrantReadWriteLock          ║");
            System.out.println("╚═══════════════════════════════════════════════════════════════╝");
            System.out.println();

            initializeSampleData();

            System.out.println("Server is ready to accept connections...");
            System.out.println();

            while (running) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    int clientId = clientIdCounter.incrementAndGet();

                    ClientHandler handler = new ClientHandler(clientSocket, repository, clientId);
                    threadPool.submit(handler);

                    System.out.println("[Server] Client " + clientId + " submitted to thread pool");
                    System.out.println("[Server] Active threads: " + getActiveThreadCount());

                } catch (IOException e) {
                    if (running) {
                        System.err.println("[Server] Error accepting client: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[Server] Server error: " + e.getMessage());
            }
        } finally {
            stop();
        }
    }

    private void initializeSampleData() {
        System.out.println("[Server] Initializing sample data...");
        repository.create(new Product("Laptop Dell XPS 15", "Electronics", 25, 1299.99));
        repository.create(new Product("Mouse Logitech MX Master", "Electronics", 50, 99.99));
        repository.create(new Product("Keyboard Mechanical RGB", "Electronics", 30, 149.99));
        repository.create(new Product("Office Chair Ergonomic", "Furniture", 15, 399.99));
        repository.create(new Product("Desk Standing Adjustable", "Furniture", 10, 599.99));
        System.out.println("[Server] Sample data initialized: " + repository.count() + " products");
        System.out.println();
    }

    private int getActiveThreadCount() {
        if (threadPool instanceof ThreadPoolExecutor) {
            return ((ThreadPoolExecutor) threadPool).getActiveCount();
        }
        return 0;
    }


    public void stop() {
        running = false;

        System.out.println("\n[Server] Shutting down server...");

        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
        }

        if (threadPool != null) {
            threadPool.shutdown();
            System.out.println("[Server] Thread pool shutdown initiated");
        }

        System.out.println("[Server] Server stopped");
    }


    public boolean isRunning() {
        return running;
    }


    public ProductRepository getRepository() {
        return repository;
    }

    public static void main(String[] args) {
        Server server = new Server();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n[Shutdown Hook] Shutting down server...");
            server.stop();
        }));

        server.start();
    }
}
