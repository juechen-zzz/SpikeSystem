package com.komorebi.service;

import com.komorebi.dao.SpikeUserDao;
import com.komorebi.exception.GlobalException;
import com.komorebi.pojo.SpikeUser;
import com.komorebi.redis.RedisService;
import com.komorebi.redis.SpikeUserKey;
import com.komorebi.result.CodeMsg;
import com.komorebi.util.MD5Util;
import com.komorebi.util.UUIDUtil;
import com.komorebi.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SpikeUserService {
    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    private SpikeUserDao spikeUserDao;

    @Autowired
    private RedisService redisService;

    public SpikeUser getById(long id) {
        // 对象缓存1：取缓存
        SpikeUser user = redisService.get(SpikeUserKey.getById, "" + id, SpikeUser.class);
        if (user != null) {
            return user;
        }
        // 对象缓存2：取数据库
        user = spikeUserDao.getById(id);
        if (user != null) {
            redisService.set(SpikeUserKey.getById, "" + id, user);
        }
        // 对象缓存3：结果输出
        return user;
    }

    public boolean updatePassword(String token, long id, String formPass) {
        // 取user
        SpikeUser user = getById(id);
        if (user == null) {throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);}
        // 更新数据库
        SpikeUser toBeUpdate = new SpikeUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass, user.getSalt()));
        spikeUserDao.update(toBeUpdate);
        // 处理缓存
        redisService.delete(SpikeUserKey.getById, "" + id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(SpikeUserKey.token, token, user);
        return true;
    }

    public SpikeUser getByToken(HttpServletResponse response, String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        SpikeUser user = redisService.get(SpikeUserKey.token, token, SpikeUser.class);
        // 延长有效期
        if (user != null) {
            addCookie(response, token, user);
        }
        return user;
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        // 判断手机号是否存在
        SpikeUser user = getById(Long.parseLong(mobile));
        if (user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 验证密码
        String dbPass = user.getPassword();
        String slatDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, slatDB);
        if (!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        // 分布式Session,生成cookie
        String token = UUIDUtil.uuid();
        addCookie(response, token, user);
        return true;
    }

    public void addCookie(HttpServletResponse response, String token, SpikeUser user) {
        redisService.set(SpikeUserKey.token, token, user);

        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
        cookie.setMaxAge(SpikeUserKey.token.expireSeconds());
        cookie.setPath("/");

        response.addCookie(cookie);
    }
}
