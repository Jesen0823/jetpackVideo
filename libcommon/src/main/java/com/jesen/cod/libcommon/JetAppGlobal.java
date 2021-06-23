package com.jesen.cod.libcommon;

import android.app.Application;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JetAppGlobal {
    private static Application mApplication;

    public static Application getApplication() {
        if (mApplication == null) {
            try {
                Method method = Class.forName("android.app.ActivityThread")
                        .getDeclaredMethod("currentApplication");
                mApplication = (Application) method.invoke(null, null);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }

        }
        return mApplication;
    }
}
