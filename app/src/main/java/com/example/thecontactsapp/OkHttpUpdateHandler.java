package com.example.thecontactsapp;

import android.os.AsyncTask;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.thecontactsapp.DatabaseRoom.ContactDatabase;
import com.example.thecontactsapp.Model.Contact;
import com.example.thecontactsapp.NetworkUtil.Client;
import com.example.thecontactsapp.type.Contact_collContactUpdateInput;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static com.example.thecontactsapp.MainActivity.userId;

class OkHttpUpdateHandler extends AsyncTask<String, String, String> {
    OkHttpClient client = new OkHttpClient();
    private static String token;
    static ApolloClient apolloClient;


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

            List<Contact_collContactUpdateInput> contacts = getContacts();

            apolloClient.mutate(UpdateContactsMutation.builder()._id(userId).contacts(contacts).build()).enqueue(new ApolloCall.Callback<UpdateContactsMutation.Data>() {
                @Override
                public void onResponse(@NotNull Response<UpdateContactsMutation.Data> response) {

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

    private List<Contact_collContactUpdateInput> getContacts() {
        List<Contact_collContactUpdateInput> result = new ArrayList<>();
        ContactDatabase appDB = MainActivity.appDB;
        List<Contact> mContactList = appDB.contactDao().getContactsWoObserver();
        for (Contact contact : mContactList) {
            Input<String> name = new Input<>(contact.getName(), true);
            Input<String> address = new Input<>(contact.getAddress(), true);
            ;
            Input<List<String>> phone = new Input<>(contact.getPhone(), true);
            Input<String> category = new Input<>(contact.getCategory(), true);
            ;
            Input<Integer> cid = new Input<>(contact.getId(), true);
            Input<String> img_uri = new Input<>(contact.getImageUri(), true);
            Input<List<String>> email = new Input<>(contact.getEmail(), true);
            Input<Integer> cid_inc = new Input<>(null, false);
            Input<Boolean> phone_unset = new Input<>(null, false);
            Input<Boolean> address_unset = new Input<>(null, false);
            Input<Boolean> cid_unset = new Input<>(null, false);
            Input<Boolean> email_unset = new Input<>(null, false);
            Input<Boolean> img_uri_unset = new Input<>(null, false);
            Input<Boolean> name_unset = new Input<>(null, false);
            Input<Boolean> category_unset = new Input<>(null, false);

//            Contact_collContactUpdateInput input = new Contact_collContactUpdateInput(cid_inc, address, phone_unset, phone, category,
//                    cid, address_unset, cid_unset, email_unset, img_uri_unset, name, img_uri, email, name_unset,
//                    category_unset);
//            result.add(input);
        }

        return result;
    }


}
