package com.leyou.user.mapper;

import com.leyou.user.pojo.User;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/**
 * @Author: 98050
 * @Time: 2018-10-21 18:40
 * @Feature:
 */
@Component
public interface UserMapper extends Mapper<User> {
}
