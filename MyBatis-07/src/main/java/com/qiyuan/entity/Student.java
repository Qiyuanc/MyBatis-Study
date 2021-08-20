package com.qiyuan.entity;

import lombok.Data;

/**
 * @ClassName Student
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/16 23:09
 * @Version 1.0
 **/
@Data
public class Student {
    private int id;
    private String name;
    // 学生关联老师
    private int tid;
}
