package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/**
 * 文件上传和下载
 */

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file)
    {
        //log.info(file.toString());
        //file是临时文件 要储存
        String originalFileName=file.getOriginalFilename();
        //使用uuid重新生成文件名,防止文件重读造成文件覆盖
        String originalFileNameLast=originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName= UUID.randomUUID().toString();

        try {
            String newFileName=basePath+"/"+fileName+originalFileNameLast;
            file.transferTo(new File(newFileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName+originalFileNameLast);
    }

    @GetMapping("/download")
    public void download(HttpServletResponse response, String name)
    {
        try {
            //输入流 读取文件内容
            FileInputStream fileInputStream=new FileInputStream(new File(basePath+"/"+name));
            //输出流 协会浏览器
            ServletOutputStream outputStream= response.getOutputStream();


            response.setContentType("image/jpeg");
            int len=0;

            byte [] bytes=new byte[1024];

            while((len=fileInputStream.read(bytes))!=-1)
            {
                outputStream.write(bytes,0,len);
                outputStream.flush();
            }
            fileInputStream.close();
            outputStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
