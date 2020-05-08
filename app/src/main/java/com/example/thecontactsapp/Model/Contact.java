package com.example.thecontactsapp.Model;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.thecontactsapp.DatabaseRoom.JsonTypeConverter;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "Contacts")
public class Contact implements Serializable, Parcelable {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private Integer id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phone")
    @TypeConverters(JsonTypeConverter.class)
    private List<String> phone;

    @ColumnInfo(name = "image")
    private String imageUri;

    @ColumnInfo(name = "email")
    @TypeConverters(JsonTypeConverter.class)
    private List<String> email;

    @ColumnInfo(name = "address")
    private String address;

    @ColumnInfo(name = "category")
    private String category;

    @Ignore
    private Drawable colorDrawable;

    public Contact() {
    }

    public Contact(Integer id, String name, List<String> phone, String imageUri, List<String> email, String address, String category) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.imageUri = imageUri;
        this.email = email;
        this.address = address;
        this.category = category;
    }

    protected Contact(Parcel in) {
        id = in.readInt();
        name = in.readString();
        phone = in.createStringArrayList();
        imageUri = in.readString();
        email = in.createStringArrayList();
        address = in.readString();
        category = in.readString();
    }

    public static final Creator<Contact> CREATOR = new Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getPhone() {
        return phone;
    }

    public String getImageUri() {
        return imageUri;
    }

    public List<String> getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }

    public Drawable getColorDrawable() {
        return colorDrawable;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(List<String> phone) {
        this.phone = phone;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setColorDrawable(Drawable colorDrawable) {
        this.colorDrawable = colorDrawable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeStringList(phone);
        dest.writeString(imageUri);
        dest.writeStringList(email);
        dest.writeString(address);
        dest.writeString(category);
    }
}
