package com.skillix.merchant.model;

public class HiredMentee {

    private String menteeId;
    private String profileImgURL;
    private String firstName;
    private String lastName;
    private String career;
    private String packageName;

    public HiredMentee(String menteeId, String profileImgURL, String firstName, String lastName, String career, String packageName) {
        this.menteeId = menteeId;
        this.profileImgURL = profileImgURL;
        this.firstName = firstName;
        this.lastName = lastName;
        this.career = career;
        this.packageName = packageName;
    }

    public HiredMentee() {
    }

    public String getMenteeId() {
        return menteeId;
    }

    public void setMenteeId(String menteeId) {
        this.menteeId = menteeId;
    }

    public String getProfileImgURL() {
        return profileImgURL;
    }

    public void setProfileImgURL(String profileImgURL) {
        this.profileImgURL = profileImgURL;
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

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
