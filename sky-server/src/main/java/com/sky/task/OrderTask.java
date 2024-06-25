package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    OrderMapper orderMapper;

    //每1分钟自动执行一次清理超时订单
    @Scheduled(cron = "0 * * * * ?")
    public void processTimeOutOrder(){
        log.info("自动处理超时订单");
        LocalDateTime timeOut = LocalDateTime.now().plusMinutes(-15);
        //查询超时订单，存在就自动处理
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, timeOut);
        if(ordersList != null && ordersList.size() >0){
            ordersList.forEach(o->{
                //设置状态为超时
                o.setStatus(Orders.CANCELLED);
                o.setCancelTime(LocalDateTime.now());
                o.setCancelReason("支付超时，系统自动取消");
                orderMapper.update(o);
            });
        }
    }

    //每一天1点自动执行一次清理状态为未完成的订单
    @Scheduled(cron = "0 0 1 * * ?")
    public void processDeliveryOrder(){
        log.info("自动处理派送中的订单");
        LocalDateTime timeOut = LocalDateTime.now().plusMinutes(-60);
        //查询超时订单，存在就自动处理
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, timeOut);
        if(ordersList != null && ordersList.size() >0){
            ordersList.forEach(o->{
                //设置状态为超时
                o.setStatus(Orders.COMPLETED);
                orderMapper.update(o);
            });
        }
    }
}
