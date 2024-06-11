package com.sky.aspet;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info(" 公共字段数据填充 .... ");
        //获取操作类型 insert update
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType value = autoFill.value();
        // 获取操作对象的方法
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){return;}
        // 获取操作的对象
        Object arg = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        try {
            Class targetObject = arg.getClass();
            Method setCreateTime = targetObject.getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME,LocalDateTime.class);
            Method setCreateUser = targetObject.getDeclaredMethod(AutoFillConstant.SET_CREATE_USER,Long.class);
            Method setUpdateTime = targetObject.getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME,LocalDateTime.class);
            Method setUpdateUser = targetObject.getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER,Long.class);
            // 修改对象的的值
            if(OperationType.INSERT == value){
                setCreateTime.invoke(arg,now);
                setCreateUser.invoke(arg,currentId);
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentId);
            }else if(OperationType.UPDATE == value){
                setUpdateTime.invoke(arg,now);
                setUpdateUser.invoke(arg,currentId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
