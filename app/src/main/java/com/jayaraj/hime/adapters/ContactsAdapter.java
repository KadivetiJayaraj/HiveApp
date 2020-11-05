package com.jayaraj.hime.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.jayaraj.hime.R;
import com.jayaraj.hime.model.FetchContacts;
import com.jayaraj.hime.model.StaticConfig;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

  private List<FetchContacts> mItems;
  private ItemListener mListener;
  private final Context context;

  public ContactsAdapter(List<FetchContacts> items, ItemListener listener, Context context) {
    this.mItems = items;
    mListener = listener;
    this.context = context;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.custom_contacts_list, parent, false));
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

    public TextView title;
    public TextView phone;
    public TextView about;

    public CircleImageView profileImage;

    String friendId;
    String friendName;
    String friendImage;
    String friendPhone;

    ViewHolder(View itemView) {
      super(itemView);
      itemView.setOnClickListener(this);
      title = (TextView) itemView.findViewById(R.id.txtName);
      phone = (TextView) itemView.findViewById(R.id.txtPhone);
      about = (TextView) itemView.findViewById(R.id.textAbout);
      profileImage = (CircleImageView) itemView.findViewById(R.id.contact_avata);
    }

    void setData(FetchContacts item) {

      friendName = item.getName();
      friendId = item.userId;
      friendImage = item.profileImage;
      friendPhone = item.getPhonenumber();
      title.setText(friendName);
      phone.setText(friendPhone);
      about.setText(item.about);

      if (friendImage.equals(StaticConfig.STR_DEFAULT_BASE64)) {
        profileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
      } else {
        byte[] decodedString = Base64.decode(friendImage, Base64.DEFAULT);
        Bitmap src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        profileImage.setImageBitmap(src);
      }
    }

    @Override
    public void onClick(View v) {
      if (mListener != null) {
        mListener.onItemClick(friendName, friendId, friendImage, friendPhone);
      }
    }
  }

  public interface ItemListener {
    void onItemClick(String friendName, String firendId, String friendImage, String friendPhone);
  }
}
