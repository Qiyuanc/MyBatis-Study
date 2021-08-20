package com.qiyuan.dao;

import com.qiyuan.entity.Student;

import java.util.List;

public interface StudentMapper {
    // 获取所有学生及其对应的老师信息
    public List<Student> getStudent();

    // 获取所有学生及其对应的老师信息
    public List<Student> getStudent2();
}
