package com.example.thecontactsapp.Adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.thecontactsapp.Model.ContactDetail;
import com.example.thecontactsapp.R;

import java.util.ArrayList;

public class ContactDetailsAdapter implements ListAdapter {
    ArrayList<ContactDetail> contactDetailsArrayList;
    Context context;

    public ContactDetailsAdapter(Context context, ArrayList<ContactDetail> contactDetailArrayList) {
        this.context = context;
        this.contactDetailsArrayList = contactDetailArrayList;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return contactDetailsArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactDetail contactDetail = contactDetailsArrayList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.contact_detail_list_item, null);

            /*convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });*/
            TextView type = convertView.findViewById(R.id.details_header);
            ImageView img = convertView.findViewById(R.id.details_action_iv);
            TextView detail = convertView.findViewById(R.id.details_value);
            type.setText(contactDetail.getDetailType());
            detail.setText(contactDetail.getDetail());
            Glide.with(context)
                    .load(contactDetail.getDetailIcon())
                    .into(img);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return contactDetailsArrayList.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


}
