package com.example.user.lapitchatapp;

import android.app.ProgressDialog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StautsActivity extends AppCompatActivity {

    private Toolbar mToolBar;

    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Firebase
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    //progress
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stauts);

        //Firebase
        mCurrentUser= FirebaseAuth.getInstance().getCurrentUser();
        String current_uid=mCurrentUser.getUid();

        mStatusDatabase= FirebaseDatabase.getInstance().getReference().child("users").child(current_uid);


        mToolBar=(Toolbar)findViewById(R.id.status_appBar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String status_value=getIntent().getStringExtra("status_value");



        mStatus=(TextInputLayout)findViewById(R.id.status_input);
        mSaveBtn=(Button)findViewById(R.id.save_btn);

        mStatus.getEditText().setText(status_value);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //progress
                mProgress=new ProgressDialog(StautsActivity.this);
                mProgress.setTitle("saving Changes");
                mProgress.setMessage("Please wait while we save changes");
                mProgress.show();
                String status=mStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            mProgress.dismiss();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });

                mStatusDatabase.child("status").setValue(status);
            }
        });


    }
}
