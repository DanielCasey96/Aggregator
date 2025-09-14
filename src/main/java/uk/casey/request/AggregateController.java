package uk.casey.request;

import java.net.InetSocketAddress;

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

    private final HttpServer httpServer;
    private final ProductServiceInterface productServiceInterface;
    private final UsersServiceInterface usersServiceInterface;
    private final JwtUtil jwtUtil;

    public AggregateController() throws Exception {
        this.productServiceInterface = new ProductService();
        this.usersServiceInterface = new UsersService();
        this.jwtUtil = new JwtUtil();

        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        httpServer.createContext("/add-product", new NewProductHandler(productServiceInterface, jwtUtil));
        httpServer.createContext("/accounts", new RetrievalHandler(productServiceInterface, jwtUtil));
        httpServer.createContext("/update-value", new UpdateProductHandler(productServiceInterface, jwtUtil));
        httpServer.createContext("/remove-product", new RemoveProductHandler(productServiceInterface, jwtUtil));
        httpServer.createContext("/register", new RegistrationHandler(usersServiceInterface));
        httpServer.createContext("/authorise", new AuthorisationHandler(usersServiceInterface));
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
