package com.qiyuan.entity;

import lombok.Data;

import java.util.List;

/**
 * @ClassName Teacher
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/16 23:10
 * @Version 1.0
 **/
@Data
public class Teacher {
    private int id;
    private String name;
    // 一个老师对应多个学生
    private List<Student> students;
}
