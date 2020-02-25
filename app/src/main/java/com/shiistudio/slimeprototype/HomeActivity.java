package com.shiistudio.slimeprototype;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class HomeActivity extends Activity {
    private int frame = 0;
    static final int FRAME_RATE = 50;
    final private Handler handler = new Handler();

    private CanvasLayout base;
    private ImageSprite is_slime,bound_top,bound_down,bound_left,bound_right;
    private TextView tv_debug;
    private Button btn_save,btn_feed;
    private HorizontalScrollView HSV_foodCabinet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setUiListener();

        btn_save = findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        HSV_foodCabinet = findViewById(R.id.HSV_foodCabinet);
        HSV_foodCabinet.setElevation(9.f);//button的高度是2dp~8dp
        btn_feed = findViewById(R.id.btn_feed);
        btn_feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(HSV_foodCabinet.getVisibility() == View.VISIBLE) {
                    HSV_foodCabinet.setVisibility(View.INVISIBLE);
                }else{
                    HSV_foodCabinet.setVisibility(View.VISIBLE);
                }
                HSV_foodCabinet.requestLayout();
            }
        });

        tv_debug = findViewById(R.id.tv_debug);
        base = findViewById(R.id.background);

        handler.post(initialize);
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

    private void setUiListener() {
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

    private void hideUi(){
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private Runnable initialize = new Runnable() {
        @Override
        public void run() {
            HSV_foodCabinet.getLayoutParams().height = base.getHeight()/3;

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
            is_slime.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent e) {
                    v.performClick();
                    switch (e.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            slimeState.addState(grabbed, false);
                            is_slime.setSpeed(0);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            is_slime.setPos(e.getRawX()-v.getWidth()/2.f, e.getRawY()-v.getHeight());
                            break;
                        case MotionEvent.ACTION_UP:
                            slimeState.removeState();
                            break;
                    }
                    return true;
                }
            });

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
            is_slime.setAnimation(slimeGirlIdleNormal);

            slimeState.addState(idle,false);
        }
    };

    long maxDelay=0;
    private Runnable mainLoop = new Runnable() {
        @Override
        public void run() {
            long startRunTime = SystemClock.currentThreadTimeMillis();

            if(frame < FRAME_RATE/2){
                is_slime.changeImageSize(+1,-1);
            }else if(frame < FRAME_RATE){
                is_slime.changeImageSize(-1,+1);
            }

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

    private StateMachine slimeState = new StateMachine();

    private StateMachine.State idle = new StateMachine.State() {
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

    private StateMachine.State grabbed = new StateMachine.State() {
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

    private ImageSprite.Animation slimeGirlIdleNormal = new ImageSprite.Animation()
            .addFrame(R.drawable.slimegirl_normal_135 , 200)
            .addFrame(R.drawable.slimegirl_normal_24 , 200)
            .addFrame(R.drawable.slimegirl_normal_135 , 200)
            .addFrame(R.drawable.slimegirl_normal_24 , 200)
            .addFrame(R.drawable.slimegirl_normal_135 , 200)
            .addFrame(R.drawable.slimegirl_normal_6 , 200);
}
