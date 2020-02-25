package com.shiistudio.slimeprototype;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        setUiListener();

        Button btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), HomeActivity.class);
                startActivity(intent);
            }
        });

        Button btn_exit = findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btn_clearSave = findViewById(R.id.btn_clearSave);
        btn_clearSave.setTextColor(Color.GRAY);
        btn_clearSave.setClickable(false);

        Button btn_credit = findViewById(R.id.btn_credit);
        btn_credit.setTextColor(Color.GRAY);
        btn_credit.setClickable(false);

    }

    @Override
    protected void onResume() {
        super.onResume();
        hideUi();
    }

    private void setUiListener() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideUi();
                    }
                }, 3000);
            }
        });
    }

    private void hideUi(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
}
