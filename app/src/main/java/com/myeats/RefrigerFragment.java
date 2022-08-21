package com.myeats;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class RefrigerFragment extends Fragment {

    private ViewGroup rootView;
    private TextView itemSelected;
    private Button recipeBtn;
    private Button selectBtn;
    MainActivity activity;
    ArrayList<String> result = new ArrayList<>();
    private OnBackPressedCallback callback;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String shared = "PREFERENCE_FILE";
    final String sharedName = shared;

    ArrayList<String> arr;

    public RefrigerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        //getActivity로 이 프래그먼트가 동작하는 액티비티 참조(여기서는 MainActivity)
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

        //액티비티 더 이상 참조 안 됨
        activity = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //프래그먼트를 생성하며 넘겨준 값들이 있다면 여기서 변수에 넣어준다
        //여기서 UI는 초기화 할 수 없다.
        //UI는 onCreateView() 함수에서 작업.
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //인플레이션 결과인 rootView는 이 프래그먼트 안에 들어가는 최상위 레이아웃
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_refriger, container, false);

        //rootView가 최상위 레이아웃이므로 rootView.find~ 로 써야 함
        itemSelected = rootView.findViewById(R.id.itemSelected);

        recipeBtn = rootView.findViewById(R.id.showRecipeBtn);
        selectBtn = rootView.findViewById(R.id.selectBtn);
        recipeBtn.setVisibility(View.GONE);

        //저장된 것이 있으면 보여줌
        sharedPreferences = activity.getSharedPreferences(sharedName, Context.MODE_PRIVATE);

        arr = new ArrayList<>(getStringArrayPref(getContext(), "checkedIngredients"));

        if (!arr.isEmpty()) {
            recipeBtn.setVisibility(View.VISIBLE);
            selectBtn.setBackgroundResource(R.drawable.lightgrey_round);
            itemSelected.setText(arr.toString().replace("[", "").replace("]", "").replace(", ", "\n"));
        }

        //만약에 shared에 저장된 게 있으면 result에 shared값 저장
        //shared에 저장된 게 없으면 result에 번들에서 가져온 값 저장
        if(!arr.isEmpty()) {
            result = arr;
            Log.e("저장된 게 있으면 result 데이터 : " , result.toString());
        } else {
            /* RefrigerSelectFragment 로부터 데이터(재료 선택값) 받기 위해 리스너 호출 */
            getParentFragmentManager().setFragmentResultListener("requestKey", this, new FragmentResultListener() {
                @Override
                public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle bundle) {
                    result = bundle.getStringArrayList("bundleKey");

                    if (!result.isEmpty()) {
                        recipeBtn.setVisibility(View.VISIBLE);
                        selectBtn.setBackgroundResource(R.drawable.lightgrey_round);
                        itemSelected.setText(result.toString().replace("[", "").replace("]", "").replace(", ", "\n"));
                    }
                }
            });
        }


        /* 레시피 보기 */
        recipeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* RefrigerRecipeFragment에서 선택된 재료로 레시피 보기 위해 값 전달 */
                Bundle resultFull = new Bundle();
                resultFull.putStringArrayList("bundleKey2", result);
                getParentFragmentManager().setFragmentResult("requestKey2", resultFull);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                RefrigerRecipeFragment recipeResult = new RefrigerRecipeFragment();
                transaction.replace(R.id.main_layout, recipeResult).commit();

            }
        });

        selectBtn.setOnClickListener(v -> {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            RefrigerSelectFragment selectFragment = new RefrigerSelectFragment();
            transaction.replace(R.id.main_layout, selectFragment).addToBackStack(null).commit();
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    //Shared clear
    public void clearShared() {
        editor.clear();
        editor.commit();
    }

    //매개변수로 shared에 저장할 key 값과 ArrayList<String> 타입의 List를 넣어주면 shared에 저장하는 메소드
    public void setStringArrayListPref(Context context, String key, ArrayList<String> values) {

        JSONArray a = new JSONArray();
        for (int i = 0; i < values.size(); i++) {
            a.put(values.get(i));
        }
        if (!values.isEmpty()) {
            editor.putString(key, a.toString());
        } else {
            editor.putString(key, null);
        }
        editor.commit();
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
}