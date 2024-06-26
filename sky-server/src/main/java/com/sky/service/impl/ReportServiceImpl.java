package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    OrderDetailMapper orderDetailMapper;

    @Override
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = genderDateList(begin,end);
        // 查询每天的营业额，再进行数据统计
        ArrayList<Double> turnoverList = new ArrayList<>();
        dateList.forEach(localDate -> {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            HashMap map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double amount = orderMapper.getByMap(map);
            amount = amount == null ? 0.0 : amount;
            turnoverList.add(amount);
        });

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //查询总用户数量根据时间
        List<LocalDate> dateList = genderDateList(begin,end);
        //查询每日新增用户数量

        ArrayList<Integer> totalUserList = new ArrayList<>();
        ArrayList<Integer> newUserList = new ArrayList<>();
        dateList.forEach(localDate -> {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            HashMap map = new HashMap<>();
            map.put("end",endTime);

            //统计总用户数量
            Integer users = userMapper.getByMap(map);
            users = users == null ? 0 : users;
            totalUserList.add(users);

            //统计新增用户数量
            map.put("begin",beginTime);
            Integer newUsers = userMapper.getByMap(map);
            newUsers = newUsers == null ? 0 : newUsers;
            newUserList.add(newUsers);

        });

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    //生成需要的连接日期
    List<LocalDate> genderDateList(LocalDate begin,LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        // 首先生成datelist
        while (!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        //查询总用户数量根据时间
        List<LocalDate> dateList = genderDateList(begin,end);

        ArrayList<Integer> totalOrder = new ArrayList<>();
        ArrayList<Integer> validOrder = new ArrayList<>();
        dateList.forEach(date->{
            LocalDateTime beginTime = LocalDateTime.of(date,LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date,LocalTime.MAX);
            HashMap<Object, Object> map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            //获取每日订单数量
            Integer totalCount = orderMapper.getByOrderMap(map);
            totalOrder.add(totalCount);

            //获取每日有效订单数量
            //有效订单数
            map.put("status",Orders.COMPLETED);
            Integer validOrderCount  = orderMapper.getByOrderMap(map);
            validOrder.add(validOrderCount);
        });

        //统计有效订单数量的总数
        Integer sumOrders = totalOrder.stream().reduce(Integer::sum).get();
        //统计所有订单的总数
        Integer sumValidOrders = validOrder.stream().reduce(Integer::sum).get();
        //计算订单有效率
        Double orderCompletedRate = 0.0;
        if(sumOrders != null){
            orderCompletedRate = sumValidOrders.doubleValue() / sumOrders;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(totalOrder,","))
                .validOrderCountList(StringUtils.join(validOrder,","))
                .totalOrderCount(sumOrders)
                .validOrderCount(sumValidOrders)
                .orderCompletionRate(orderCompletedRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        //统计销量前10的产口名称，发及数量
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getSaleTop10(beginTime,endTime);
        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }


}
