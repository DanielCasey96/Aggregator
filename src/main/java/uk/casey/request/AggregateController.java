package uk.casey.request;

import java.net.InetSocketAddress;
import java.util.Properties;
import java.util.concurrent.Executors;

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
        ProductServiceInterface productServiceInterface = new ProductService();
        UsersServiceInterface usersServiceInterface = new UsersService();
        Properties properties = new Properties();

        HttpServer httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        httpServer.createContext("/add-product", new NewProductHandler(productServiceInterface, properties));
        httpServer.createContext("/accounts", new RetrievalHandler(productServiceInterface, properties));
        httpServer.createContext("/update-value", new UpdateProductHandler(productServiceInterface, properties));
        httpServer.createContext("/remove-product", new RemoveProductHandler(productServiceInterface, properties));
        httpServer.createContext("/register", new RegistrationHandler(usersServiceInterface, properties));
        httpServer.createContext("/authorise", new AuthorisationHandler(usersServiceInterface, properties));
        httpServer.setExecutor(Executors.newFixedThreadPool(10)); // Remove if hosting on lambda
        httpServer.start();
    }
}
