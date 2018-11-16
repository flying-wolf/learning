package com.machao.learning.degsinpattern.builder;

public class JPBuilder implements PersonBuilder {
    @Override
    public void BuilderHead() {
        System.out.println("日本人 圆脸");
    }

    @Override
    public void BuilderBody() {
        System.out.println("日本人 比较矮");

    }

    @Override
    public void BuilderFoot() {
        System.out.println("日本人 腿短");

    }
}
