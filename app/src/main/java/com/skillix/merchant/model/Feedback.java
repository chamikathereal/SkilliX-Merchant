package com.skillix.merchant.model;

public class Feedback {

    private String profileImgURL;
    private String fname;
    private String lname;
    private String career;
    private String feedback;

    public Feedback() {
    }

    public Feedback(String profileImgURL, String fname, String lname, String career, String feedback) {
        this.profileImgURL = profileImgURL;
        this.fname = fname;
        this.lname = lname;
        this.career = career;
        this.feedback = feedback;
    }

    public String getProfileImgURL() {
        return profileImgURL;
    }

    public void setProfileImgURL(String profileImgURL) {
        this.profileImgURL = profileImgURL;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getCareer() {
        return career;
    }

    public void setCareer(String career) {
        this.career = career;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}
