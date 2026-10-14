package com.printxpress.app.data

import com.printxpress.app.data.model.ProductCategory
import com.printxpress.app.data.model.ProductEntity

object ProductCatalog {
    val seedProducts: List<ProductEntity> = listOf(
        ProductEntity(
            id = "bc-standard",
            name = "Standard Business Cards",
            category = ProductCategory.BUSINESS_CARDS.name,
            description = "Matte or gloss cards for professionals and startups across Sri Lanka.",
            basePriceLkr = 1800.0,
            materials = "300gsm Art Card,Matte Laminate,Gloss Laminate",
            sizes = "90x55mm,85x55mm",
            samplePreview = "Clean two-sided card with bold name and contact strip."
        ),
        ProductEntity(
            id = "bc-premium",
            name = "Premium Spot UV Cards",
            category = ProductCategory.BUSINESS_CARDS.name,
            description = "Raised spot UV highlights for logos and titles.",
            basePriceLkr = 3200.0,
            materials = "350gsm Soft-Touch,Spot UV",
            sizes = "90x55mm",
            samplePreview = "Dark navy card with glossy logo accent."
        ),
        ProductEntity(
            id = "fly-a5",
            name = "A5 Flyers",
            category = ProductCategory.FLYERS.name,
            description = "Ideal for cafés, events, and local promotions.",
            basePriceLkr = 4500.0,
            materials = "150gsm Gloss,170gsm Matte",
            sizes = "A5",
            samplePreview = "Colourful promo flyer with offer badge."
        ),
        ProductEntity(
            id = "fly-a4",
            name = "A4 Flyers",
            category = ProductCategory.FLYERS.name,
            description = "Larger format for menus and campaign handouts.",
            basePriceLkr = 6500.0,
            materials = "150gsm Gloss,170gsm Matte",
            sizes = "A4",
            samplePreview = "Full-bleed product flyer with QR code."
        ),
        ProductEntity(
            id = "pos-a2",
            name = "A2 Poster",
            category = ProductCategory.POSTERS.name,
            description = "Indoor display posters for shops and classrooms.",
            basePriceLkr = 1200.0,
            materials = "200gsm Gloss Paper,Photo Paper",
            sizes = "A2",
            samplePreview = "Event poster with strong headline hierarchy."
        ),
        ProductEntity(
            id = "pos-a1",
            name = "A1 Poster",
            category = ProductCategory.POSTERS.name,
            description = "Large format posters for window and wall display.",
            basePriceLkr = 2200.0,
            materials = "200gsm Gloss Paper,Vinyl Poster",
            sizes = "A1",
            samplePreview = "Retail sale poster with high-contrast imagery."
        ),
        ProductEntity(
            id = "ban-vinyl",
            name = "Vinyl Banner",
            category = ProductCategory.BANNERS.name,
            description = "Outdoor-ready vinyl with hem and eyelets.",
            basePriceLkr = 3500.0,
            materials = "13oz Vinyl,Mesh Vinyl",
            sizes = "3x2 ft,4x2 ft,6x3 ft",
            samplePreview = "Grand opening banner with bold store name."
        ),
        ProductEntity(
            id = "stk-diecut",
            name = "Die-Cut Stickers",
            category = ProductCategory.STICKERS.name,
            description = "Custom shape stickers for packaging and branding.",
            basePriceLkr = 2500.0,
            materials = "Vinyl Matte,Vinyl Gloss,Waterproof",
            sizes = "5cm,8cm,Custom",
            samplePreview = "Logo stickers on kraft packaging."
        ),
        ProductEntity(
            id = "mug-white",
            name = "Custom Ceramic Mug",
            category = ProductCategory.MUGS.name,
            description = "Sublimation print on durable white ceramic mugs.",
            basePriceLkr = 1500.0,
            materials = "White Ceramic,Colour Rim",
            sizes = "11oz",
            samplePreview = "Photo wrap mug with family portrait."
        ),
        ProductEntity(
            id = "tee-crew",
            name = "Custom Crew Neck Tee",
            category = ProductCategory.T_SHIRTS.name,
            description = "DTG print on soft cotton tees for teams and events.",
            basePriceLkr = 2800.0,
            materials = "100% Cotton,Cotton Blend",
            sizes = "S,M,L,XL,XXL",
            samplePreview = "Front-print tee with minimalist logo."
        )
    )

    val seasonalOffers = listOf(
        "Avurudu Special: 15% off flyers & posters until 14 April.",
        "Bulk Business Cards: buy 500, get 100 free.",
        "Back-to-School banners from LKR 2,999."
    )
}
