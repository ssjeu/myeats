package com.myeats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;

import java.util.ArrayList;

public class RefrigerSelectFragment extends Fragment {
    MainActivity activity;
    ViewGroup rootView;

    ArrayList<String> checkList = new ArrayList<>();    //다음 화면으로 전달하는 ArrayList
    private Button selectBtn;

    //기타 체크박스 & 기타 EditText
    private EditText editMeatEtc;
    private CheckBox chMeatEtc;
    private CheckBox chVegetableEtc;
    private EditText editVegeEtc;
    private CheckBox chGrainEtc;
    private EditText editGrainEtc;
    private CheckBox chSeaEtc;
    private EditText editSeaEtc;
    private CheckBox chGuEtc;
    private EditText editGuEtc;
    private CheckBox chSauceEtc;
    private EditText editSauceEtc;

    ArrayList<CheckBox> chMeats = new ArrayList<>();    //육류
    ArrayList<CheckBox> chVeges = new ArrayList<>();    //채소류
    ArrayList<CheckBox> chGrains = new ArrayList<>();   //곡물, 콩류
    ArrayList<CheckBox> chSeas = new ArrayList<>();     //해산물류
    ArrayList<CheckBox> chGus = new ArrayList<>();      //가공,유제품
    ArrayList<CheckBox> chSauces = new ArrayList<>();   //조미료

    //shared
    private String shared = "PREFERENCE_FILE";
    final String sharedName = shared;
    SharedPreferences Pref;
    SharedPreferences.Editor editor;

    public RefrigerSelectFragment() {
        // Required empty public constructor
    }

    public void onBackKey() {
        activity.setOnKeyBackPressedListener(null);
        activity.onBackPressed();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_refriger_select, container, false);

        //모든 체크박스 아이디 참조
        getCheckBoxIds();

        selectBtn = rootView.findViewById(R.id.fillRefridgeBtn);
        //냉장고 채우기 버튼 클릭
        selectBtnClicked();

        //shared
        Pref = activity.getSharedPreferences(sharedName, Context.MODE_PRIVATE);
        editor = Pref.edit();

