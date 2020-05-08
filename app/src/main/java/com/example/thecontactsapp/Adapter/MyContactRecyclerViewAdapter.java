package com.example.thecontactsapp.Adapter;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.example.thecontactsapp.Model.Contact;
import com.example.thecontactsapp.R;

import java.util.List;
import java.util.Random;


public class MyContactRecyclerViewAdapter extends RecyclerView.Adapter<MyContactRecyclerViewAdapter.ViewHolder> {

    private final List<Contact> mContactList;
    private ContactClickListener mListener;
    private Context mContext;

    public MyContactRecyclerViewAdapter(Context mContext, List<Contact> items, ContactClickListener mListener) {
        this.mContactList = items;
        this.mContext = mContext;
        this.mListener = mListener;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.contact_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Contact contact = mContactList.get(position);

        holder.mNameView.setText(mContactList.get(position).getName());

        if (mContactList.get(position).getImageUri() != null && !mContactList.get(position).getImageUri().equals("")) {

            holder.mContactImg.setVisibility(View.VISIBLE);
            holder.mNoImgView.setVisibility(View.INVISIBLE);

            Glide.with(mContext)
                    .load(Uri.parse(contact.getImageUri()))
                    .apply(RequestOptions.circleCropTransform()
                    ).into(holder.mContactImg);

        } else {
            holder.mContactImg.setVisibility(View.INVISIBLE);
            holder.mNoImgView.setVisibility(View.VISIBLE);
            Random random = new Random();
            int color = Color.argb(255, random.nextInt(256), random.nextInt(256),
                    random.nextInt(256));

            Drawable drawable = changeDrawableColor(mContext, R.drawable.round, color);
            contact.setColorDrawable(drawable);

            holder.mEmptyLetter.setText(String.valueOf(contact.getName().charAt(0)).toUpperCase());
            holder.mNoImgView.setBackground(drawable);
        }

    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView mContactImg;
        TextView mNameView;
        FrameLayout mNoImgView;
        TextView mEmptyLetter;

        public ViewHolder(View view) {
            super(view);
            mContactImg = view.findViewById(R.id.contact_img);
            mNoImgView = view.findViewById(R.id.no_image_view);
            mNameView = view.findViewById(R.id.name);
            mEmptyLetter = view.findViewById(R.id.letter_tv);
            view.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return super.toString() + " " + mNameView.getText() + "'";
        }


        @Override
        public void onClick(View v) {
            mListener.onItemClicked(mContactList, getAdapterPosition());
        }
    }

    public interface ContactClickListener {
        void onItemClicked(List<Contact> contactList, int position);

    }

    public static Drawable changeDrawableColor(Context context, int id, int color) {

        Drawable drawable = ContextCompat.getDrawable(context, id).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        return drawable;
    }

}
