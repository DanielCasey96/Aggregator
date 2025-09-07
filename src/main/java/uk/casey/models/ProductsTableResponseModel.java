package uk.casey.models;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ProductsTableResponseModel {

    private Integer id;
    private String name;
    private String type;
    private String provider;
    private String category;
    private BigDecimal value;
    private Timestamp updatedAt;

    ProductsTableResponseModel(
           Integer id,
           String name,
           String type,
           String provider,
           String category,
           BigDecimal value,
           Timestamp updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.provider = provider;
        this.category = category;
        this.value = value;
        this.updatedAt = updatedAt;
    }

    public ProductsTableResponseModel() {
    }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getValue() {
            return value;
        }

        public void setValue(BigDecimal value) {
            this.value = value;
        }

        public Timestamp getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(Timestamp updatedAt) {
            this.updatedAt = updatedAt;
        }
}
