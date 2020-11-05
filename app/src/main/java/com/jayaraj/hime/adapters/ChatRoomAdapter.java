package com.jayaraj.hime.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jayaraj.hime.R;
import com.jayaraj.hime.model.Chats;
import com.jayaraj.hime.model.StaticConfig;
import com.jayaraj.hime.util.HimeUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

  private List<Chats> mItems;
  private ItemListener mListener;
  private final Activity context;

  public ChatRoomAdapter(List<Chats> items, ItemListener listener, Activity context) {
    this.mItems = items;
    mListener = listener;
    this.context = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.custom_chat_friends_list, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    holder.setData(mItems.get(position));
  }

  @Override
  public int getItemCount() {
    return mItems.size();
  }

  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    CircleImageView profileImage;
    ImageView fileImageview;
    TextView txtName;
    TextView txtTime;
    TextView txtMessage;
    TextView counterValue;

    String friendId;
    String friendName;
    String friendImage;
    String friendPhone;
    String roomID;

    ViewHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(this);
      profileImage = (CircleImageView) itemView.findViewById(R.id.chatfriendImageUrl);
      txtName = (TextView) itemView.findViewById(R.id.chatfriendName);
      txtTime = (TextView) itemView.findViewById(R.id.timestamp_text_view);
      txtMessage = (TextView) itemView.findViewById(R.id.chatfriendMessage);
      fileImageview = itemView.findViewById(R.id.file_image);
      counterValue = itemView.findViewById(R.id.messagecountTextview);
    }

    void setData(Chats item) {

      friendName = item.name;
      friendId = item.userId;
      roomID = item.idRoom;
      friendImage = item.profileImage;
      friendPhone = item.phonenumber;
      final String message = item.messageBody;
      final String messageType = item.messageType;
      final String timeStamp = item.timestamp;

      txtName.setText(friendName);

      if (friendImage.equals(StaticConfig.STR_DEFAULT_BASE64)) {
        profileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
      } else {
        byte[] decodedString = Base64.decode(friendImage, Base64.DEFAULT);
        Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        profileImage.setImageBitmap(src);
      }

      SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm sss");
      Date mDate;
      try {
        mDate = dateFormat.parse(timeStamp);
        long timeInMilliseconds = mDate.getTime();
        txtTime.setText(HimeUtils.getTimeAge(timeInMilliseconds));
      } catch (ParseException e) {
        Log.e("Exc", e.toString());
      }

      if (messageType.equals("Photo")) {

        txtMessage.setText(message);
        fileImageview.setBackgroundResource(R.drawable.ic_image_black);
        fileImageview.setVisibility(View.VISIBLE);

      } else if (messageType.equals("File")) {

        txtMessage.setText(message);
        fileImageview.setBackgroundResource(R.drawable.ic_insert_drive_file_black_24dp);
        fileImageview.setVisibility(View.VISIBLE);

      } else if (messageType.equals("Video")) {

        txtMessage.setText(message);
        fileImageview.setBackgroundResource(R.drawable.ic_video_library_black_24dp);
        fileImageview.setVisibility(View.VISIBLE);

      } else if (messageType.equals("Audio")) {

        txtMessage.setText(message);
        fileImageview.setBackgroundResource(R.drawable.ic_audiotrack_black_24dp);
        fileImageview.setVisibility(View.VISIBLE);

      } else if (messageType.equals("Location")) {

        txtMessage.setText(message);
        fileImageview.setBackgroundResource(R.drawable.ic_location_on_black_24dp);
        fileImageview.setVisibility(View.VISIBLE);

      } else if (messageType.equals("Text")) {
        txtMessage.setText(message);
        fileImageview.setVisibility(View.GONE);
      }
    }

    @Override
    public void onClick(View v) {
      if (mListener != null) {
        mListener.onItemClick(friendName, friendId, friendImage, friendPhone);
      }
    }
  }

  public void setfilter(List<Chats> listitem) {
    mItems = new ArrayList<>();
    mItems.addAll(listitem);
    notifyDataSetChanged();
  }

  public interface ItemListener {
    void onItemClick(String friendName, String firendId, String friendImage, String friendPhone);
  }
}
