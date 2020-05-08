package com.example.thecontactsapp.UI;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.thecontactsapp.Adapter.ContactDetailsAdapter;
import com.example.thecontactsapp.DatabaseRoom.AppExecutors;
import com.example.thecontactsapp.Model.Contact;
import com.example.thecontactsapp.Model.ContactDetail;
import com.example.thecontactsapp.R;

import java.util.ArrayList;
import java.util.Random;

import static com.example.thecontactsapp.Adapter.MyContactRecyclerViewAdapter.changeDrawableColor;
import static com.example.thecontactsapp.MainActivity.appDB;


public class ContactDetailFragment extends Fragment {
    private Contact mContact;
    public final static String CONTACT = "CONTACT";
    private Toolbar mToolbar;
    private ImageView mUserImageView;
    private FrameLayout mUserNoImageLayout;
    private TextView mNoImageLetter;
    private ListView mListView;
    FragmentManager fragmentManager;
    Fragment fragment;
    FragmentTransaction fragmentTransaction;


    public ContactDetailFragment() {
        // Required empty public constructor
    }


    public static ContactDetailFragment newInstance(Contact contact) {
        ContactDetailFragment fragment = new ContactDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CONTACT, contact);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_contact_detail, container, false);
        mToolbar = view.findViewById(R.id.details_toolbar);
        mListView = view.findViewById(R.id.contact_detail_list);
        mNoImageLetter = view.findViewById(R.id.letter_tv);
        mUserImageView = view.findViewById(R.id.details_user_iv);
        mUserNoImageLayout = view.findViewById(R.id.no_image_view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<ContactDetail> contactDetails = new ArrayList<>();
        mContact = (Contact) getArguments().getSerializable(CONTACT);

        if (mContact.getPhone() != null && !mContact.getPhone().isEmpty()) {
            for (String phone : mContact.getPhone())
                contactDetails.add(new ContactDetail("Phone", phone, getActivity().getDrawable(R.drawable.ic_phone_black_48dp)));
        }
        if (mContact.getEmail() != null && !mContact.getEmail().isEmpty()) {
            for (String email : mContact.getEmail())
                contactDetails.add(new ContactDetail("Email", email, getActivity().getDrawable(R.drawable.email)));
        }
        if (mContact.getCategory() != null && !mContact.getCategory().equals("")) {
            contactDetails.add(new ContactDetail("Category", mContact.getCategory(), getActivity().getDrawable(R.drawable.category)));
        }
        if (mContact.getAddress() != null && !mContact.getAddress().equals("")) {
            contactDetails.add(new ContactDetail("Address", mContact.getAddress(), getActivity().getDrawable(R.drawable.location)));
        }
        if (!contactDetails.isEmpty()) {
            ContactDetailsAdapter customAdapter = new ContactDetailsAdapter(getActivity(), contactDetails);
            mListView.setAdapter(customAdapter);
        }


        mToolbar.setTitle(mContact.getName());

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);


        if (mContact.getImageUri() != null) {

            mUserImageView.setVisibility(View.VISIBLE);
            mUserNoImageLayout.setVisibility(View.INVISIBLE);

            Glide.with(getContext())
                    .load(Uri.parse(mContact.getImageUri()))
                    .placeholder(R.drawable.ic_male)
                    .into(mUserImageView);
        } else {

            mUserImageView.setVisibility(View.INVISIBLE);
            mUserNoImageLayout.setVisibility(View.VISIBLE);

            Glide.with(getContext())
                    .load(R.drawable.ic_male)
                    .into(mUserImageView);

            mNoImageLetter.setText(String.valueOf(mContact.getName().charAt(0)).toUpperCase());
            if(mContact.getColorDrawable()==null){
                Random random = new Random();
                int color = Color.argb(255, random.nextInt(256), random.nextInt(256),
                        random.nextInt(256));

                Drawable drawable = changeDrawableColor(getContext(), R.drawable.round, color);
                mUserNoImageLayout.setBackground(drawable);
            }else {
                mUserNoImageLayout.setBackground(mContact.getColorDrawable());
            }


        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.contact_options_menu,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit:
                fragment = new AddContactFragment();
                fragmentManager = getActivity().getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                Bundle bundle = new Bundle();
                Contact obj = mContact;
                bundle.putSerializable("EDIT_CONTACT", obj);
                fragment.setArguments(bundle);
                fragmentTransaction.replace(R.id.frame, fragment);
                fragmentTransaction.addToBackStack(fragment.toString());
                fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                fragmentTransaction.commit();
                return true;
            case R.id.delete:
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Contact")
                        .setMessage("Confirm?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        appDB.contactDao().deleteContact(mContact.getId());
                                        fragment = new ContactFragment();
                                        fragmentManager = getActivity().getSupportFragmentManager();
                                        fragmentTransaction = fragmentManager.beginTransaction();
                                        fragmentTransaction.replace(R.id.frame, fragment);
                                        fragmentTransaction.commit();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onBackPressed() {
        fragment = new ContactFragment();
        fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }
}
