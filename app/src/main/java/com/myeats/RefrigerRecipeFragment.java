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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;

public class RefrigerRecipeFragment extends Fragment {
    RefrigerRecipeAdapter adapter = new RefrigerRecipeAdapter();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference recipe = db.collection("recipe");
    private OnBackPressedCallback callback;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference imagesRef = storageRef.child("recipeImage");
    StorageReference imgUrlRef;

    MainActivity activity;
    private ViewGroup rootView;
    ArrayList<String> resultFull = new ArrayList<>();

    Query query;
    String name;
    String amount;
    Uri imgUrl;

    public RefrigerRecipeFragment() {
        // Required empty public constructor
    }

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
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_refriger_recipe, container, false);
        ListView listView = (ListView) rootView.findViewById(R.id.listView);

        /* RefrigerSelectFragment 에서 선택된 재료 불러오기 */
        super.onCreate(savedInstanceState);
        getParentFragmentManager().setFragmentResultListener("requestKey2", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                resultFull = bundle.getStringArrayList("bundleKey2");
                query = recipe.whereArrayContainsAny("main", resultFull);
                query.get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        Log.d(TAG, document.getId() + " => " + document.getData());
                                        /* 선택된 재료로 레시피 보여주기 */
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

            }
        });

        listView.setAdapter(adapter);

        /* 레시피 상세 페이지 연결 */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RecipeList item = (RecipeList) adapter.getItem(position);
                Intent intent = new Intent(getActivity(), RecipeItem.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

                intent.putExtra("name",item.name);
                intent.putExtra("amount", item.amount);
                intent.putExtra("imgUrl", item.imgUrl);
                startActivity(intent);
            }
        });
        return rootView;
    }

    class RefrigerRecipeAdapter extends BaseAdapter{
        ArrayList<RecipeList> items = new ArrayList<RecipeList>();

        public int getCount() { return items.size(); }
        public void addItem(RecipeList item) { items.add(item); }

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
            RecipeListView view = null;
            if(convertView == null){
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