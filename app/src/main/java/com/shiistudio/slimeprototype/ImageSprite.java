package com.shiistudio.slimeprototype;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

@SuppressWarnings({"WeakerAccess", "unused"})
public class ImageSprite{
    private Context context;
    private CanvasLayout parent;
    private ImageView imageView, hitbox;
    private float direction, speed;
    private ArrayList<OnCollisionListener> collisionList = new ArrayList<>();

    private boolean hasAnimation = false;
    private Animation animation;
    private Handler handler = new Handler();

    public boolean pauseCollision = false;

    //初始化物件
    private void ini(CanvasLayout viewGroup, Context context){
        this.context = context;
        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        hitbox = new ImageView(context);
        hitbox.setScaleType(ImageView.ScaleType.FIT_XY);
        hitbox.setId(View.generateViewId());
        parent = viewGroup;
        parent.addImageSprite(this);
    }

    public ImageSprite(CanvasLayout viewGroup, Context context) {
        ini(viewGroup, context);
        viewGroup.addView(imageView);
        viewGroup.addView(hitbox);
    }

    public ImageSprite(int width, int height, CanvasLayout viewGroup, Context context) {
        ini(viewGroup, context);
        viewGroup.addView(imageView, width, height);
        viewGroup.addView(hitbox, width, height);
    }

    public int getId(){
        return hitbox.getId();
    }

    //設定圖片
    public void setDrawable(Drawable drawable){
        imageView.setImageDrawable(drawable);
    }

