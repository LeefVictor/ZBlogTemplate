package com.example.securitydemo.controller;

import com.example.securitydemo.custom.MyUserDetails;
import com.google.common.collect.ImmutableList;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.Principal;

@RestController
@RefreshScope
public class DemoController {


    @RequestMapping("myLogin")
    public String login(HttpSession session,
                        String username,
                        String password
            , HttpServletResponse response) throws IOException {
        if (username.equals("user1") && password.equals("123456")) {
            MyUserDetails userDetails = new MyUserDetails();
            userDetails.setUserName(username)
                    .setRoles(ImmutableList.of("READ", "WRITE"))
                    .setPermissions(ImmutableList.of("菜单1", "表单2", "购买功能"));

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, password, userDetails.toSecurityAuthority());
            authentication.setDetails(userDetails);
            SecurityContextHolder.getContext().setAuthentication(authentication);


            session.setAttribute("userDetail", userDetails);

            //如果是访问其他页面因没有登录而跳转到登录页的， 将这个链接返回， 让前端跳转一遍
            Object origin = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");//如果为空， 就跳会首页
            return "success, " + (origin == null ? "/" : ((DefaultSavedRequest) origin).getRequestURL());
        }

        return "fail";
    }

    @RequestMapping("myLogout")
    public String logout(HttpServletRequest request) throws ServletException {
        //直接清除掉session和securityContext的内容
        request.logout();
        return "success";
    }


    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping("write")
    public String write(Principal principal) {
        return "Done";
    }


    //可以传入参数， hasPermission(#id, 'p14') 然后自定义校验逻辑，
    @PreAuthorize("hasPermission(#myUserDetails, '购买功能1')")
    @RequestMapping("buy")
    public String buy(@SessionAttribute("userDetail") MyUserDetails myUserDetails) {
        return "Done";
    }



}
