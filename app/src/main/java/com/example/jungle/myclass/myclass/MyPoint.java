package com.example.jungle.myclass.myclass;

/**
 * Created by jungle on 2016/5/13.
 */
public class MyPoint {
    private int left=0;
    private int right=0;

    public MyPoint() {
    }

    public MyPoint(int left, int right) {
        this.left = left;
        this.right = right;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }
}
