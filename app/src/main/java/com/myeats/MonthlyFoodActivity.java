package com.myeats;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class MonthlyFoodActivity extends AppCompatActivity {
    private String getCntntsNo; //레시피 컨텐츠 번호
    private TextView nameTextView;
    private TextView ingrTextView;
    private TextView recipeTextView;
    private ImageView monthFdImg;
    private StringBuffer realImgName;
    private String imageUrl;    //대표이미지 최종 주소

    StringBuffer imgSrcData = new StringBuffer("http://www.nongsaro.go.kr/cms_contents/855/");

    MonthlyRcmdRecipe mRecipeObj = new MonthlyRcmdRecipe();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_food);

        getCntntsNo = getIntent().getStringExtra("cntntsNo");

        nameTextView = findViewById(R.id.name); //음식 명
        ingrTextView = findViewById(R.id.ingredient);   //재료
        recipeTextView = findViewById(R.id.recipe); //레시피
        monthFdImg = findViewById(R.id.monthFdImg); //이미지 뷰

        new Thread(new Runnable() {
            @Override
            public void run() {
                getMonthlyRdFdRecipe(); //api에서 정보가져와서 객체 생성

                //mRecipeObj.getImgName() 해서 21번째 인덱스 이후로 다 지워서 mRecipeObj의 imgName에 저장
                realImgName = new StringBuffer(mRecipeObj.getImgName());
                realImgName.delete(21, realImgName.length());
                mRecipeObj.setImgName(realImgName.toString());
                //주소+이미지 파일 명 imageUrl에 저장
                imageUrl = imgSrcData.toString() + mRecipeObj.getImgName();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nameTextView.setText(mRecipeObj.getFoodName());
                        ingrTextView.setText(mRecipeObj.getIngredient());
                        recipeTextView.setText(mRecipeObj.getRecipe());

                        //이미지뷰로 출력
                        Glide.with(getApplicationContext()).load(imageUrl).into(monthFdImg);
                    }
                });
            }
        }).start();

    }

    //이 달의 레시피 API Parsing하는 Code
    void getMonthlyRdFdRecipe() {

        String apiKey = "20211006BT3JO5YXXJQ5LV8J5RQQ";
        String queryUrl = "http://api.nongsaro.go.kr/service/monthFd/monthNewFdDtl?apiKey="
                + apiKey + "&cntntsNo=" + getCntntsNo;

        try {
            URL url = new URL(queryUrl);
            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));

            String tag;
            xpp.next();
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                    case XmlPullParser.TEXT:

                    case XmlPullParser.END_TAG:
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); //태그 이름

                        if (tag.equals("item")) ;
                        else if (tag.equals("fdNm")) {
                            //객체에 setFoodName
                            xpp.next();
                            mRecipeObj.setFoodName(xpp.getText());
                        } else if (tag.equals("matrlInfo")) {
                            //주재료, 부재료, 양념 앞 "▶" 표시 지울 것
                            xpp.next();
                            mRecipeObj.setIngredient(xpp.getText());
                        } else if (tag.equals("ckngMthInfo")) {
                            xpp.next();
                            mRecipeObj.setRecipe(xpp.getText());
                        } else if (tag.equals("rtnStreFileNm")) {
                            //209008_MF_BIMG_01.jpg   g가 index 20. index 21부터 지워야됨
                            //209010_MF_BIMG_01.jpg
                            xpp.next();
                            mRecipeObj.setImgName(xpp.getText());
                        }
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}