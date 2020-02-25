package com.shiistudio.slimeprototype;

import java.util.Stack;

@SuppressWarnings({"WeakerAccess", "unused"})
public class StateMachine {
    public static abstract class State{
        //用於實作state的介面
        protected abstract void run();
        protected void Create(){}
        protected void pause(){}
        protected void resume(){}
        protected void Destroy(){}
    }

    private Stack<State> stateStack = new Stack<>();

    public void addState(State state, boolean isReplacing){
        if(!stateStack.empty()){
            if(isReplacing){
                stateStack.pop().Destroy();
            }else {
                stateStack.peek().pause();
            }
        }
        state.Create();
        stateStack.push(state);
    }

    public void removeState(){
        stateStack.pop().Destroy();
        if(!stateStack.empty()){
            stateStack.peek().resume();
        }
    }

    public void run(){
        stateStack.peek().run();
    }

    public boolean isEmpty(){
        return stateStack.empty();
    }
}
