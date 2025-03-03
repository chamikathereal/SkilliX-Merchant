package com.skillix.merchant.model;

public class MessageModel {
    private String id;
    private String imgUrl;
    private String firstName;
    private String lastName;
    private String career;


    public MessageModel(String id, String imgUrl, String firstName, String lastName, String career) {
        this.id = id;
        this.imgUrl = imgUrl;
        this.firstName = firstName;
        this.lastName = lastName;
        this.career = career;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }
}

