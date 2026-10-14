package com.printxpress.app.data;

import com.printxpress.app.data.model.ProductCategory;
import com.printxpress.app.data.model.ProductEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ProductCatalog {
    private ProductCatalog() {
    }

    public static final List<ProductEntity> SEED_PRODUCTS = Collections.unmodifiableList(Arrays.asList(
            new ProductEntity(
                    "bc-standard",
                    "Standard Business Cards",
                    ProductCategory.BUSINESS_CARDS.name(),
                    "Matte or gloss cards for professionals and startups across Sri Lanka.",
                    1800.0,
                    "300gsm Art Card,Matte Laminate,Gloss Laminate",
                    "90x55mm,85x55mm",
                    "Clean two-sided card with bold name and contact strip."
            ),
            new ProductEntity(
                    "bc-premium",
                    "Premium Spot UV Cards",
                    ProductCategory.BUSINESS_CARDS.name(),
                    "Raised spot UV highlights for logos and titles.",
                    3200.0,
                    "350gsm Soft-Touch,Spot UV",
                    "90x55mm",
                    "Dark navy card with glossy logo accent."
            ),
            new ProductEntity(
                    "fly-a5",
                    "A5 Flyers",
                    ProductCategory.FLYERS.name(),
                    "Ideal for cafés, events, and local promotions.",
                    4500.0,
                    "150gsm Gloss,170gsm Matte",
                    "A5",
                    "Colourful promo flyer with offer badge."
            ),
            new ProductEntity(
                    "fly-a4",
                    "A4 Flyers",
                    ProductCategory.FLYERS.name(),
                    "Larger format for menus and campaign handouts.",
                    6500.0,
                    "150gsm Gloss,170gsm Matte",
                    "A4",
                    "Full-bleed product flyer with QR code."
            ),
            new ProductEntity(
                    "pos-a2",
                    "A2 Poster",
                    ProductCategory.POSTERS.name(),
                    "Indoor display posters for shops and classrooms.",
                    1200.0,
                    "200gsm Gloss Paper,Photo Paper",
                    "A2",
                    "Event poster with strong headline hierarchy."
            ),
            new ProductEntity(
                    "pos-a1",
                    "A1 Poster",
                    ProductCategory.POSTERS.name(),
                    "Large format posters for window and wall display.",
                    2200.0,
                    "200gsm Gloss Paper,Vinyl Poster",
                    "A1",
                    "Retail sale poster with high-contrast imagery."
            ),
            new ProductEntity(
                    "ban-vinyl",
                    "Vinyl Banner",
                    ProductCategory.BANNERS.name(),
                    "Outdoor-ready vinyl with hem and eyelets.",
                    3500.0,
                    "13oz Vinyl,Mesh Vinyl",
                    "3x2 ft,4x2 ft,6x3 ft",
                    "Grand opening banner with bold store name."
            ),
            new ProductEntity(
                    "stk-diecut",
                    "Die-Cut Stickers",
                    ProductCategory.STICKERS.name(),
                    "Custom shape stickers for packaging and branding.",
                    2500.0,
                    "Vinyl Matte,Vinyl Gloss,Waterproof",
                    "5cm,8cm,Custom",
                    "Logo stickers on kraft packaging."
            ),
            new ProductEntity(
                    "mug-white",
                    "Custom Ceramic Mug",
                    ProductCategory.MUGS.name(),
                    "Sublimation print on durable white ceramic mugs.",
                    1500.0,
                    "White Ceramic,Colour Rim",
                    "11oz",
                    "Photo wrap mug with family portrait."
            ),
            new ProductEntity(
                    "tee-crew",
                    "Custom Crew Neck Tee",
                    ProductCategory.T_SHIRTS.name(),
                    "DTG print on soft cotton tees for teams and events.",
                    2800.0,
                    "100% Cotton,Cotton Blend",
                    "S,M,L,XL,XXL",
                    "Front-print tee with minimalist logo."
            )
    ));

    public static final List<String> SEASONAL_OFFERS = Collections.unmodifiableList(Arrays.asList(
            "Avurudu Special: 15% off flyers & posters until 14 April.",
            "Bulk Business Cards: buy 500, get 100 free.",
            "Back-to-School banners from LKR 2,999."
    ));
}
