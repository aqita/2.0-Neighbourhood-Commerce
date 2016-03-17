package com.aqitapartners.sellers;

/**
 * Created by Mustafa on 06-01-2016.
 */
public class ServiceListItem {

    boolean isUOMAvailable ;
    String id ;
    String uom ;
    String quantity ;
    String name ;
    private String price;

/*
    String imageUrl ;
    boolean isImageAvailable ;
*/


    public boolean isUOMAvailable() {
        return isUOMAvailable;
    }

    public void setIsUOMAvailable(boolean isUOMAvailable) {
        this.isUOMAvailable = isUOMAvailable;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

/*
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isImageAvailable() {
        return isImageAvailable;
    }

    public void setIsImageAvailable(boolean isImageAvailable) {
        this.isImageAvailable = isImageAvailable;
    }
*/
    public String getId()
    {
        return id ;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPrice()
    {
        return price ;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
