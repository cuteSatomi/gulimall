package com.zzx.gulimall.product;

import com.zzx.common.gulienum.BizCodeEnum;
import com.zzx.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @date 2021-05-06 20:59
 * <p>
 * 全局的异常处理类
 * basePackages表示处理改包下产生的所有异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zzx.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    /**
     * 处理数据校验产生的异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<String, String>();
        bindingResult.getFieldErrors().forEach((fieldError) -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data", errorMap);
    }

    /**
     * 处理所有异常
     *
     * @param throwable
     * @return
     */
    @ExceptionHandler(Throwable.class)
    public R handleValidException(Throwable throwable) {

        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMsg());
    }
}
