package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.entity.User;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list=new ArrayList<>();
        //list.add(begin);
        while (true){
            list.add(begin);
            if(begin.equals(end))
            {
                break;
            }
            begin=begin.plusDays(1);

        }

        List<Double> amountsList=new ArrayList<>();

        for (LocalDate localDate : list) {
            LocalDateTime beginTimeMin=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTimeMax=LocalDateTime.of(localDate, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("begin",beginTimeMin);
            map.put("end",endTimeMax);
            map.put("status", Orders.COMPLETED);
            Double amounts=orderMapper.getAmount(map);
            if(amounts==null){
                amounts=0.0;
            }
            amountsList.add(amounts);
        }


        TurnoverReportVO turnoverReportVO=new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(list,","));
        turnoverReportVO.setTurnoverList(StringUtils.join(amountsList,","));
        return turnoverReportVO;
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        while (!begin.equals(end)){
            dateList.add(begin);
            begin.plusDays(1);
        }


        List<Integer> newUserList=new ArrayList<>();
        List<Integer> totalList=new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("end",endTime);
            Integer sumUserToday=userMapper.getUserToday(map);
            map.put("begin",beginTime);
            Integer sumUser=userMapper.getSum(map);
            newUserList.add(sumUser);
            totalList.add(sumUserToday);
        }
        UserReportVO userReportVO=new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList,","));
        userReportVO.setNewUserList(StringUtils.join(newUserList,","));
        userReportVO.setTotalUserList(StringUtils.join(totalList,","));
        return userReportVO;
    }

    @Override
    public OrderReportVO getorderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList=new ArrayList<>();
        while (!begin.equals(end)){
            dateList.add(begin);
            begin.plusDays(1);
        }

        List<Integer> totalList=new ArrayList<>();
        List<Integer> comedList=new ArrayList<>();

        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(localDate, LocalTime.MAX);
            Map map=new HashMap<>();
            map.put("end",endTime);
            map.put("begin",beginTime);
            Integer total=orderMapper.getTotal(map);
            map.put("status",Orders.COMPLETED);
            Integer comed=orderMapper.getTotal(map);
            totalList.add(total);
            comedList.add(comed);
        }

        Integer total=0;
        for (Integer i : totalList) {
            total+=i;
        }

        Integer valid=0;
        for (Integer i : comedList) {
            valid+=i;
        }

        OrderReportVO orderReportVO=new OrderReportVO();
        orderReportVO.setDateList(StringUtils.join(dateList,","));
        orderReportVO.setOrderCountList(StringUtils.join(totalList,","));
        orderReportVO.setValidOrderCountList(StringUtils.join(comedList,","));
        orderReportVO.setTotalOrderCount(total);
        orderReportVO.setValidOrderCount(valid);
        Double orderCompletionRate=0.0;
        if(total!=0){
            orderCompletionRate=(double)valid/total;
        }
        orderReportVO.setOrderCompletionRate(orderCompletionRate);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        List<GoodsSalesDTO> saleTop10 = orderMapper.getSaleTop10(beginTime, endTime);
        List<String> nameList = saleTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList= saleTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        String name = StringUtils.join(nameList, ",");
        String number = StringUtils.join(numberList, ",");

        SalesTop10ReportVO salesTop10ReportVO = SalesTop10ReportVO.builder()
                .nameList(name)
                .numberList(number)
                .build();

        return salesTop10ReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate localDatebigin=LocalDate.now().minusDays(30);
        LocalDate localDateend=LocalDate.now();
        LocalDateTime begin=LocalDateTime.of(localDatebigin,LocalTime.MIN);
        LocalDateTime end=LocalDateTime.of(localDateend,LocalTime.MAX);
        BusinessDataVO businessData = workspaceService.getBusinessData(begin,end);

        InputStream in=this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {
            XSSFWorkbook excel=new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间:"+localDatebigin+"之"+localDateend);
            sheet.getRow(3).getCell(2).setCellValue(businessData.getTurnover());
            sheet.getRow(3).getCell(4).setCellValue(businessData.getOrderCompletionRate());
            sheet.getRow(3).getCell(6).setCellValue(businessData.getNewUsers());
            sheet.getRow(4).getCell(2).setCellValue(businessData.getValidOrderCount());
            sheet.getRow(4).getCell(4).setCellValue(businessData.getUnitPrice());


            for (int i=0;i<30;i++){
                LocalDate date=localDatebigin.plusDays(1);
                BusinessDataVO businessData1 = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(String.valueOf(date));
                row.getCell(2).setCellValue(businessData1.getTurnover());
                row.getCell(3).setCellValue(businessData1.getValidOrderCount());
                row.getCell(4).setCellValue(businessData1.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData1.getNewUsers());
                row.getCell(6).setCellValue(businessData1.getUnitPrice());
            }

            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
