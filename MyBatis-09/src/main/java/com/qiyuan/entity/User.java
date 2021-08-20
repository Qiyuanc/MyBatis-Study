package com.qiyuan.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName User
 * @Description TODO
 * @Author Qiyuan
 * @Date 2021/8/19 18:06
 * @Version 1.0
 **/
@Data
@AllArgsConstructor
public class User implements Serializable {
    private int id;
    private String name;
    private String pwd;
}
