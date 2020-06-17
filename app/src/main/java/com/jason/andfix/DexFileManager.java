package com.jason.andfix;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Enumeration;

import dalvik.system.DexFile;

/**
 * Description:DexFileManager
 *
 * @author 陈宝阳
 * @create 2020/6/16 10:36
 */
public class DexFileManager {

  private Context context;

  private static final DexFileManager INSTANCE = new DexFileManager();

  private DexFileManager(){}

  public static DexFileManager getInstance() {
    return INSTANCE;
  }

  public void setContext(Context context) {
    this.context = context.getApplicationContext();
  }

  /**
   * 加载dex文件
   * @param path
   */
  public void loadDexFile(String path) {
    File file = new File(path);
    loadDexFile(file);
  }

  public void loadDexFile(File file) {
    try {
      //dalvik虚拟机的dex对象
      DexFile dexFile = DexFile.loadDex(file.getAbsolutePath(),
          new File(context.getCacheDir(), "opt").getAbsolutePath(), Context.MODE_PRIVATE);
      //下一步  得到class   ----取出修复好的Method
      Enumeration<String> entry= dexFile.entries();
      while (entry.hasMoreElements()) {
        //拿到全类名
        String className=entry.nextElement();
        //Class.forName(className);   拿到修复的dex的类
        Class clazz = dexFile.loadClass(className, context.getClassLoader());
        if (clazz != null) {
          fixClazz(clazz);
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void fixClazz(Class fixClazz) {
    //修复好的class
    Method[] methods = fixClazz.getDeclaredMethods();
    for (Method rightMethod : methods) {
      MethodReplace replace = rightMethod.getAnnotation(MethodReplace.class);
      if(replace == null) {
        continue;
      }

      String wrongClazzName = replace.clazz();
      String wrongMethodName = replace.method();

      try{
        Class clazz = Class.forName(wrongClazzName);
        Method wrongMethod = clazz.getDeclaredMethod(wrongMethodName, rightMethod.getParameterTypes());
        if (Build.VERSION.SDK_INT <= 19) {
          replaceDalvik(Build.VERSION.SDK_INT ,wrongMethod, rightMethod);
        }else {
          replaceArt(wrongMethod, rightMethod);
        }
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  private native  void replaceArt(Method wrongMethod, Method rightMethod);

  public native void replaceDalvik(int sdk, Method wrongMethod, Method rightMethod);

}

