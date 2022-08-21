package com.myeats;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.storage.StorageReference;

//하나의 아이템을 위한 뷰



//LinearLayout을 상속해서 만들면 레이아웃 인플레이션 후 붙여줄 수 있음
public class RecipeListView extends LinearLayout {
    TextView textView;
    TextView textView2;
    ImageView imageView;

    public RecipeListView(Context context) {
        super(context);
        init(context);
    }

    public RecipeListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    //초기화를 위한 메소드
    private void init(Context context) {
        //xml 레이아웃 파일을 인플레이션 해서 붙여주는 역할
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //this : 리니어레이아웃을 상속받았으므로 singer_item.xml에 있는 최상위 레이아웃이 리니어니까
        //인플레이션 해서 여기다 붙일 수 있음
        inflater.inflate(R.layout.recipe_list, this, true);

        //인플레이션 후에는 singer_item.xml에 있는 뷰를 참조할 수 있음
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    //레이아웃의 첫 번째 텍스트 뷰에 글자가 보이게 됨
    public void setName(String name) {
        textView.setText(name);
    }

    public void setAmount(String amount) {
        textView2.setText(amount);
    }

    public void setImg(Uri imgUrl) {
        GlideApp.with(imageView)
                .load(imgUrl)
//                .placeholder(R.drawable.ic_app)
                .error(R.mipmap.ic_launcher)
                .centerCrop()
                .into(imageView);
    }
}
