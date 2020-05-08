package com.example.thecontactsapp.Model;

import android.graphics.drawable.Drawable;

public class ContactDetail {

    private String detailType;
    private String detail;
    private Drawable detailIcon;

    public ContactDetail(String detailType, String detail, Drawable detailIcon) {
        this.detailType = detailType;
        this.detail = detail;
        this.detailIcon = detailIcon;
    }

    public String getDetailType() {
        return detailType;
    }

    public void setDetailType(String detailType) {
        this.detailType = detailType;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Drawable getDetailIcon() {
        return detailIcon;
    }

    public void setDetailIcon(Drawable detailIcon) {
        this.detailIcon = detailIcon;
    }
}
