package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequestMapping("shoppingCart")
@RestController
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list()
    {
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        lambdaQueryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList=shoppingCartService.list(lambdaQueryWrapper);
        return R.success(shoppingCartList);
    }


    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart)
    {
        //设置购物车属于哪个用户 用户id
        Long userId= BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        //查询当前菜品是否已经存在与购物车种(需要区分套餐还是dish)
        Long dishId= shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId());//先将id匹配到 之后根据条件查询
        if(dishId!=null)//是菜品
        {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,shoppingCart.getDishId());
        }else//是套餐
        {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //如果已经存在，就在之前的amount上+1
        ShoppingCart cartServiceOne=shoppingCartService.getOne(lambdaQueryWrapper);
        if(cartServiceOne!=null)
        {
            Integer number=cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }
        //不存在则保存加入
        else
        {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cartServiceOne=shoppingCart;
        }


        return R.success(cartServiceOne);
    }

    @DeleteMapping
    public R<String> clean()
    {
        Long userId=BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return R.error("删除成功");
    }

    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart)
    {
        Long dishId= shoppingCart.getDishId();
        Long setMealId=shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        if(dishId!=null)
        {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else
        {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,setMealId);
        }

        ShoppingCart shoppingCart1=shoppingCartService.getOne(lambdaQueryWrapper);

        int number=shoppingCart1.getNumber();
        if(number==1)
        {
            shoppingCartService.removeById(shoppingCart1.getId());
        }else
        {
            shoppingCart1.setNumber(number-1);
            shoppingCartService.updateById(shoppingCart1);

        }
        return R.success(shoppingCart1);

    }

}
