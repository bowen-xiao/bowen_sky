package com.sky.task;

import com.alibaba.fastjson.JSON;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WebSocketTask {
    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    OrderMapper orderMapper;

    /**
     * 通过WebSocket每隔5秒向客户端发送消息
     */
    @Scheduled(cron = "0 * * * * ?")
    public void sendMessageToClient() {
        //可以主动向客户端发消息
        log.info("sendMessageToClient :::这是来自服务端的消息 ");
       // webSocketServer.sendToAllClient("这是来自服务端的消息：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
        // 每分钟推送一次未接单的信息
       /* List<Orders> ordersList = orderMapper.getByStatus(Orders.TO_BE_CONFIRMED);
        if(ordersList != null && ordersList.size() >0){
            ordersList.forEach( orders -> {
                Map map = new HashMap();
                map.put("type", 1);//通知类型 1来单提醒 2客户催单
                map.put("orderId", orders.getId());//订单id
                map.put("content","订单号:" + orders.getNumber());
                webSocketServer.sendToAllClient(JSON.toJSONString(map));

            });
        }*/
    }
}
