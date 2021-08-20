package com.qiyuan.dao;

import com.qiyuan.entity.Blog;

import java.util.List;
import java.util.Map;

public interface BlogMapper {
    // 插入一条博客信息
    int addBlog(Blog blog);
    // 根据条件查询博客
    List<Blog> queryBlogIf(Map map);
    // 根据条件查询博客，选其一
    List<Blog> queryBlogChoose(Map map);
    // 更新博客信息
    int updateBlog(Map map);
    // 查询 1 2 3 号博客的记录
    List<Blog> queryBlogForeach(Map map);
}
