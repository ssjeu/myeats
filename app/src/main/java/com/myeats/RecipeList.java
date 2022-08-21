package com.myeats;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class RecipeList {
    public RecipeList() {}

    String name = "";
    String amount = "";
    ArrayList<String> main = new ArrayList<>();
    ArrayList<String> sub = new ArrayList<>();
    ArrayList<String> order = new ArrayList<>();
    Uri imgUrl;

    public RecipeList(String name, String amount, Uri imgUrl) {
        this.name = name;
        this.amount = amount;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getAmount() {
        return amount;
    }
    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Uri getImgUrl() { return imgUrl; }
    public void setImgUrl(Uri imgUrl) { this.imgUrl = imgUrl; }

    public ArrayList<String> getMain() { return main; }
    public void setMain(ArrayList<String> main) { this.main = main; }

    public ArrayList<String> getSub() { return sub;}
    public void setSub(ArrayList<String> sub) { this.sub = sub; }

    public ArrayList<String> getOrder() { return order; }
    public void setOrder(ArrayList<String> order) { this.order = order; }

    @Override
    public String toString() {
        return "RecipeItem{" +
                "name='" + name + '\'' +
                ", amount='" + amount + '\'' +
                '}';
    }
}
