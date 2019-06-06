package com.example.feriproject;

public class ImageMessage {
    private String Description;
    private String ImageData;

     ImageMessage(String description, String imageData) {
        this.Description = description;
        this.ImageData = imageData;
    }

    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }

    public String getImageData() {
        return ImageData;
    }
    public void setImageData(String imageData) {
        ImageData = imageData;
    }
}
