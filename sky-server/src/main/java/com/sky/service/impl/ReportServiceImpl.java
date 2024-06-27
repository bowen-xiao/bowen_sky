package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.val;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    @Autowired
    WorkspaceService workspaceService;

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

    public void exportBusinessData(HttpServletResponse response) {
        //获取近30日的订单统计数据
        LocalDate beginDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now().minusDays(1);
        BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(beginDate, LocalTime.MIN), LocalDateTime.of(endDate, LocalTime.MAX));
        // 计算出开始和结束日期
        try (
                InputStream ins = getClass().getClassLoader().getResource("template/report.xlsx").openStream();
                XSSFWorkbook excel = new XSSFWorkbook(ins);
        ) {
            // 查询统计出来的数据进行填充
            XSSFSheet sheet = excel.getSheet("Sheet1");
            XSSFRow row = sheet.getRow(1);
            row.getCell(1).setCellValue("日期:" + beginDate + "至"+ endDate);
            row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessData.getTurnover());
            row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessData.getNewUsers());

            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessData.getValidOrderCount());
            row.getCell(4).setCellValue(businessData.getUnitPrice());
            // 统计每天的数据
            for (int i = 0; i < 30; i++) {
                LocalDate localDate = beginDate.plusDays(i);
                businessData = workspaceService.getBusinessData(LocalDateTime.of(localDate, LocalTime.MIN), LocalDateTime.of(localDate, LocalTime.MAX));
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(localDate.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }
            //  返回需要的数据
            excel.write(response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
