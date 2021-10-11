package com.example.securitydemo.custom;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class MyUserDetails {

    private String userName;

    private List<String> roles;

    private List<String> permissions;

    public String getUserName() {
        return userName;
    }

    public MyUserDetails setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public MyUserDetails setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public MyUserDetails setPermissions(List<String> permissions) {
        this.permissions = permissions;
        return this;
    }

    public List<GrantedAuthority> toSecurityAuthority(){
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }
}
