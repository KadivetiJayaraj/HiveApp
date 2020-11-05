package com.jayaraj.hime.notifications;

public class Data {
  private String title;
  private String body;
  private String profilePicture;
  private String himeId;

  public Data(String title, String body, String profilePicture, String himeId) {
    this.title = title;
    this.body = body;
    this.profilePicture = profilePicture;
    this.himeId = himeId;
  }

  public Data() {}
}
