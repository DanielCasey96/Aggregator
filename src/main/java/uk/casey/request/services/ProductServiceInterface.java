package uk.casey.request.services;

import uk.casey.models.ProductsTableResponseModel;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.UUID;
import java.util.List;

public interface ProductServiceInterface {

    List<ProductsTableResponseModel> retrieveProductsFromDatabase(UUID userId, Properties properties) throws IOException, SQLException;

    boolean updateProductToDatabase(BigDecimal newValue, int id, UUID userId, Properties properties) throws IOException, SQLException;

    boolean createProductInDatabase(UUID userId, String name, String type, String provider, String category, BigDecimal value, Timestamp updated_at, Properties properties) throws IOException, SQLException;

    boolean removeProductFromDatabase(UUID userId, int id, Properties properties) throws IOException, SQLException;

}
