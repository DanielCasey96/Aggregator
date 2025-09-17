package uk.casey.request;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import uk.casey.request.handlers.AuthorisationHandler;
import uk.casey.request.handlers.NewProductHandler;
import uk.casey.request.handlers.RegistrationHandler;
import uk.casey.request.handlers.RemoveProductHandler;
import uk.casey.request.handlers.RetrievalHandler;
import uk.casey.request.handlers.UpdateProductHandler;
import uk.casey.request.services.ProductService;
import uk.casey.request.services.UsersService;
import uk.casey.request.services.ProductServiceInterface;
import uk.casey.request.services.UsersServiceInterface;
import uk.casey.utils.JwtUtil;

public class AggregateController {

    public AggregateController() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            properties.load(input);
        }
        ProductServiceInterface productServiceInterface = new ProductService(properties);
        UsersServiceInterface usersServiceInterface = new UsersService(properties);
        ObjectMapper objectMapper = new ObjectMapper();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        httpServer.createContext("/add-product", new NewProductHandler(productServiceInterface, objectMapper));
        httpServer.createContext("/accounts", new RetrievalHandler(productServiceInterface, objectMapper));
        httpServer.createContext("/update-value", new UpdateProductHandler(productServiceInterface, objectMapper));
        httpServer.createContext("/remove-product", new RemoveProductHandler(productServiceInterface));
        httpServer.createContext("/register", new RegistrationHandler(usersServiceInterface, objectMapper));
        httpServer.createContext("/authorise", new AuthorisationHandler(usersServiceInterface , objectMapper));
        httpServer.setExecutor(Executors.newFixedThreadPool(10)); // Remove if hosting on lambda
        httpServer.start();
    }
}
