package com.example.simpleweb.controller;

import com.example.simpleweb.serv.ServC;
import com.example.simpleweb.serv.ServD;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MController {

    private final ServC servC;
    private final ServD servD;

    public MController(ServC servC, ServD servD, HttpServletRequest request) {
        this.servC = servC;
        this.servD = servD;
    }

    @RequestMapping("a")
    public String a(){
         servC.getC();
         servD.getD();
         return "a";
    }

    @GetMapping("b")
    public String b(){
        servC.getC();
        servD.getD();
        return "b";
    }

    @PostMapping("ca")
    public String ca(){
        servC.getC();
        servD.getD();
        return "ca";
    }

    @RequestMapping(value = "download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void download(HttpServletResponse response) throws IOException {
        File file = new File("D://temp//通讯录批量导入模板.xlsx");


        //response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setContentType("application/ms-excel;charset=UTF-8");
        response.setHeader("Content-disposition", "attachment;filename=111.xlsx");
        OutputStream outputStream = response.getOutputStream();

        FileInputStream fs = new FileInputStream(file);

        int len;
        byte[] bytes = new byte[1024];
        while ((len = fs.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        fs.close();
        outputStream.flush();
        outputStream.close();
    }

    private void cc(){
        System.out.println("cc");
    }
}
