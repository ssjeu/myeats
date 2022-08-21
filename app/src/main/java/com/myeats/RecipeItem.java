package com.myeats;


import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;

public class RecipeItem extends Activity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference recipe;
    private static final String TAG = "RecipeItem";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_item);

        TextView name = (TextView)findViewById(R.id.recipeName);
        TextView amount = (TextView)findViewById(R.id.recipeAmount);
        TextView main = (TextView)findViewById(R.id.recipeMainList);
        TextView sub = (TextView)findViewById(R.id.recipeSubList);
        TextView order = (TextView)findViewById((R.id.recipeOrder));
        ImageView img = (ImageView)findViewById(R.id.recipeImage);


        Intent intent = getIntent(); // 보내온 Intent를 얻는다
        name.setText(intent.getStringExtra("name"));
        amount.setText(intent.getStringExtra("amount"));
        Glide.with(this).load((Uri) intent.getParcelableExtra("imgUrl")).into(img);

        /* RecipeList에서 보낸 intent 정보(name)로 해당 레시피 상세정보 db에서 불러옴 */
        recipe = db.collection("recipe");
        DocumentReference docRef = recipe.document(intent.getStringExtra("name"));
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                RecipeList recipeItem = documentSnapshot.toObject(RecipeList.class);
                main.setText(recipeItem.main.toString().replace("[","").replace("]",""));
                sub.setText(recipeItem.sub.toString().replace("[","").replace("]",""));
                String orderList="";
                for (int i = 0; i < recipeItem.order.size(); ++i) {
                    orderList += (i+1) + ". " + recipeItem.order.get(i).toString()+"\n\n";
                }
                order.setText(orderList);
            }
        });



    }
}

