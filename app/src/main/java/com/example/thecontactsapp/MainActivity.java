package com.example.thecontactsapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.room.Room;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.thecontactsapp.DatabaseRoom.AppExecutors;
import com.example.thecontactsapp.DatabaseRoom.ContactDatabase;
import com.example.thecontactsapp.Model.Contact;
import com.example.thecontactsapp.NetworkUtil.Client;
import com.example.thecontactsapp.UI.ContactDetailFragment;
import com.example.thecontactsapp.UI.ContactFragment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mongodb.Tag;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.example.thecontactsapp.NetworkUtil.Client.AUTH_URL;


public class MainActivity extends AppCompatActivity {

    public static ProgressDialog dialog;
    private static final String TAG = "Main Activity";
    public static String userId;
    private static String token;
    private static OkHttpClient okHTTPClient;
    private static ArrayList<Contact> contactList;
    public static FragmentTransaction fragmentTransaction;
    public static Fragment fragment;
    public static SharedPreferences prefs;
    static ApolloClient apolloClient;
    public static ContactDatabase appDB;
    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appDB = ContactDatabase.getInstance(this);

        //This is deduplication of contacts
        AppExecutors.getInstance().diskIO().execute(this::dedupName);


        fragment = new ContactFragment();
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userId = prefs.getString("userID", "");
        dialog = new ProgressDialog(this);
        dialog.setMessage("Setting up GraphQL");
        dialog.show();

        if (userId.equals("")) {
            final StitchAppClient client =
                    Stitch.initializeDefaultAppClient(getResources().getString(R.string.my_app_id));
            final StitchAppClient stitchAppClient = Stitch.getDefaultAppClient();
            setupDB(stitchAppClient);
        } else {
            ArrayList<Contact> arrlistofContacts = new ArrayList<Contact>(appDB.contactDao().getContactsWoObserver());

            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("CONTACT LIST", arrlistofContacts);
            fragment.setArguments(bundle);
            fragmentTransaction.commit();

//     TODO:       updateDB(Stitch.getDefaultAppClient());
        }


    }

    private void dedupName() {

        List<Contact> contactList = appDB.contactDao().getContactsWoObserver();
        for (int i = 0; i < contactList.size(); i++) {
            Contact c1 = contactList.get(i);
            for (int j = i + 1; j < contactList.size(); j++) {
                Contact c2 = contactList.get(j);
                if (c1.getName().length() != c2.getName().length()) {
                    continue;
                }
                int hammingDistance = hammingDist(c1.getName().toLowerCase(), c2.getName().toLowerCase());
                //Set condition here
                if (hammingDistance < 2) {
                    if(c1.getEmail() == null){
                        c1.setEmail(new ArrayList<>());
                    }
                    if(c1.getPhone() == null){
                        c1.setPhone(new ArrayList<>());
                    }
                    if (c2.getEmail() != null && !c2.getEmail().isEmpty())
                        c1.getEmail().addAll(c2.getEmail());
                    if (c2.getPhone() != null && !c2.getPhone().isEmpty())
                        c1.getPhone().addAll(c2.getPhone());
                    if (c1.getCategory() == null && c2.getCategory() != null)
                        c1.setCategory(c2.getCategory());
                    if (c1.getAddress() == null && c2.getAddress() != null)
                        c1.setAddress(c2.getAddress());

                    List<String> phoneDistinct = new ArrayList<>();
                    List<String> emailDistinct = new ArrayList<>();
                    List<String> phoneList = c1.getPhone();
                    List<String> emailList = c1.getEmail();

                    if (phoneList != null && !phoneList.isEmpty()) {
                        for (String phone : phoneList) {
                            if (phoneDistinct.contains(phone)) {
                                continue;
                            }
                            phoneDistinct.add(phone);
                        }
                    }
                    if (emailList != null && !emailList.isEmpty()) {
                        for (String email : emailList) {
                            if (emailDistinct.contains(email)) {
                                continue;
                            }
                            emailDistinct.add(email);
                        }
                    }

                    c1.setPhone(phoneDistinct);
                    c1.setEmail(emailDistinct);
                    appDB.contactDao().deleteContact(c2.getId());
                    appDB.contactDao().updateContact(c1);


                }
            }
        }

    }

//    private void updateDB(StitchAppClient stitchAppClient) {
//        stitchAppClient.getAuth().loginWithCredential(new AnonymousCredential()).addOnSuccessListener(new OnSuccessListener<StitchUser>() {
//            @Override
//            public void onSuccess(StitchUser stitchUser) {
//                final RemoteMongoClient mongoClient = stitchAppClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
//                okHTTPClient = new OkHttpClient();
//                OkHttpUpdateHandler handler = new OkHttpUpdateHandler();
//                handler.execute(AUTH_URL);
//
//            }
//        });
//    }

    private void setupDB(StitchAppClient stitchAppClient) {

        stitchAppClient.getAuth().loginWithCredential(new AnonymousCredential()).addOnSuccessListener(new OnSuccessListener<StitchUser>() {
            @Override
            public void onSuccess(StitchUser stitchUser) {
                final RemoteMongoClient mongoClient = stitchAppClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
                okHTTPClient = new OkHttpClient();
                OkHttpHandler okHttpHandler = new OkHttpHandler();
                okHttpHandler.execute(AUTH_URL);

            }
        });
    }

    public static class OkHttpHandler extends AsyncTask<String, String, String> {

        OkHttpClient client = new OkHttpClient();

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
                })
                        .build();
                apolloClient = ApolloClient.builder().serverUrl(Client.BASE_URL).okHttpClient(okHttpClient).build();

                apolloClient.mutate(CreateDummyContactMutation.builder().build()).enqueue(new ApolloCall.Callback<CreateDummyContactMutation.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<CreateDummyContactMutation.Data> response) {
                        userId = response.getData().insertOneContact_coll._id + "";
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("userID", userId);
                        editor.apply();

                        apolloClient.query(GetContactsQuery.builder()._id(userId).build()).enqueue(new ApolloCall.Callback<GetContactsQuery.Data>() {

                            @Override
                            public void onResponse(@NotNull Response<GetContactsQuery.Data> response) {
                                contactList = new ArrayList<>();
                                if (response.getData() != null && response.getData().contact_coll != null) {
                                    List<GetContactsQuery.Contact> contactList1 = response.getData().contact_coll.contacts;
                                    for (int i = 0; contactList1.size() > 0 && i < contactList1.size(); i++) {
                                        GetContactsQuery.Contact contact = contactList1.get(i);
                                        Contact contact1 = new Contact(contact.cid, contact.name, contact.phone, contact.img_uri, contact.email, contact.address, contact.category);
                                        appDB.contactDao().insertContact(contact1);
                                        contactList.add(contact1);
                                    }
                                }
                                Bundle bundle = new Bundle();
                                bundle.putParcelableArrayList("CONTACT LIST", contactList);
                                fragment.setArguments(bundle);
                                fragmentTransaction.commit();
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {
                                Log.d(TAG, e.toString());
                            }
                        });

                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {

                    }
                });


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }


    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager().findFragmentByTag("DetailFragment");
        if (f instanceof ContactDetailFragment) {
            ((ContactDetailFragment) f).onBackPressed();
        } else {

            super.onBackPressed();
        }
    }

    static int hammingDist(String str1, String str2) {
        int i = 0, count = 0;
        while (i < str1.length()) {
            if (str1.charAt(i) != str2.charAt(i))
                count++;
            i++;
        }
        return count;
    }
}
