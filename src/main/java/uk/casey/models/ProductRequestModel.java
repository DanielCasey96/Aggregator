package uk.casey.models;

import java.util.UUID;

public class ProductRequestModel {
    private UUID userId;
    private String name;
    private String type;
    private String provider;
    private String category;
    private java.math.BigDecimal value;
    private java.sql.Timestamp updatedAt;

    public ProductRequestModel() {}

    public ProductRequestModel(UUID userId, String name, String type, String provider, String category, java.math.BigDecimal value, java.sql.Timestamp updatedAt) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.provider = provider;
        this.category = category;
        this.value = value;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
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

    public java.math.BigDecimal getValue() {
        return value;
    }

    public void setValue(java.math.BigDecimal value) {
        this.value = value;
    }

    public java.sql.Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(java.sql.Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}