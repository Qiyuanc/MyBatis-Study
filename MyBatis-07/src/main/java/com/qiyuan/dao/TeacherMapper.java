package com.qiyuan.dao;

import com.qiyuan.entity.Teacher;

public interface TeacherMapper {
    // 获取指定 id 老师的信息 包含其所有的学生
    public Teacher getTeacher(int id);
    // 获取指定 id 老师的信息 包含其所有的学生，按照结果嵌套处理
    public Teacher getTeacher2(int id);
}
