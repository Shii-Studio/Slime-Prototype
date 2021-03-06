package com.shiistudio.slimeprototype;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

@SuppressWarnings("FieldCanBeLocal")
public class HomeActivity extends Activity implements View.OnClickListener, View.OnTouchListener {
    private int frame = 0;
    static final int FRAME_RATE = 50;
    final private Handler handler = new Handler();

    private CanvasLayout base;
    private ImageSprite is_slime,bound_top,bound_down,bound_left,bound_right,platedFood;
    private TextView tv_debug,tv_debug2;
    private ImageView iv_food;
    private Button btn_save,btn_feed;
    private ConstraintLayout CL_foodCabinet;
    private boolean foodPlated = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setUiListener();

        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);
        btn_feed = findViewById(R.id.btn_feed);
        btn_feed.setOnClickListener(this);

        CL_foodCabinet = findViewById(R.id.CL_foodCabinet);
        CL_foodCabinet.setElevation(9.f);//button的高度是2dp~8dp
        CL_foodCabinet.setVisibility(View.INVISIBLE);

        tv_debug = findViewById(R.id.tv_debug);
        tv_debug2 = findViewById(R.id.tv_debug2);
        iv_food = findViewById(R.id.iv_food);
        base = findViewById(R.id.background);

        handler.postDelayed(initialize,20);
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideUi();
        handler.postDelayed(mainLoop, 50);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(mainLoop);
    }

    @Override
    public void onClick(View v) {//處理家中所有按鈕的事件
        switch (v.getId()){
            case R.id.btn_save:
                finish();
                break;
            case R.id.btn_feed:
                if(CL_foodCabinet.getVisibility() == View.VISIBLE) {
                    CL_foodCabinet.setVisibility(View.INVISIBLE);
                }else{
                    CL_foodCabinet.setVisibility(View.VISIBLE);
                }
                CL_foodCabinet.requestLayout();
                break;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        if(v.getId() == is_slime.getId()){//史萊姆被觸碰時的事件
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    tv_debug2.setText("slime touched");
                    slimeState.addState(grabbed, false);
                    is_slime.setSpeed(0);
                    break;
                case MotionEvent.ACTION_MOVE:
                    tv_debug2.setText("slime moved");
                    is_slime.moveToMotion(event);
                    break;
                case MotionEvent.ACTION_UP:
                    tv_debug2.setText("slime up");
                    slimeState.removeState();
                    break;
            }
            return true;
        }else if(v.getId() == platedFood.getId()){//拿取食物的方法
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    tv_debug2.setText("platedFood touched");
                    if(foodPlated){
                        platedFood.moveToMotion(event);
                    }else if(CL_foodCabinet.getVisibility() == View.VISIBLE){
                        foodPlated = true;
                        platedFood.setDrawable(iv_food.getDrawable());
                    }else{
                        return false;
                    }
                    platedFood.setElevation(10.f);
                    break;
                case MotionEvent.ACTION_MOVE:
                    tv_debug2.setText("platedFood moved");
                    platedFood.moveToMotion(event);
                    break;
                case MotionEvent.ACTION_UP:
                    tv_debug2.setText("platedFood untouched");
                    if(platedFood.checkCollision(bound_top)){
                        if(CL_foodCabinet.getVisibility() == View.VISIBLE) {
                            platedFood.removeDrawable();
                            platedFood.setPos(iv_food.getX(), iv_food.getY());
                            foodPlated = false;
                        }else {
                            platedFood.setPos(platedFood.getX(), bound_top.getHitboxheight());
                        }
                    }
                    platedFood.setElevation(2.f);
                    break;
            }
            return true;
        }
        return false;
    }

    private void setUiListener() {//在navigation bar出現3秒後再次隱藏
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        hideUi();
                    }
                }, 3000);
            }
        });
    }

    private void hideUi(){//隱藏上方的status bar和下方的navigation bar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private Runnable initialize = new Runnable() {
        @Override
        public void run() {//初始化執行緒，只執行一次
            CL_foodCabinet.getLayoutParams().height = base.getHeight()/3;
            platedFood = new ImageSprite(iv_food.getWidth(),iv_food.getHeight(),base,HomeActivity.this);
            platedFood.setPos(iv_food.getX(),iv_food.getY());
            platedFood.removeDrawable();
            platedFood.setOnTouchListener(HomeActivity.this);

            bound_top = new ImageSprite(base, HomeActivity.this);
            bound_down = new ImageSprite(base, HomeActivity.this);
            bound_left = new ImageSprite(base, HomeActivity.this);
            bound_right = new ImageSprite(base, HomeActivity.this);
            bound_top.setHitboxSize(base.getWidth(),base.getHeight()/3);
            bound_top.setPos(0,0);
            bound_down.setHitboxSize(base.getWidth(),100);
            bound_down.setPos(0, base.getHeight());
            bound_left.setHitboxSize(100, base.getHeight());
            bound_left.setPos(-100,0);
            bound_right.setHitboxSize(100, base.getHeight());
            bound_right.setPos(base.getWidth(),0);

            is_slime = new ImageSprite(150,150, base,HomeActivity.this);
            //is_slime.setDrawableById(R.drawable.slime);
            is_slime.setImageSize(421,300);
            is_slime.setOnTouchListener(HomeActivity.this);

            is_slime.addOnCollisionListener(new ImageSprite.OnCollisionListener(bound_top) {
                @Override
                public void onCollision(ImageSprite self, ImageSprite target) {
                    is_slime.setPos(is_slime.getX(), base.getHeight()/3);
                    if(is_slime.getDirection()>0 && is_slime.getDirection()<180) {
                        is_slime.setDirection(-is_slime.getDirection());
                    }
                }
            });
            is_slime.addOnCollisionListener(new ImageSprite.OnCollisionListener(bound_down) {
                @Override
                public void onCollision(ImageSprite self, ImageSprite target) {
                    is_slime.setPos(is_slime.getX(), base.getHeight()-is_slime.getHitboxheight());
                    if(is_slime.getDirection()>180) {
                        is_slime.setDirection(-is_slime.getDirection());
                    }
                }
            });
            is_slime.addOnCollisionListener(new ImageSprite.OnCollisionListener(bound_left) {
                @Override
                public void onCollision(ImageSprite self, ImageSprite target) {
                    is_slime.setPos(0, is_slime.getY());
                    if(is_slime.getDirection()>90 && is_slime.getDirection()<270) {
                        is_slime.setDirection(180-is_slime.getDirection());
                    }
                }
            });
            is_slime.addOnCollisionListener(new ImageSprite.OnCollisionListener(bound_right) {
                @Override
                public void onCollision(ImageSprite self, ImageSprite target) {
                    is_slime.setPos(base.getWidth()-is_slime.getHitboxWidth(), is_slime.getY());
                    if(is_slime.getDirection()<90 || is_slime.getDirection()>270) {
                        is_slime.setDirection(180-is_slime.getDirection());
                    }
                }
            });

            is_slime.setPos(base.getWidth()/2,base.getHeight()/2);
            is_slime.setAnimation(SlimeAnimation.SLIMEGIRL_NORMAL);

            slimeState.addState(idle,false);
        }
    };

    long maxDelay=0;
    private Runnable mainLoop = new Runnable() {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {//遊戲主執行緒
            long startRunTime = SystemClock.currentThreadTimeMillis();

            base.handleAllCollision();
            tv_debug.setText("slime direction: "+is_slime.getDirection()+
                    "\nslime speed: "+is_slime.getSpeed()+
                    "\nframe: "+frame+
                    "\ncollision count: "+CanvasLayout.collisionCount+
                    "\nmax delay: "+maxDelay);

            slimeState.run();

            if(is_slime.getDirection()>90 && is_slime.getDirection()<270){
                is_slime.setScaleX(-1.f);
            }else {
                is_slime.setScaleX(1.f);
            }

            frame += 1;
            if(frame >= FRAME_RATE) {
                frame = 0;
                maxDelay = 0;
            }
            if(SystemClock.currentThreadTimeMillis()-startRunTime > maxDelay) maxDelay = SystemClock.currentThreadTimeMillis()-startRunTime;

            long nextRunTime = startRunTime + 1000/FRAME_RATE - SystemClock.currentThreadTimeMillis();
            if(nextRunTime > 0) handler.postDelayed(mainLoop, 1000/FRAME_RATE);
            else handler.post(mainLoop);
        }
    };

    private StateMachine slimeState = new StateMachine(){
        //史萊姆狀態機
        @Override
        public void run() {
            super.run();
            if(frame < FRAME_RATE/2){//史萊姆抖動
                is_slime.changeImageSize(+1,-1);
            }else if(frame < FRAME_RATE){
                is_slime.changeImageSize(-1,+1);
            }
        }
    };

    private StateMachine.State idle = new StateMachine.State() {//閒置狀態
        private long moveTime = SystemClock.currentThreadTimeMillis() + 1000;
        boolean isMoving = false;
        @Override
        protected void run() {
            if(SystemClock.currentThreadTimeMillis() >= moveTime){
                if(isMoving){
                    is_slime.setSpeed(0);
                    isMoving = false;
                    moveTime = SystemClock.currentThreadTimeMillis() + 1000;
                }else {
                    is_slime.setDirection((float)(Math.random()*360));
                    is_slime.setSpeed(5);
                    isMoving = true;
                    moveTime = SystemClock.currentThreadTimeMillis() + 500;
                }
            }
        }
    };

    private StateMachine.State grabbed = new StateMachine.State() {//抓取狀態
        @Override
        protected void run() {}

        @Override
        protected void Create() {
            is_slime.pauseCollision = true;
            is_slime.setElevation(10.f);
        }

        @Override
        protected void Destroy() {
            is_slime.pauseCollision = false;
            is_slime.setElevation(1.f);
        }
    };
}
