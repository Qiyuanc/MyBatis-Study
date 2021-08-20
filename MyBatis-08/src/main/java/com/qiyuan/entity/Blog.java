package com.qiyuan.entity;

import lombok.Data;

import java.util.Date;

/**
 * @ClassName Blog
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/17 23:17
 * @Version 1.0
 **/
@Data
public class Blog {
    // 数据库中是 varchar 型的
    private String id;
    private String title;
    private String author;
    // 使用 util 下的 Date
    private Date createTime;
    private int views;
}
