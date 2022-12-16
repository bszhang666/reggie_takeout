package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.EmailTask;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailTask emailTask;

    @PostMapping("sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession)
    {
        //获取邮箱号
        String email=user.getPhone();
        if(StringUtils.isNotEmpty(email))
        {
            //生成验证码
            String code=ValidateCodeUtils.generateValidateCode(4).toString();

            //调佣服务发送邮件/短信
            SimpleMailMessage message=new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("博硕科技验证码信息");
            message.setText("您的验证码为"+code);
            emailTask.sendAsync(message);
            //保存验证码到session中 为了之后验证
            httpSession.setAttribute(email,code);
            return R.success("成功发送邮件");
        }
        return R.error("邮件发送失败");
    }

    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession httpSession)
    {
        //map中获取phone和验证码
        String email=map.get("phone").toString();
        String code=map.get("code").toString();
        //seeion中获取验证码
        Object codeInSession=httpSession.getAttribute(email);
        //进行比对
        if(codeInSession!=null&&codeInSession.equals(code))
        {
            //成功

            //判断是否为新用户
            LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,email);
            User user=userService.getOne(lambdaQueryWrapper);//成功的话也返回
            if(user==null)//不成功的话新建之后返回
            {
                user=new User();
                user.setPhone(email);
                user.setStatus(1);
                userService.save(user);

            }
            //登陆成功之后把用户的id往session中放一份 为了让过滤器通过
            Long userId=user.getId();
            httpSession.setAttribute("user",userId);

            return R.success(user);
        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpServletRequest request)
    {
        request.removeAttribute("user");
        return R.success("登出成功");
    }
}