    public void setDrawableById(int id){
        imageView.setImageDrawable(context.getDrawable(id));
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setOnTouchListener(View.OnTouchListener onTouchListener){
        //設定碰觸事件
        //注意:在OnTouch事件中傳入的view是只有hitbox，而不是整個ImageSprite，建議避免直接操作傳入的view參數
        hitbox.setOnTouchListener(onTouchListener);
    }

    public void requestLayout(){
        //要求父容器重畫這個View
        imageView.requestLayout();
        hitbox.requestLayout();
    }

    public float getX(){
        return hitbox.getX();
    }

    public float getY(){
        return hitbox.getY();
    }

    public void setPos(float x, float y){
        hitbox.setX(x);
        hitbox.setY(y);
        rePosImage();
    }

    public void changePos(float xDelta, float yDelta){
        hitbox.setX(hitbox.getX() + xDelta);
        hitbox.setY(hitbox.getY() + yDelta);
        rePosImage();
    }

    public void moveToMotion(MotionEvent event){
        setPos(event.getRawX()-hitbox.getWidth()/2.f, event.getRawY()-hitbox.getHeight()/2.f);
    }

    public void setElevation(float elevation){
        imageView.setElevation(elevation);
        hitbox.setElevation(elevation);
    }

    //調整圖片顯示的方法
    public int getImageWidth(){
        return imageView.getLayoutParams().width;
    }

    public int getImageHeight(){
        return imageView.getLayoutParams().height;
    }

    public void setImageSize(int width, int height){
        imageView.getLayoutParams().width = width;
        imageView.getLayoutParams().height = height;
        rePosImage();
    }

    public void changeImageSize(int widthDelta, int heightDelta){
        imageView.getLayoutParams().width += widthDelta;
        imageView.getLayoutParams().height += heightDelta;
        rePosImage();
    }

    public void setScaleX(float scaleX){
        imageView.setScaleX(scaleX);
    }

    public void setScaleY(float scaleY){
        imageView.setScaleY(scaleY);
    }

    private void rePosImage(){
        //重設圖片的位置(相對於hitbox)
        imageView.setX(hitbox.getX() - (imageView.getLayoutParams().width - hitbox.getLayoutParams().width)/2.f);
        imageView.setY(hitbox.getY() - (imageView.getLayoutParams().height - hitbox.getLayoutParams().height)/2.f);
    }

    public float getDirection(){
        return direction;
    }

    public void setDirection(float newDirection){
        this.direction = newDirection;
        this.direction %= 360;
        if(this.direction < 0) this.direction += 360;
    }

    public void changeDirection(float DirectionDelta){
        this.direction += DirectionDelta;
        this.direction %= 360;
        if(this.direction < 0) this.direction +=360;
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void changeSpeed(float speedDelta){
        this.speed += speedDelta;
    }

    public void processSpeed(){
        if(speed!=0) {
            changePos((float)Math.cos(getDirection()/180*Math.PI)*speed, -(float)Math.sin(getDirection()/180*Math.PI)*speed);
        }
    }

    //調整判定區域相關的方法
    public void showHitbox(boolean show){
        if(show){
            hitbox.setImageDrawable(context.getDrawable(R.drawable.ic_hitbox));
        }else {
            hitbox.setImageResource(android.R.color.transparent);
        }
    }

    public int getHitboxWidth(){
        return hitbox.getLayoutParams().width;
    }

    public int getHitboxheight(){
        return hitbox.getLayoutParams().height;
    }

    public void setHitboxSize(int width, int height){
        hitbox.getLayoutParams().width = width;
        hitbox.getLayoutParams().height = height;
    }

    public void changeHitboxSize(int widthDelta, int heightDelta){
        hitbox.getLayoutParams().width += widthDelta;
        hitbox.getLayoutParams().height += heightDelta;
    }

    //處理hitbox發生碰撞時的行為
    public abstract static class OnCollisionListener{
        private ImageSprite target;

        public OnCollisionListener(ImageSprite target){
            this.target = target;
        }

        private void processCollision(ImageSprite self){
            if(self.checkCollision(target)){
                onCollision(self, target);
            }
        }

        abstract public void onCollision(ImageSprite self, ImageSprite target);
    }

    public boolean checkCollision(ImageSprite target){
        Rect r1 = new Rect(),r2 = new Rect();
        hitbox.getHitRect(r1);
        target.hitbox.getHitRect(r2);
        return r1.intersect(r2);
    }

    public void addOnCollisionListener(OnCollisionListener listener){
        //注意:請勿在onResume方法內新增listener，以避免memory leak
        collisionList.add(listener);
    }

    public void processCollision(){
        if(!pauseCollision) {
            for (OnCollisionListener listener : collisionList) {
                CanvasLayout.collisionCount += 1;
                listener.processCollision(this);
            }
        }
    }

    public void removeAllCollision(){
        collisionList.clear();
    }

    public static class Animation{
        private ArrayList<Frame> frameList = new ArrayList<>();
        private int currentFrameIndex = 0;

        private class Frame{
            private int drawableId;
            private long duration;
            public Frame(int drawableId, long duration) {
                this.drawableId = drawableId;
                this.duration = duration;
            }
        }

        public Animation addFrame(int drawableId, long duration){
            frameList.add(new Frame(drawableId, duration));
            return this;
        }

        public int getFrameId(){
            return frameList.get(currentFrameIndex).drawableId;
        }

        public long getDuration(){
            return frameList.get(currentFrameIndex).duration;
        }

        public void next(){
            currentFrameIndex++;
            if(currentFrameIndex == frameList.size()) currentFrameIndex = 0;
        }
    }

    public void setAnimation(Animation newAnimation){
        if(hasAnimation) handler.removeCallbacks(runAnimation);
        hasAnimation = true;
        animation = newAnimation;
        handler.post(runAnimation);
    }

    public void stopAnimation(){
        if(hasAnimation) {
            hasAnimation = false;
            handler.removeCallbacks(runAnimation);
        }
    }

    private Runnable runAnimation = new Runnable() {
        @Override
        public void run() {
            setDrawableById(animation.getFrameId());
            handler.postDelayed(runAnimation, animation.getDuration());
            animation.next();
        }
    };

    public void destroy(){
        parent.removeImageSprite(this);
        parent.removeView(this.imageView);
        parent.removeView(this.hitbox);
    }
}
