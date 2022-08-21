package com.myeats;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Random;

public class MainFragment extends Fragment {
    MainActivity activity;
    private OnBackPressedCallback callback;
    private ConstraintLayout monthlyFood;
    private TextView monthlyRandomFdText;   //이 달의 음식 이름 TextView
    private String monthlyRandomFdData;
    private String recipeCntntsNum;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference recipe = db.collection("recipe");
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("recipeImage");
    StorageReference imgUrlRef;

    //이번 달
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM");
    Date date = new Date();
    String month = simpleDateFormat.format(date);

    //shared
    private String shared = "PREFERENCE_FILE";
    final String sharedName = shared;
    SharedPreferences sharedPreferences;

    //냉장고 추천
    ImageView refrigerMainImageView;
    Uri refrigerRandom;
    ArrayList<String> refrigerSelect = new ArrayList<>();
    Query query;
    Query query2;
    String name = "";
    ArrayList<RecipeList> randomItem = new ArrayList<>();
    String amount = "";
    Uri imgUrl;
    ArrayList<Uri> refrigerUrl = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                requireActivity().onBackPressed(); // 앱 종료
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);

        //오늘은 ~ 어때요 TextView
        monthlyRandomFdText = rootView.findViewById(R.id.monthlyRandomFd);

        TextView textView = rootView.findViewById(R.id.monthRecipeText);
        textView.setText(month+"월의 추천 레시피!");
        //2016~2019년 이 달의 음식 중 랜덤으로 하나 음식 이름 보여주기
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> contents = new ArrayList<>(getMonthlyFoodCntntsNumData()); //컨텐츠 넘버 목록들

                //아래 메소드를 호출하여 XML data를 파싱해서 String 객체로 음식명 얻어오기
                monthlyRandomFdData = getMonthlyFoodNameData(contents);

                //NullPointerException 방지
                if(activity == null)
                    return;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        monthlyRandomFdText.setText(monthlyRandomFdData);   //TextView에 문자열 data 출력
                    }
                });
            }
        }).start();

        //컨텐츠 번호 받고 이달의 음식 소개페이지 액티비티로 이동
        monthlyFood = (ConstraintLayout) rootView.findViewById(R.id.monthlyFood);
        monthlyFood.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MonthlyFoodActivity.class);
            intent.putExtra("cntntsNo", recipeCntntsNum);
            startActivity(intent);
        });


        //냉장고 재료 해당 레시피 랜덤으로 보여주기
        sharedPreferences = activity.getSharedPreferences(sharedName, Context.MODE_PRIVATE);
        refrigerSelect = new ArrayList<>(getStringArrayPref(getContext(), "checkedIngredients"));
        refrigerMainImageView = rootView.findViewById(R.id.refrigerMainImageView);

        if(!refrigerSelect.isEmpty()) {
            query = recipe.whereArrayContainsAny("main", refrigerSelect);
            query2 = recipe.whereNotIn("main", Arrays.asList(refrigerSelect));
            query2.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            refrigerMainImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                            refrigerMainImageView.setImageResource(R.drawable.ic_app_empty);
                        }
                    });

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            name = document.getString("name");
                            imgUrlRef = imagesRef.child(name + ".jpg");
                            imgUrlRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        name = document.getString("name");
                                        amount = document.getString("amount");
                                        imgUrl = task.getResult();
                                        randomItem.add(new RecipeList(name, amount, imgUrl));
                                        /* 재료에 해당되는 레시피 url ArrayList(refrigerUrl)에 추가*/
                                        refrigerUrl.add(imgUrl);
                                        int rand = (int) (Math.random() * refrigerUrl.size() + 1);
                                        refrigerRandom = refrigerUrl.get(rand - 1);
                                        GlideApp.with(rootView)
                                                .load(refrigerRandom)
                                                .error(R.mipmap.ic_launcher)
                                                .centerCrop()
                                                .into(refrigerMainImageView);

                                        RecipeList item = randomItem.get(rand - 1);
                                        /* 이미지 클릭 레시피 정보 페이지 출력 */
                                        refrigerMainImageView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent(getActivity(), RecipeItem.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);

                                                intent.putExtra("name", item.name);
                                                intent.putExtra("amount", item.amount);
                                                intent.putExtra("imgUrl", item.imgUrl);
                                                startActivity(intent);
                                            }
                                        });
                                    } else
                                        Log.d(TAG, "img: ", task.getException());
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                }
            });
        }else {
            refrigerMainImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            refrigerMainImageView.setImageResource(R.drawable.ic_app_empty);
        }
        return rootView;
    }

    //매개변수로 저장한 key 값을 입력해주고 받을 ArrayList<String>에 담아내는 메소드
    public ArrayList<String> getStringArrayPref(Context context, String key) {
        String json = sharedPreferences.getString(key, null);
        ArrayList<String> urls = new ArrayList<>();
        if (json != null) {
            try {
                JSONArray a = new JSONArray(json);
                for (int i = 0; i < a.length(); i++) {
                    String url = a.optString(i);
                    urls.add(url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return urls;
    }

    //ArrayList에 이 달의 컨텐츠 넘버 String 으로 저장
    ArrayList<String> getMonthlyFoodCntntsNumData() {
        ArrayList<String> cntntsNoList = new ArrayList<>(); //이달의 음식 컨텐츠 번호 목록

        //API 제공하는 연도인 2016~2019까지 중 랜덤 연도 구하기
        int maxYear = 2019;
        int minYear = 2016;
        Random random = new Random();
        int randomYear = random.nextInt(maxYear - minYear + 1) + minYear;

        String apiKey = "20211006BT3JO5YXXJQ5LV8J5RQQ";
        String year = String.valueOf(randomYear);
        //달은 전역변수로
        String queryUrl1 = "http://api.nongsaro.go.kr/service/monthFd/monthNewFdLst?apiKey=" + apiKey
                + "&thisYear=" + year + "&thisMonth=" + month;

        try {
            URL url = new URL(queryUrl1); //문자열로 된 요청 url을 URL 객체로 생성
            InputStream is = url.openStream(); //url 위치로 입력 스트림 연결

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8"));   //inputstream으로부터 xml 입력받기

            String tag;
            xpp.next();
            int eventType = xpp.getEventType();

            //xml 문서 읽기
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); //태그 이름 얻어오기

                        if (tag.equals("item")) ;
                        else if (tag.equals("cntntsNo")) {
                            //레시피 구분 코드가 290001 이면
                            //컨텐츠 번호를 ArrayList에 저장. (나중에 랜덤으로 보여주기)
                            xpp.next();
                            cntntsNoList.add(xpp.getText());
                        }
                        else if (tag.equals("fdSeCode")) {
                            xpp.next();
                            if(xpp.getText().equals("290002") || xpp.getText().equals("290003"))
                                cntntsNoList.remove((cntntsNoList.size() - 1));
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;
                    case XmlPullParser.END_TAG:
                        tag = xpp.getName();
                        if (tag.equals("item"));
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cntntsNoList;
    }


    //**************xml파싱 메소드 (음식명(fdNm) 가져오기)
    //필요한 요청변수 : apiKey, cntntsNo(레시피 컨텐츠 번호)
    String getMonthlyFoodNameData(ArrayList<String> list) {
        StringBuffer buffer = new StringBuffer();


        //cntntsNoList 중 랜덤으로 하나
        Collections.shuffle(list);
        String cntntsNo = list.get(0);
        recipeCntntsNum = cntntsNo;

        String apiKey = "20211006BT3JO5YXXJQ5LV8J5RQQ";
        String queryUrl2 = "http://api.nongsaro.go.kr/service/monthFd/monthNewFdDtl?apiKey="
                +apiKey+"&cntntsNo="+cntntsNo;

        try {
            URL url = new URL(queryUrl2);
            InputStream is = url.openStream();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new InputStreamReader(is, "UTF-8")); //inputstream으로부터 xml 입력받기

            String tag;
            xpp.next();
            int eventType = xpp.getEventType();

            //xml 문서 읽기
            while(eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tag = xpp.getName(); //태그 이름 얻어오기

                        if(tag.equals("item"));
                        else if(tag.equals("fdNm")) {
                            //음식 명
                            xpp.next();
                            buffer.append(xpp.getText());
                        }
                        break;
                    case XmlPullParser.TEXT:
                        break;

                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xpp.next();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return buffer.toString();
    }


}