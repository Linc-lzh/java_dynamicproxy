package com.zhouyu;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class Main {

    public static void main(String[] args) {
        final UserService target = new UserService();

        Enhancer enhancer = new Enhancer();
        enhancer.setUseCache(false);
        enhancer.setSuperclass(UserService.class);
        enhancer.setCallback(new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

                if (method.getName().equals("test")) {
                    System.out.println("before...");

                    //methodProxy.invoke(o, objects);  // test()  Method
                    //methodProxy.invokeSuper(target, objects);  // CGLIB$test$4() Method
                    method.invoke(target, objects);

                    System.out.println("after...");
                    return null;
                }

                return method.invoke(target, objects);
            }
        });

        UserService userService = (UserService) enhancer.create();
        userService.test();


        // FastClass
        // UserService FastClass代理类
        // UserService代理类 FastClass代理类

    }
}
