package com.leyou.auth.service;

import com.leyou.auth.client.UserClient;
import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.properties.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Author: 98050
 * @Time: 2018-10-23 22:47
 * @Feature:
 */
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtProperties properties;

    /**
     * 用户授权
     *
     * @param username
     * @param password
     * @return
     */
    public String authentication(String username, String password) {

        try {
            //1.调用微服务查询用户信息
            User user = this.userClient.queryUser(username, password);
            //2.查询结果为空，则直接返回null
            if (user == null) {
                return null;
            }
            //3.查询结果不为空，则生成token
            return JwtUtils.generateToken(new UserInfo(user.getId(), user.getUsername()),
                    properties.getPrivateKey(), properties.getExpire());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 用户验证
     *
     * @param token
     * @return
     */
    @GetMapping("verify")
    public UserInfo verifyUser(
            @CookieValue("LY_TOKEN") String token,
            HttpServletRequest request,
            HttpServletResponse response) throws Exception{

        //1.从token中解析token信息,公钥解密
        UserInfo userInfo = JwtUtils.getInfoFromToken(token, this.properties.getPublicKey());
        //2.解析成功要重新刷新token, 私钥加密
        token = JwtUtils.generateToken(userInfo, this.properties.getPrivateKey(), this.properties.getExpire());
        //3.更新Cookie中的token
        CookieUtils.setCookie(request, response, this.properties.getCookieName(), token, this.properties.getCookieMaxAge());
        //4.解析成功返回用户信息
        //5.出现异常,相应401
        return userInfo;


    }
}
