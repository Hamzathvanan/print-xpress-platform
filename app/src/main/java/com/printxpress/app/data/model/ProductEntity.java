package com.printxpress.app.data.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "products")
public class ProductEntity {
    @PrimaryKey
    @NonNull
    public String id = "";

    public String name;
    public String category;
    public String description;
    public double basePriceLkr;
    public String materials;
    public String sizes;
    public String samplePreview;

    public ProductEntity() {
    }

    @Ignore
    public ProductEntity(@NonNull String id, String name, String category, String description,
                         double basePriceLkr, String materials, String sizes, String samplePreview) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.basePriceLkr = basePriceLkr;
        this.materials = materials;
        this.sizes = sizes;
        this.samplePreview = samplePreview;
    }
}
