
package com.apartment;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.OutputStream;

public class WebServer {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/", exchange -> {
            try {
                byte[] page = Files.readAllBytes(
                    Path.of("web", "index.html")
                );

                exchange.getResponseHeaders().set(
                    "Content-Type", "text/html; charset=UTF-8"
                );

                exchange.sendResponseHeaders(200, page.length);

                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(page);
                }

            } catch (Exception e) {
                byte[] error = "Website file not found!".getBytes();
                exchange.sendResponseHeaders(500, error.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(error);
                }
            }
        });

        server.start();

        System.out.println("Apartment Website Started!");
        System.out.println("Open: http://localhost:8080");
    }
}
