package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/order")
@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders)
    {

        orderService.submit(orders);
        return R.success("成功提交");
    }

    @Transactional
    @GetMapping("/userPage")
    public R<Page> userpage(int page,int pageSize)
    {
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage=new Page<>();
        Long userId= BaseContext.getCurrentId();


        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId,userId);
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,ordersLambdaQueryWrapper);

        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");

        List<Orders> ordersList=ordersPage.getRecords();

        List<OrdersDto> ordersDtoList=ordersList.stream().map(item->{
            OrdersDto ordersDto=new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);
            Long Id = item.getId();
            //根据id查分类对象
            Orders orders = orderService.getById(Id);
            String number = orders.getNumber();
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId,number);
            List<OrderDetail> orderDetailList = orderDetailService.list(lambdaQueryWrapper);
            int num=0;
            for(OrderDetail l:orderDetailList){
                num+=l.getNumber().intValue();
            }

            ordersDto.setSumNum(num);
            return ordersDto;

        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String number,String beginTime,String endTime)
    {
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        Page<OrdersDto> ordersDtoPage=new Page<>();

        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper=new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(StringUtils.isNotEmpty(number),Orders::getNumber,number);

        if(beginTime!=null&&endTime!=null)
        {
            ordersLambdaQueryWrapper.le(Orders::getOrderTime,endTime);
            ordersLambdaQueryWrapper.ge(Orders::getOrderTime,beginTime);

            ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        }
        orderService.page(ordersPage,ordersLambdaQueryWrapper);

        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        List<Orders> ordersList=ordersPage.getRecords();
        List<OrdersDto> ordersDtoList=ordersList.stream().map(item->{
            OrdersDto ordersDto=new OrdersDto();
            BeanUtils.copyProperties(item,ordersDto);

            //用户添加
            Long userId=item.getUserId();
            User user= userService.getById(userId);
            String name=user.getName();
            ordersDto.setUserName(name);
            return  ordersDto;


        }).collect(Collectors.toList());


        ordersDtoPage.setRecords(ordersDtoList);
        return R.success(ordersDtoPage);
    }

    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders)
    {
        Long orderId= orders.getId();
        Orders orders1=orderService.getById(orderId);
        orders1.setStatus(orders.getStatus());
        orderService.updateById(orders1);
        return R.success("更新成功");
    }

    @PostMapping("/again")
    public R<OrdersDto> again(@RequestBody Orders orders)
    {
        Long orderId= orders.getId();
        Orders oldOrder=orderService.getById(orderId);

        Long newOrderId= IdWorker.getId();
        oldOrder.setId(newOrderId);
        String newNumber=String.valueOf(IdWorker.getId());
        oldOrder.setNumber(newNumber);
        oldOrder.setOrderTime(LocalDateTime.now());
        oldOrder.setCheckoutTime(LocalDateTime.now());
        oldOrder.setStatus(2);

        //看有几件商品
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderId);
        int count=orderDetailService.count(orderDetailLambdaQueryWrapper);
        OrdersDto ordersDto=new OrdersDto();
        BeanUtils.copyProperties(oldOrder,ordersDto);
        ordersDto.setSumNum(count);
        orderService.save(ordersDto);


        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper1=new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper1.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetailList=orderDetailService.list(orderDetailLambdaQueryWrapper);
        orderDetailList=orderDetailList.stream().map(item->{
            item.setOrderId(newOrderId);
            Long detailId=IdWorker.getId();
            item.setId(detailId);

            return item;
        }).collect(Collectors.toList());
        orderDetailService.saveBatch(orderDetailList);

        return R.success(ordersDto);


    }


}
