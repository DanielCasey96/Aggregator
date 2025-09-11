package uk.casey.request;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import uk.casey.request.handlers.AuthorisationHandler;
import uk.casey.request.handlers.NewProductHandler;
import uk.casey.request.handlers.RegistrationHandler;
import uk.casey.request.handlers.RemoveProductHandler;
import uk.casey.request.handlers.RetrievalHandler;
import uk.casey.request.handlers.UpdateProductHandler;

public class AggregateController {

    private final HttpServer httpServer;
    ProductService productService = new ProductService();

    public AggregateController() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress("localhost", 8080), 0);
        httpServer.createContext("/add-product", new NewProductHandler(productService));
        httpServer.createContext("/accounts", new RetrievalHandler(productService));
        httpServer.createContext("/update-value", new UpdateProductHandler(productService));
        httpServer.createContext("/remove-product", new RemoveProductHandler(productService));
        httpServer.createContext("/register", new RegistrationHandler());
        httpServer.createContext("/authorise", new AuthorisationHandler());
        httpServer.setExecutor(null);
        httpServer.start();
    }
}
