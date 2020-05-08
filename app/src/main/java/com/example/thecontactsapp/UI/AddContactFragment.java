package com.example.thecontactsapp.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.thecontactsapp.DatabaseRoom.AppExecutors;
import com.example.thecontactsapp.DatabaseRoom.ContactDatabase;
import com.example.thecontactsapp.Model.Contact;
import com.example.thecontactsapp.R;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class AddContactFragment extends Fragment {

    private static final int RESULT_LOAD_IMG = 100;
    private AutoCompleteTextView nameET;
    private AutoCompleteTextView phoneET;
    private AutoCompleteTextView emailET;
    private AutoCompleteTextView addressET;
    private Spinner categorySpinner;
    private ImageView photoIV;
    private ImageView location;
    private ImageView camera;
    private ImageView addPhone;
    private ImageView addEmail;
    private List<String> categoryList;
    private String category;
    private String address;
    private Integer cid = null;
    private LinearLayout linearLayout;
    private int phoneIndex = 4;
    private int emailIndex = 5;
    private final static String PHONE_TAG = "Phone";
    private final static String EMAIL_TAG = "Email";
    private ContactDatabase appDB;
    private FragmentTransaction fragmentTransaction;
    private ContactDetailFragment fragment;
    private FragmentManager fragmentManager;
    private Toolbar mToolbar;

    public AddContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        appDB = ContactDatabase.getInstance(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);
        nameET = view.findViewById(R.id.name_et);
        emailET = view.findViewById(R.id.email_et);
        mToolbar = view.findViewById(R.id.contacts_toolbar);
        phoneET = view.findViewById(R.id.phone_et);
        addressET = view.findViewById(R.id.address_et);
        categorySpinner = view.findViewById(R.id.spinner);
        photoIV = view.findViewById(R.id.contact_img);
        camera = view.findViewById(R.id.camera);
        addEmail = view.findViewById(R.id.add_email_iv);
        addPhone = view.findViewById(R.id.add_phone_iv);
        location = view.findViewById(R.id.target);
        linearLayout = view.findViewById(R.id.linear_layout);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(130);
        params.setMarginEnd(115);
        categoryList = new ArrayList<>();
        categoryList.add("Select Category");
        categoryList.add("Family");
        categoryList.add("Home");
        categoryList.add("Office");
        categoryList.add("Help");
        categoryList.add("Misc");
        categoryList.add("Custom");
        Bundle bundle = getArguments();
        if (bundle != null) {
            Contact contact = (Contact) bundle.getSerializable("EDIT_CONTACT");
            mToolbar.setTitle("Edit Contact");
            cid = contact.getId();
            nameET.setText(contact.getName());
            if (contact.getPhone() != null && !contact.getPhone().isEmpty()) {
                List<String> phoneList = contact.getPhone();
                phoneET.setText(phoneList.get(0));
                if (phoneList.size() > 1) {
                    for (String phone : phoneList.subList(1, phoneList.size())) {
                        AutoCompleteTextView phoneET = new AutoCompleteTextView(getContext());
                        phoneET.setText(phone);
                        phoneET.setTextSize(18);
                        phoneET.setLayoutParams(params);
                        phoneET.setTag(PHONE_TAG);
                        phoneET.setInputType(InputType.TYPE_CLASS_PHONE);
                        linearLayout.addView(phoneET, phoneIndex++);
                        emailIndex++;
                    }
                }
            }

            if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
                List<String> emailList = contact.getEmail();
                emailET.setText(emailList.get(0));
                if (emailList.size() > 1) {
                    for (String email : emailList.subList(1, emailList.size())) {
                        AutoCompleteTextView emailET = new AutoCompleteTextView(getContext());
                        emailET.setText(email);
                        emailET.setLayoutParams(params);
                        emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                        emailET.setTextSize(18);
                        emailET.setTag(EMAIL_TAG);
                        linearLayout.addView(emailET, emailIndex++);
                    }
                }
            }
            if (contact.getAddress() != null && !contact.getAddress().equals("")) {
                addressET.setText(contact.getAddress());
            }
            if (contact.getCategory() != null && !contact.getCategory().equals("")) {
                categoryList.set(0, contact.getCategory());
            }
        }

        photoIV.setOnClickListener(v -> {
            getImage();
        });


        ArrayAdapter<String> aa = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, categoryList);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        categorySpinner.setAdapter(aa);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (selectedItem.equals("Custom")) {
                    AutoCompleteTextView autoCompleteTextView = new AutoCompleteTextView(getContext());
                    AlertDialog dialog = new AlertDialog.Builder(getContext())
                            .setTitle("Create Custom Category")
                            .setView(autoCompleteTextView)
                            .setPositiveButton("OK", (dialogInterface, i) -> {
                                category = autoCompleteTextView.getText().toString().trim();
                                categoryList.set(6, category);
                                categorySpinner.setAdapter(aa);
                                categorySpinner.setSelection(6);
                            })
                            .setNegativeButton("Cancel", null)
                            .create();
                    dialog.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        location.setVisibility(View.GONE);
        location.setOnClickListener(v -> {


        });

        addPhone.setOnClickListener(v -> {
            AutoCompleteTextView phoneET = new AutoCompleteTextView(getContext());
            phoneET.setHint("Phone");
            phoneET.setTextSize(18);
            phoneET.setLayoutParams(params);
            phoneET.setTag(PHONE_TAG);
            phoneET.setInputType(InputType.TYPE_CLASS_PHONE);
            linearLayout.addView(phoneET, phoneIndex++);
            emailIndex++;
        });

        addEmail.setOnClickListener(v -> {
            AutoCompleteTextView emailET = new AutoCompleteTextView(getContext());
            emailET.setHint(EMAIL_TAG);
            emailET.setLayoutParams(params);
            emailET.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            emailET.setTextSize(18);
            emailET.setTag("Email");
            linearLayout.addView(emailET, emailIndex++);
        });


    }

    private static ArrayList<String> getDetailByTag(ViewGroup root, String tag) {
        ArrayList<String> detail = new ArrayList<String>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                detail.addAll(getDetailByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                String text = ((AutoCompleteTextView) child).getText().toString().trim();
                if (!text.trim().equals(""))
                    detail.add(text);
            }
        }
        return detail;
    }

    private void getImage() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                photoIV.setImageBitmap(getCircularBitmap(selectedImage));
                camera.setVisibility(View.GONE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(getContext(), "You haven't picked Image", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap getCircularBitmap(Bitmap srcBitmap) {
        int squareBitmapWidth = 500;
        Bitmap dstBitmap = Bitmap.createBitmap(
                squareBitmapWidth, // Width
                squareBitmapWidth, // Height
                Bitmap.Config.ARGB_8888 // Config
        );
        Canvas canvas = new Canvas(dstBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, squareBitmapWidth, squareBitmapWidth);
        RectF rectF = new RectF(rect);
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        float left = (squareBitmapWidth - srcBitmap.getWidth()) / 2;
        float top = (squareBitmapWidth - srcBitmap.getHeight()) / 2;
        canvas.drawBitmap(srcBitmap, left, top, paint);
        srcBitmap.recycle();
        return dstBitmap;
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.new_contact, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                if (!nameET.getText().toString().trim().equals("") && nameET.getText() != null) {
                    Contact contact = new Contact();
                    contact.setName(nameET.getText().toString().trim());
                    contact.setId(cid);

                    if (!getDetailByTag(linearLayout, PHONE_TAG).isEmpty()) {
                        contact.setPhone(getDetailByTag(linearLayout, PHONE_TAG));
                    }
                    if (!getDetailByTag(linearLayout, EMAIL_TAG).isEmpty()) {
                        contact.setEmail(getDetailByTag(linearLayout, EMAIL_TAG));
                    }
                    if (!addressET.getText().toString().trim().equals("") && addressET.getText() != null) {
                        contact.setAddress(addressET.getText().toString().trim());
                    }
                    if (!((String) categorySpinner.getSelectedItem()).equals("Select Category"))
                        contact.setCategory((String) categorySpinner.getSelectedItem());
                    //TODO: handle image
                    AppExecutors.getInstance().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (contact.getId() != null && contact.getId() != 0) {
                                appDB.contactDao().updateContact(contact);
                                fragment = ContactDetailFragment.newInstance(contact);
                                fragmentManager = getActivity().getSupportFragmentManager();
                                fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.replace(R.id.frame, fragment, "DetailFragment");
                                fragmentTransaction.commit();
                            } else {

                                List<Contact> list = appDB.contactDao().getContactsWoObserver();
                                Contact temp = null;

                                //De DUP Email
                                List<String> emailList = new ArrayList<>();
                                for (Contact contact1 : list) {
                                    emailList = contact1.getEmail();
                                    if (emailList == null)
                                        continue;
                                    boolean flag = false;
                                    for (String email : emailList) {
                                        if(contact.getEmail()==null || contact.getEmail().isEmpty())
                                            break;
                                        if (contact.getEmail().contains(email)) {
                                            emailList.addAll(contact.getEmail());
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        temp = contact1;
                                        break;
                                    }
                                }


                                //DE DUP Phone
                                List<String> phoneList = new ArrayList<>();
                                for (Contact contact1 : list) {
                                    phoneList = contact1.getPhone();
                                    if (phoneList == null)
                                        continue;
                                    boolean flag = false;

                                    for (String phone : phoneList) {
                                        if(contact.getPhone()==null || contact.getPhone().isEmpty())
                                            break;
                                        if (contact.getPhone().contains(phone)) {
                                            phoneList.addAll(contact.getPhone());
                                            flag = true;
                                            break;
                                        }
                                    }
                                    if (flag) {
                                        temp = contact1;
                                        break;
                                    }
                                }




                                if (temp == null) {
                                    appDB.contactDao().insertContact(contact);
                                    fragment = ContactDetailFragment.newInstance(contact);
                                    fragmentManager = getActivity().getSupportFragmentManager();
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.frame, fragment, "DetailFragment");
                                    fragmentTransaction.commit();
                                } else {
                                        List<String> phoneDistinct = new ArrayList<>();
                                        List<String> emailDistinct = new ArrayList<>();

                                        if(phoneList!=null && !phoneList.isEmpty()){
                                            for (String phone : phoneList) {
                                                if (phoneDistinct.contains(phone)) {
                                                    continue;
                                                }
                                                phoneDistinct.add(phone);
                                            }
                                        }
                                        if(emailList!=null && !emailList.isEmpty()){
                                            for (String email : emailList) {
                                                if (emailDistinct.contains(email)) {
                                                    continue;
                                                }
                                                emailDistinct.add(email);
                                            }
                                        }

                                    temp.setPhone(phoneDistinct);
                                    temp.setEmail(emailDistinct);


                                    appDB.contactDao().updateContact(temp);
                                    fragment = ContactDetailFragment.newInstance(temp);
                                    fragmentManager = getActivity().getSupportFragmentManager();
                                    fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.frame, fragment, "DetailFragment");
                                    fragmentTransaction.commit();
                                }

                            }
                        }
                    });

                } else {
                    Toast.makeText(getContext(), "Enter Contact name", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);


        }
    }


}
