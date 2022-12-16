package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    public CategoryService categoryService;

    @PostMapping
    public R<String> addCategory(@RequestBody Category category)
    {
        log.info("新增{}",category.toString());
        categoryService.save(category);
        return R.success("成功新增分类");
    }


    @GetMapping("/page")
    public R<Page> page(int page,int pageSize)
    {
        Page<Category> pageInfo=new Page<>(page,pageSize);
        LambdaQueryWrapper<Category> lambdaQueryWrapper=new LambdaQueryWrapper();
        lambdaQueryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo,lambdaQueryWrapper);
        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> delete(Long ids)
    {

        log.info("删除商品为：{}",ids);
        categoryService.remove(ids);
        return R.success("删除成功");

    }

    @PutMapping
    public R<String> update(@RequestBody Category category)
    {
        categoryService.updateById(category);

        return R.success("更新success");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category)
    {
        LambdaQueryWrapper<Category> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> result=categoryService.list(queryWrapper);
        return R.success(result);
    }




}
