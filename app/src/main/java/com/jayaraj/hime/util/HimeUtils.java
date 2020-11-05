package com.jayaraj.hime.util;

import android.text.format.DateFormat;

import java.util.Calendar;

public class HimeUtils {

  public HimeUtils() {}

  public static String getTimeAge(long time) {

    Calendar messageTime = Calendar.getInstance();
    messageTime.setTimeInMillis(time);

    Calendar now = Calendar.getInstance();

    final String timeFormatString = "hh:mm a";
    final String dateFormatString = "dd/MM/yyyy";

    if (now.get(Calendar.DATE) == messageTime.get(Calendar.DATE)) {
      return DateFormat.format(timeFormatString, messageTime).toString().toUpperCase();
    } else if (now.get(Calendar.DATE) - messageTime.get(Calendar.DATE) == 1) {
      return "Yesterday";
    } else if (now.get(Calendar.YEAR) == messageTime.get(Calendar.YEAR)) {
      return DateFormat.format(dateFormatString, messageTime).toString();
    } else {
      return DateFormat.format(dateFormatString, messageTime).toString();
    }
  }
}
