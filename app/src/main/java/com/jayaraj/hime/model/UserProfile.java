package com.jayaraj.hime.model;

public class UserProfile {

  public String phonenumber;
  public String name;
  public String about;
  public String profileImage;
  public String userId;
  public String userUid;
  public String serviceNo;
  public String modelNo;

  public UserProfile() {}

  public UserProfile(
      String phonenumber,
      String name,
      String about,
      String profileImage,
      String userId,
      String userUid) {
    this.phonenumber = phonenumber;
    this.name = name;
    this.about = about;
    this.profileImage = profileImage;
    this.userId = userId;
    this.userUid = userUid;
  }

  public String getPhonenumber() {
    return phonenumber;
  }

  public void setPhonenumber(String phonenumber) {
    this.phonenumber = phonenumber;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAbout() {
    return about;
  }

  public void setAbout(String about) {
    this.about = about;
  }

  public String getProfileImage() {
    return profileImage;
  }

  public void setProfileImage(String profileImage) {
    this.profileImage = profileImage;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getUserUid() {
    return userUid;
  }

  public void setUserUid(String userUid) {
    this.userUid = userUid;
  }
}
