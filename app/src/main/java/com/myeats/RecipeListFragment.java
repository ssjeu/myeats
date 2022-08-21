package com.myeats;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Objects;

public class RecipeListFragment extends Fragment {
    MainActivity activity;
    private RecipeListAdapter adapter;
    private OnBackPressedCallback callback;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ViewGroup rootView;
    private CollectionReference recipe = db.collection("recipe");

    int recipeNum = 0;
    String[] recipeId = new String[1000];
    String name;
    String amount;
    Uri imgUrl;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("recipeImage");
    StorageReference imgUrlRef;

    private final ArrayList<RecipeList> recipeListArray = new ArrayList<>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
        callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //인플레이션 결과인 rootView는 이 프래그먼트 안에 들어가는 최상위 레이아웃
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_recipe_list, container, false);

        SearchView searchView = rootView.findViewById(R.id.searchView);
        ListView listView = (ListView) rootView.findViewById(R.id.listView);

        //리스트 뷰에다 어댑터 객체를 만든 후 설정
        adapter = new RecipeListAdapter();
        recipe.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    /* 'recipe' collection 검색 */
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            /* collection 내 document 확인 for문 */
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                try {
                                    /* recipe id 받아와 해당 document내 field 값 접근 후 adapter 추가 */
                                    recipeId[recipeNum] = document.getId();
                                    DocumentReference docRef = recipe.document(recipeId[recipeNum]);
                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    name = document.getString("name");
                                                    amount = document.getString("amount");
                                                    imgUrlRef = imagesRef.child(name + ".jpg");

                                                    imgUrlRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Uri> task) {
                                                            if (task.isSuccessful()) {
                                                                imgUrl = task.getResult();
                                                                adapter.addItem(new RecipeList(document.getString("name"), document.getString("amount"), imgUrl));
                                                                adapter.notifyDataSetChanged();

                                                                //레시피 검색할 수 있게 레시피 객체 배열
                                                                recipeListArray.add(new RecipeList(document.getString("name"), document.getString("amount"), imgUrl));
                                                            } else
                                                                Log.d(TAG, "img: ", task.getException());
                                                        }
                                                    });
                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                            }
                                        }
                                    });
                                } catch (NullPointerException e) {
                                    Log.d(TAG, "not hello ....", task.getException());
                                }
                                recipeNum++;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
        listView.setAdapter(adapter);

        //searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //입력받은 문자열 처리
                //검색 버튼 누를 때 호출
               /* adapter.items.clear();

                adapter.search(query);

                adapter.notifyDataSetChanged();

                listView.setAdapter(adapter);*/
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //검색창에서 글자 변경이 일어날 때마다 호출
                //어댑터에 재설정
                if (!newText.isEmpty())
                    search(newText);

                return true;
            }
        });



        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecipeList item = (RecipeList) adapter.getItem(position);
                Toast.makeText(getActivity().getApplicationContext(), "선택 : " + item.getName(), Toast.LENGTH_SHORT).show();

                // 상세정보 화면으로 이동하기(인텐트 날리기)
                // 1. 다음화면을 만든다
                // 2. AndroidManifest.xml 에 화면을 등록한다
                // 3. Intent 객체를 생성하여 날린다
//                Intent intent = new Intent(
//                        getApplicationContext(), // 현재화면의 제어권자
//                        RecipeItem.class); // 다음넘어갈 화면
                Intent intent = new Intent(getActivity(), RecipeItem.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                // intent 객체에 데이터를 실어서 보내기
                // 리스트뷰 클릭시 인텐트 (Intent) 생성하고 position 값을 이용하여 인텐트로 넘길값들을 넘긴다
                intent.putExtra("name", item.name);
                intent.putExtra("amount", item.amount);
                intent.putExtra("imgUrl", item.imgUrl);

                startActivity(intent);
            }
        });

        return rootView;
    }

    //searchview 검색 기능 (trim() --> 공백 제외)
    private void search(String query) {
        //recipeListArray.get(i);을 안 써도 되나?
        adapter.items.clear();
        for (int i = 0; i < recipeListArray.size(); i++) {
            RecipeList item = recipeListArray.get(i);
            if (item.getName().trim().contains(query.replace(" ", ""))) {
                //어댑터에 item 추가
                adapter.addItem(item);
            }
        }
        adapter.notifyDataSetChanged();
    }

    //객체를 만들기 위한 어댑터 클래스를 먼저 정의
    //기존 어댑터를 상속해서 필요한 만큼 코드 추가
    class RecipeListAdapter extends BaseAdapter {
        //데이터 관리
        ArrayList<RecipeList> items = new ArrayList<>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(RecipeList item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //안드로이드에선 뷰는 context를 객체로 받음
            RecipeListView view;
            if (convertView == null) {
                view = new RecipeListView(getActivity().getApplicationContext());
            } else {
                view = (RecipeListView) convertView;
            }

            RecipeList item = items.get(position);
            view.setName(item.getName());
            view.setAmount(item.getAmount());
            view.setImg(item.getImgUrl());

            return view;
        }
    }
}