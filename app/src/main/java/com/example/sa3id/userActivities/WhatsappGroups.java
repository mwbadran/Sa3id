package com.example.sa3id.userActivities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sa3id.BaseActivity;
import com.example.sa3id.R;

public class WhatsappGroups extends BaseActivity {
    Button btnMainCommunity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_whatsapp_groups);
        btnMainCommunity = findViewById(R.id.btnMainCommunity);
        btnMainCommunity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = "https://chat.whatsapp.com/DBIyUPTvp4a6ScRIN2kAmx";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_whatsapp_groups;
    }


}