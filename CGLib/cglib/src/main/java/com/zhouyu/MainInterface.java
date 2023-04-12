package com.zhouyu;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class MainInterface {

    public static void main(final String[] args) {

        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(UserInterface.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                System.out.println("切面逻辑...");
                return null;
            }
        });

        UserInterface userInterface = (UserInterface) enhancer.create();
        userInterface.test();

    }
}
