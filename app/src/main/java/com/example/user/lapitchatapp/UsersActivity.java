package com.example.user.lapitchatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private RecyclerView mUsersList;

    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolBar=(Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("users");


        mUsersList=(RecyclerView)findViewById(R.id.users_list);

        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Users,usersViewHolder> firebaseRecyclerAdapter=new FirebaseRecyclerAdapter<Users, usersViewHolder>(
                Users.class,
                R.layout.users_single_layout,
                usersViewHolder.class,
                mUsersDatabase


        ) {
            @Override
            protected void populateViewHolder(usersViewHolder viewHolder, Users users, int position) {
                viewHolder.setName(users.getName());
                viewHolder.setStatus(users.getStatus());
                viewHolder.setUserImage(users.getThumb_image(),getApplicationContext());

                final String user_id=getRef(position).getKey();

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);

                        startActivity(profileIntent);

                    }
                });


            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class usersViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public usersViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setName(String name){
            TextView userNameview=(TextView)mView.findViewById(R.id.user_single_name);
            userNameview.setText(name);

        }

        public void setStatus(String status){
            TextView userStatusview=(TextView)mView.findViewById(R.id.user_single_status);
            userStatusview.setText(status);
        }

        public void setUserImage(String thumb_image, Context ctx){

            CircleImageView userImageView=(CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.avatar).into(userImageView);
        }
    }
}
