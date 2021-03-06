package com.teacher.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageInfo;
import com.teacher.common.Const;
import com.teacher.common.ServerResponse;
import com.teacher.entity.Grade;
import com.teacher.entity.User;
import com.teacher.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;

@Controller
public class GradeController {
    @Autowired
    GradeService gradeService;

    @PostMapping("/grade")
    public String add(Grade grade, @RequestParam("uploadFile") MultipartFile uploadFile, HttpSession session, HttpServletRequest request, HttpServletResponse response){
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        grade.setUserId(user.getId());
        if (grade.getFileName() == null){
            grade.setFileName("");
        }
        if (uploadFile.getSize() > 0 ) {
            String filename = uploadFile.getOriginalFilename();
            String leftPath = session.getServletContext().getRealPath("/file");
            File path =new File(leftPath);
            if ( !path.exists() && path.isAbsolute() ){
                path.mkdir();
            }
            File file = new File(leftPath, filename);
            if ( file.exists() ){
                  file.delete();
            }
            try {
                uploadFile.transferTo(file);
                grade.setFileName(filename);
                String json = JSON.toJSONString(gradeService.add(grade));
                request.setAttribute("serverResponse",json);
                return "main/grade_add";
            } catch (IOException e) {
                e.printStackTrace();
                String json = JSON.toJSONString(ServerResponse.createError("提交失败！"));
                request.setAttribute("serverResponse",json);
                return "main/grade_add";
            }
        }
        String json = JSON.toJSONString(gradeService.add(grade));
        request.setAttribute("serverResponse",json);
        return "main/grade_add";
    }

    @PostMapping("/gradedelete/{id}")
    @ResponseBody
    public ServerResponse delete(@PathVariable("id") Long id, HttpSession session, HttpServletRequest request){
        String fileName = gradeService.findById(id).getData().getFileName();
        ServerResponse serverResponse = gradeService.delete(id);
        //重复代码可改为方法
        if (serverResponse.isSuccess()){
            if (fileName != ""){
                String targetFile = session.getServletContext().getRealPath("/file/"+fileName);
                File file = new File(targetFile);
                file.delete();
            }
        }

        return serverResponse;
    }

    @PostMapping("/gradeupdate")
    public String update( Grade grade,@RequestParam("uploadFile") MultipartFile uploadFile,HttpSession session,HttpServletRequest request){
            if (grade.getFileName() == null){
                grade.setFileName("");
            }
            if (uploadFile.getSize() > 0 ) {
                String filename = uploadFile.getOriginalFilename();
                String leftPath = session.getServletContext().getRealPath("/file");
                File path =new File(leftPath);
                if ( !path.exists() && path.isAbsolute() ){
                    path.mkdir();
                }
                File file = new File(leftPath, filename);
                if ( file.exists() ){
                    file.delete();
                }
                try {
                    uploadFile.transferTo(file);
                    grade.setFileName(filename);
                    String json = JSON.toJSONString(gradeService.update(grade));
                    request.setAttribute("serverResponse",json);
                    return "main/grade_find";
                } catch (IOException e) {
                    e.printStackTrace();
                    String json = JSON.toJSONString(ServerResponse.createError("提交失败！"));
                    request.setAttribute("serverResponse",json);
                    return "main/grade_find";
                }
            }
            String json = JSON.toJSONString(gradeService.update(grade));
            request.setAttribute("serverResponse",json);
            return "main/grade_find";
    }

    @GetMapping("/grade/{id}")
    @ResponseBody
    public ServerResponse<Grade> findById(@PathVariable("id") Long id){
        return gradeService.findById(id);
    }

    @PostMapping("/grades/{pageNum}")
    @ResponseBody
    public ServerResponse<PageInfo> findByCondition(@RequestBody Grade grade,@PathVariable(value = "pageNum")  int pageNum){
        return gradeService.findByCondition(grade,pageNum,10);
    }
}