        return rootView;
    }   //onCreateView..


    public void getCheckBoxIds() {
        //chMeats 아이디 참조
        //체크박스를 chMeats.get(i)로 사용할 수 있음
        for (int i = 0; i < 4; i++) {
            int getId = getResources().getIdentifier("ch_meat_" + i, "id", activity.getPackageName());
            chMeats.add(rootView.findViewById(getId));
        }
        //육류 기타
        chMeatEtc = rootView.findViewById(R.id.ch_meat_etc);
        editMeatEtc = rootView.findViewById(R.id.editMeatEtc);
        chMeatEtc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chMeatEtc.isChecked()) {
                editMeatEtc.setEnabled(true);
            }
        });

        //채소류 아이디 참조
        //체크박스를 chVeges.get(i)로 사용할 수 있음
        for (int i = 0; i < 24; i++) {
            int getId = getResources().getIdentifier("ch_vege_" + i, "id", activity.getPackageName());
            chVeges.add(rootView.findViewById(getId));
        }
        //채소류 기타
        chVegetableEtc = rootView.findViewById(R.id.ch_vegetable_etc);
        editVegeEtc = rootView.findViewById(R.id.editVegetableEtc);
        chVegetableEtc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chVegetableEtc.isChecked()) {
                editVegeEtc.setEnabled(true);
            }
        });

        //곡물류 아이디 참조
        //체크박스를 chGrains.get(i)로 사용할 수 있음
        for (int i = 0; i < 14; i++) {
            int getId = getResources().getIdentifier("ch_grain_" + i, "id", activity.getPackageName());
            chGrains.add(rootView.findViewById(getId));
        }
        //곡물류 기타
        chGrainEtc = rootView.findViewById(R.id.ch_grain_etc);
        editGrainEtc = rootView.findViewById(R.id.editVegetableEtc);
        chGrainEtc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chGrainEtc.isChecked()) {
                editGrainEtc.setEnabled(true);
            }
        });

        //해산물류 아이디 참조
        //체크박스를 chSeas.get(i)로 사용할 수 있음
        for (int i = 0; i < 29; i++) {
            int getId = getResources().getIdentifier("ch_sea_" + i, "id", activity.getPackageName());
            chSeas.add(rootView.findViewById(getId));
        }
        //해산물류 기타
        chSeaEtc = rootView.findViewById(R.id.ch_sea_etc);
        editSeaEtc = rootView.findViewById(R.id.editSeaEtc);
        chSeaEtc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chSeaEtc.isChecked()) {
                editSeaEtc.setEnabled(true);
            }
        });

        //가공/유제품류 아이디 참조
        //체크박스를 chGus.get(i)로 사용할 수 있음
        for (int i = 0; i < 29; i++) {
            int getId = getResources().getIdentifier("ch_gu_" + i, "id", activity.getPackageName());
            chGus.add(rootView.findViewById(getId));
        }
        //가공/유제품류 기타
        chGuEtc = rootView.findViewById(R.id.ch_gu_etc);
        editGuEtc = rootView.findViewById(R.id.editGuEtc);
        chGuEtc.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (chGuEtc.isChecked()) {
                editGuEtc.setEnabled(true);
            }
        });

        //조미료류 아이디 참조
        //체크박스를 chGus.get(i)로 사용할 수 있음
        for (int i = 0; i < 24; i++) {
            int getId = getResources().getIdentifier("ch_sauce_" + i, "id", activity.getPackageName());
            chSauces.add(rootView.findViewById(getId));
        }
        //조미료 기타
        chSauceEtc = rootView.findViewById(R.id.ch_sauce_etc);
        editSauceEtc = rootView.findViewById(R.id.editSauceEtc);
        chSauceEtc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chSauceEtc.isChecked()) {
                    editSauceEtc.setEnabled(true);
                }
            }
        });
    }

    //'냉장고 채우기' 버튼 클릭 시. checkBox 체크 확인
    public void selectBtnClicked() {
        selectBtn.setOnClickListener(v -> {
            //육류
            for (int i = 0; i < 4; i++) {
                if (chMeats.get(i).isChecked()) {
                    checkList.add(chMeats.get(i).getText().toString());
                }
            }
            if (chMeatEtc.isChecked()) {
                if (editMeatEtc.getText().toString().replace(" ", "").length() > 0)
                    checkList.add(editMeatEtc.getText().toString());
            }
            //채소류
            for (int i = 0; i < 24; i++) {
                if (chVeges.get(i).isChecked()) {
                    checkList.add(chVeges.get(i).getText().toString());
                }
            }
            if (chVegetableEtc.isChecked()) {
                if (editVegeEtc.getText().toString().replace(" ", "").length() > 0)
                    checkList.add(editVegeEtc.getText().toString());
            }

            //곡물, 콩류
            for (int i = 0; i < 14; i++) {
                if (chGrains.get(i).isChecked())
                    checkList.add(chGrains.get(i).getText().toString());
            }
            if (chGrainEtc.isChecked()) {
                if (chGrainEtc.getText().toString().replace(" ", "").length() > 0)
                    checkList.add(editGrainEtc.getText().toString());
            }

            //해산물
            for (int i = 0; i < 29; i++) {
                if (chSeas.get(i).isChecked()) {
                    checkList.add(chSeas.get(i).getText().toString());
                }
            }
            if (chSeaEtc.isChecked()) {
                if (editSeaEtc.getText().toString().replace(" ", "").length() > 0)
                    checkList.add(editSeaEtc.getText().toString());
            }

            //가공, 유제품
            for (int i = 0; i < 10; i++) {
                if (chGus.get(i).isChecked()) {
                    checkList.add(chGus.get(i).getText().toString());
                }
            }
            if (chGuEtc.isChecked()) {
                if (editGuEtc.getText().toString().replace(" ", "").length() > 0)
                    checkList.add(editGuEtc.getText().toString());
            }

            //조미료
            for (int i = 0; i < 24; i++) {
                if (chSauces.get(i).isChecked()) {
                    checkList.add(chSauces.get(i).getText().toString());
                }
            }
            if (chSauceEtc.isChecked()) {
                if (editSauceEtc.getText().toString().replace(" ", "").length() > 0)
                    checkList.add(editSauceEtc.getText().toString());
            }

            /* 체크값 RefrigerFullFragment로 전달하기 위해 결과 설정 */
            Bundle result = new Bundle();
            result.putStringArrayList("bundleKey", checkList);
            getParentFragmentManager().setFragmentResult("requestKey", result);

            /* RefrigerFragment로 이동 */
            if (result.isEmpty()) {
                Toast.makeText(getActivity().getApplicationContext(), "재료를 선택해주세요!", Toast.LENGTH_SHORT).show();
            } else {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                RefrigerFragment refrigerFull = new RefrigerFragment();
                transaction.replace(R.id.main_layout, refrigerFull).commit();
            }

        });

    }

    @Override
    public void onPause() {
        super.onPause();

        if (!checkList.isEmpty()) {
            //체크된 게 있으면 SharedPreferences에 저장
            //clearShared();
            setStringArrayListPref(activity.getApplicationContext(), "checkedIngredients", checkList);
        }
    }

    //Shared 비우는 메소드
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
        String json = Pref.getString(key, null);
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