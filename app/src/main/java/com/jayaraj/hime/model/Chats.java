package com.jayaraj.hime.model;

public class Chats extends UserProfile {
  public String idRoom;
  public String messageBody;
  public String messageType;
  public String timestamp;

  public Chats(String idRoom, String messageBody, String messageType, String timestamp) {
    this.idRoom = idRoom;
    this.messageBody = messageBody;
    this.messageType = messageType;
    this.timestamp = timestamp;
  }

  public Chats() {}
}
