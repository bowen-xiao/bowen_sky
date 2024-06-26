package com.sky.controller.user;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "C端-下单")
public class OrderController {


    @Autowired
    private OrderService orderService;

    @PostMapping("/submit")
    @ApiOperation("用户下单接口")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO dto){
        return Result.success(orderService.submitOrder(dto));
    }


    /**
     * 历史订单查询
     *
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> page(int page, int pageSize, Integer status) {
        PageResult pageResult = orderService.pageQuery4User(page, pageSize, status);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }

    /**
     * 用户催单信息
     *
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("用户催单")
    public Result reminder(@PathVariable Long id){
//        orderService.paySuccess(outTradeNo);
        //用户催单
        orderService.reminder(id);
        return Result.success();
    }


    /**
     * 无法实现支付的测试方法
     *
     * @param outTradeNo
     * @return
     */
    @GetMapping("/paysuccess/{outTradeNo}")
    @ApiOperation("用于测试支付成功")
    public Result paySuccessTest(@PathVariable String outTradeNo){
//        orderService.paySuccess(outTradeNo);
        log.info("测试支付成功..::{}.....",outTradeNo);
        orderService.paySuccess(outTradeNo);
        return Result.success();
    }

 /**
     * 无法实现支付的测试方法
     *
     * @param outTradeNo
     * @return
     */
   /* @GetMapping("/order/orderDetail/{id}")
    @ApiOperation("用户查询订单详细")
    public Result paySuccessTest(@PathVariable String outTradeNo){
//        orderService.paySuccess(outTradeNo);
        log.info("测试支付成功..::{}.....",outTradeNo);
        orderService.paySuccess(outTradeNo);
        return Result.success();
    }*/



}
