package com.codivio.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codivio.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     */
    User findByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     */
    User findByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(@Param("username") String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(@Param("email") String email);
}