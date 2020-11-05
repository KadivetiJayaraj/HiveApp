package com.jayaraj.hime.model;

public class Message {
  public String messageBody;
  public String imageUrl;
  public String fileUrl;
  public String timestamp;
  public String messagetype;
  public String address;
  public String latitude;
  public String longitude;
  public String roomId;
  public String mediaSize;
  public String profileImage;
  public String himeID;
  public String name;
  public String phoneNumber;
  public boolean isseen;
  public String mediaUri;
  public String messageStatus;
  public int messageCount;

  public Message() {}

  public Message(
      String messageBody,
      String imageUrl,
      String fileUrl,
      String timestamp,
      String messagetype,
      String address,
      String latitude,
      String longitude,
      String roomId,
      String mediaSize,
      String profileImage,
      String himeID,
      String name,
      String phoneNumber,
      boolean isseen,
      String mediaUri) {
    this.messageBody = messageBody;
    this.imageUrl = imageUrl;
    this.fileUrl = fileUrl;
    this.timestamp = timestamp;
    this.messagetype = messagetype;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
    this.roomId = roomId;
    this.mediaSize = mediaSize;
    this.profileImage = profileImage;
    this.himeID = himeID;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.isseen = isseen;
    this.mediaUri = mediaUri;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public boolean isIsseen() {
    return isseen;
  }

  public void setIsseen(boolean isseen) {
    this.isseen = isseen;
  }

  public String getMessageBody() {
    return messageBody;
  }

  public void setMessageBody(String messageBody) {
    this.messageBody = messageBody;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public String getFileUrl() {
    return fileUrl;
  }

  public void setFileUrl(String fileUrl) {
    this.fileUrl = fileUrl;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getMessagetype() {
    return messagetype;
  }

  public void setMessagetype(String messagetype) {
    this.messagetype = messagetype;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getLatitude() {
    return latitude;
  }

  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }

  public String getLongitude() {
    return longitude;
  }

  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public String getMediaSize() {
    return mediaSize;
  }

  public void setMediaSize(String mediaSize) {
    this.mediaSize = mediaSize;
  }

  public String getProfileImage() {
    return profileImage;
  }

  public void setProfileImage(String profileImage) {
    this.profileImage = profileImage;
  }

  public String getHimeID() {
    return himeID;
  }

  public void setHimeID(String himeID) {
    this.himeID = himeID;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getMediaUri() {
    return mediaUri;
  }

  public void setMediaUri(String mediaUri) {
    this.mediaUri = mediaUri;
  }

  public String getMessageStatus() {
    return messageStatus;
  }

  public void setMessageStatus(String messageStatus) {
    this.messageStatus = messageStatus;
  }

  public int getMessageCount() {
    return messageCount;
  }

  public void setMessageCount(int messageCount) {
    this.messageCount = messageCount;
  }
}
