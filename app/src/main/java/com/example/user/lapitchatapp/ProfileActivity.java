package com.example.user.lapitchatapp;

import android.app.ProgressDialog;
import java.text.DateFormat;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus,mProfileFriendsCount;
    private Button mProfileSendReq,mProfileDeclineReq;

    private DatabaseReference mUserDatabase;

    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgressDialog;

    private String mcurrent_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id=getIntent().getStringExtra("user_id");

        mRootRef=FirebaseDatabase.getInstance().getReference();
        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        mFriendReqDatabase=FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");

        mCurrent_user= FirebaseAuth.getInstance().getCurrentUser();


        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_displayName);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileFriendsCount=(TextView)findViewById(R.id.profile_totalFriends);
        mProfileSendReq=(Button) findViewById(R.id.profile_sendReq);
        mProfileDeclineReq=(Button) findViewById(R.id.profile_declineReq);

        mcurrent_state="not_friends";

        mProfileDeclineReq.setVisibility(View.INVISIBLE);
        mProfileDeclineReq.setEnabled(false);

        mProgressDialog=new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("please wait while we load user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String display_name=dataSnapshot.child("name").getValue().toString();
                String status=dataSnapshot.child("status").getValue().toString();
                String image=dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.avatar).into(mProfileImage);

                if(mCurrent_user.getUid().equals(user_id)){

                    mProfileDeclineReq.setEnabled(false);
                    mProfileDeclineReq.setVisibility(View.INVISIBLE);

                    mProfileSendReq.setEnabled(false);
                    mProfileSendReq.setVisibility(View.INVISIBLE);
                }


                //--------------Friend Req Feature------------

                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){

                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if(req_type.equals("received")){

                                mcurrent_state="req_received";
                                mProfileSendReq.setText("Accept Friend Request");

                                mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                mProfileDeclineReq.setEnabled(false);

                            }
                            else if (req_type.equals("sent")){
                                mcurrent_state="req_sent";
                                mProfileSendReq.setText("Cancel Friend Request");
                                mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                mProfileDeclineReq.setEnabled(false);

                            }
                            mProgressDialog.dismiss();
                        }
                        else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){

                                        mcurrent_state="friends";
                                        mProfileSendReq.setText(" unFriend This Person");
                                        mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                        mProfileDeclineReq.setEnabled(false);

                                    }
                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });
                        }



                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendReq.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {


                mProfileSendReq.setEnabled(false);

                //-------------------NOT FRIENDS--------------------


                if(mcurrent_state.equals("not_friends")) {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();

                    String newNotificationId=newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();


                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");


                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/"+user_id+"/"+newNotificationId,notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {


                            if(databaseError!= null){
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request", Toast.LENGTH_LONG).show();

                            }

                            mProfileSendReq.setEnabled(true);

                            mcurrent_state="req_sent";
                            mProfileSendReq.setText("Cancel Friend Request");



                        }
                    });

                }

                //-------------------CAncel Req--------------------

                if(mcurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReq.setEnabled(true);
                                    mcurrent_state="not_friends";
                                    mProfileSendReq.setText("Send Friend Request");

                                    mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                    mProfileDeclineReq.setEnabled(false);

                                }
                            });
                        }
                    });
                }


                //-------------------Request receive


                if(mcurrent_state.equals("req_received")){

                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate);



                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id,null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null){

                                mProfileSendReq.setEnabled(true);
                                mcurrent_state="friends";
                                mProfileSendReq.setText("Unfriend this person");

                                mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                mProfileDeclineReq.setEnabled(false);
                            }
                            else{

                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }


                        }
                    });




                }


                //-------------------UNFRIENDS-----------------//


                if(mcurrent_state.equals("friends")){

                    Map unfriendMap=new HashMap();

                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id,null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError==null){


                                mcurrent_state="not_friends";
                                mProfileSendReq.setText("send friend request");

                                mProfileDeclineReq.setVisibility(View.INVISIBLE);
                                mProfileDeclineReq.setEnabled(false);
                            }
                            else{

                                String error=databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }

                            mProfileSendReq.setEnabled(true);

                        }
                    });

                }



            }
        });

    }
}
