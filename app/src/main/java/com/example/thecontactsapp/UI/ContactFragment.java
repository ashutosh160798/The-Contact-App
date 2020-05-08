package com.example.thecontactsapp.UI;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.thecontactsapp.Adapter.MyContactRecyclerViewAdapter;
import com.example.thecontactsapp.DatabaseRoom.AppExecutors;
import com.example.thecontactsapp.DatabaseRoom.ContactDatabase;
import com.example.thecontactsapp.MainActivity;
import com.example.thecontactsapp.Model.Contact;
import com.example.thecontactsapp.NetworkUtil.Client;
import com.example.thecontactsapp.R;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.example.thecontactsapp.MainActivity.dialog;
import static com.example.thecontactsapp.NetworkUtil.Client.AUTH_URL;


public class ContactFragment extends Fragment {

    private RecyclerView mContactsRv;
    private FloatingActionButton fab;
    private List<Contact> mContactList;
    private MyContactRecyclerViewAdapter mContactsAdapter;
    private static ApolloClient apolloClient;
    private Fragment fragment;
    private FragmentTransaction fragmentTransaction;
    private ContactDatabase appDB;

    public ContactFragment() {
        mContactList = new ArrayList<>();
    }

    public static ContactFragment newInstance(int columnCount) {
        return new ContactFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        dialog.dismiss();
        appDB = ContactDatabase.getInstance(getContext());

        Bundle bundle = getArguments();
        mContactList = bundle != null ? bundle.getParcelableArrayList("CONTACT LIST") : null;
        if (mContactList == null || mContactList.isEmpty()) {
            mContactList = appDB.contactDao().getContactsWoObserver();
        }

        mContactsRv = view.findViewById(R.id.contacts_recycler);
        mContactsAdapter = new MyContactRecyclerViewAdapter(getActivity(), mContactList, new MyContactRecyclerViewAdapter.ContactClickListener() {
            @Override
            public void onItemClicked(List<Contact> contactList, int position) {
                ContactDetailFragment detailFragment = ContactDetailFragment.newInstance(contactList.get(position));
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame, detailFragment, "DetailFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getActivity().getResources().getDrawable(R.drawable.divider));

        mContactsRv.addItemDecoration(dividerItemDecoration);
        mContactsRv.setAdapter(mContactsAdapter);
        mContactsRv.setNestedScrollingEnabled(false);
        mContactsRv.setLayoutManager(new LinearLayoutManager(getContext()));

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            fragment = new AddContactFragment();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame, fragment);
            fragmentTransaction.addToBackStack(fragment.toString());
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            fragmentTransaction.commit();
        });


        return view;
    }

    public static class OkHttpHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient();
        String token;

        @Override
        protected String doInBackground(String... params) {

            Request.Builder builder = new Request.Builder();
            builder.url(params[0]);
            Request request = builder.build();

            try {
                okhttp3.Response response = client.newCall(request).execute();

                String s = response.body().string();
                try {
                    JSONObject auth = new JSONObject(s);
                    token = auth.getString("access_token");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder1 = original.newBuilder().method(original.method(), original.body());
                    builder1.header("Authorization", "Bearer " + token);
                    return chain.proceed(builder1.build());
                }).build();

                apolloClient = ApolloClient.builder().serverUrl(Client.BASE_URL).okHttpClient(okHttpClient).build();
                List<String> phnNo = new ArrayList<>();
                phnNo.add("32456543");


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }


    }

}
