package com.skillix.merchant.model;

public class Mentee {
    private String firstName;
    private String lastName;
    private String email;
    private String mobileNumber;
    private String gender;
    private String country;
    private String career;

    // Empty Constructor (Needed for Firebase)
    public Mentee() {}

    // Constructor with parameters
    public Mentee(String firstName, String lastName, String email, String mobileNumber, String gender, String country, String career) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.gender = gender;
        this.country = country;
        this.career = career;
    }

    // Getters & Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCareer() { return career; }
    public void setCareer(String career) { this.career = career; }
}

