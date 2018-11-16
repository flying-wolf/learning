package com.machao.learning.degsinpattern.builder;

public class ManBuilder implements PersonBuilder {

    private Person person;

    @Override
    public void BuilderHead() {
        System.out.println("美国人头部 鼻子尖、长脸、蓝眼睛");
    }

    @Override
    public void BuilderBody() {
        System.out.println("美国人 长得比较高，块头大");
    }

    @Override
    public void BuilderFoot() {
        System.out.println("美国人 腿长。。。");
    }
}
