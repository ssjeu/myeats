package com.myeats;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    RefrigerFragment refrigerFragment;  //네비게이션 셀렉티드 안에다 new Fragment 하면 계속 만들어져서 메모리 낭비됨
    MainFragment mainFragment;
    MyPageFragment mypageFragment;
    RecipeListFragment recipeListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFragment = new MainFragment();
        refrigerFragment = new RefrigerFragment();
        mypageFragment = new MyPageFragment();
        recipeListFragment = new RecipeListFragment();
        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        //시작하자마자 프래그먼트 띄운 상태로 만들고 싶을 때
        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mainFragment).commit();

        //바텀 네비게이션뷰 안의 아이템 설정
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    //item 클릭 시 id 값 가져와 FrameLayout에 fragment.xml 띄우기
                    case R.id.item_home:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mainFragment).commit();
                        break;
                    case R.id.item_refrigerator:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, refrigerFragment).commit();
                        break;
                    case R.id.item_community:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, recipeListFragment).commit();
                        break;
                    case R.id.item_mypage:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_layout, mypageFragment).commit();
                        break;
                }
                return true;
            }
        });
    }

    // 뒤로가기 버튼을 뺏어올 리스너 등록
    public interface onKeyBackPressedListener {
        void onBackKey();
    }
    private onKeyBackPressedListener mOnKeyBackPressedListener;
    public void setOnKeyBackPressedListener(onKeyBackPressedListener listener){
        mOnKeyBackPressedListener = listener;
    }

    public void onBackPressed(){
        if (mOnKeyBackPressedListener != null){
            mOnKeyBackPressedListener.onBackKey();
        } else {
            // 쌓인 BackStack 여부에 따라
            if(getSupportFragmentManager().getBackStackEntryCount() ==0){

            }else {
                super.onBackPressed();
            }
        }
    }

}