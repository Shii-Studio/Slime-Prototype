package com.shiistudio.slimeprototype;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;

import androidx.constraintlayout.widget.ConstraintLayout;

@SuppressWarnings("unused")
public class CanvasLayout extends ConstraintLayout {
    public static int collisionCount=0; //紀錄handleAllCollision時的總判斷次數
    private ArrayList<ImageSprite> spriteList = new ArrayList<>();
    InterceptTouchHandler interceptTouchHandler = new InterceptTouchHandler() {
        @Override
        public boolean onInterceptTouch(MotionEvent event) {
            return false;
        }
    };

    public CanvasLayout(Context context) {
        super(context);
    }

    public CanvasLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CanvasLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public interface InterceptTouchHandler{//在需要CanvasLayout擷取畫面觸碰事件時使用
        boolean onInterceptTouch(MotionEvent event);
    }

    public void setInterceptTouchHandler(InterceptTouchHandler interceptTouchHandler){
        this.interceptTouchHandler = interceptTouchHandler;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return interceptTouchHandler.onInterceptTouch(event);
    }

    public void addImageSprite(ImageSprite sprite){
        spriteList.add(sprite);
    }//在CanvasLayout中新增ImageSprite，用於碰撞事件的監聽

    public void removeImageSprite(ImageSprite sprite){
        spriteList.remove(sprite);
    }

    public void handleAllCollision(){//依照速度移動ImageSprite，同時監聽碰撞事件
        collisionCount = 0;
        for(ImageSprite sprite: spriteList){
            sprite.processSpeed();
            sprite.processCollision();
        }
    }

    public void pause(){//將所有動畫停止
        for(ImageSprite imageSprite: spriteList){
            imageSprite.stopAnimation();
        }
    }
}
