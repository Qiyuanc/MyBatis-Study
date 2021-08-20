package com.qiyuan.utils;

import org.junit.Test;

import java.util.UUID;

/**
 * @ClassName IDUtils
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/17 23:27
 * @Version 1.0
 **/
public class IDUtils {
    public static String getId(){
        // 通过 java.util.UUID 获取随机 uid，将其中的 "-" 删掉
        return UUID.randomUUID().toString().replace("-","");
    }

    @Test
    public void Test(){
        System.out.println(IDUtils.getId());
    }
}
