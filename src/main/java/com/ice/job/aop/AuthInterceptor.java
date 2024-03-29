package com.ice.job.aop;


import com.ice.job.annotation.AuthCheck;
import com.ice.job.common.ErrorCode;
import com.ice.job.constant.UserHolder;
import com.ice.job.exception.BusinessException;
import com.ice.job.model.enums.UserRoleEnum;
import com.ice.job.model.vo.UserLoginVO;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 权限校验 AOP
 */
@Aspect
@Component
public class AuthInterceptor {

    /**
     * 执行拦截
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();

        // 当前登录用户
        UserLoginVO user = UserHolder.getUser();

        // 必须有该权限才通过
        if (StringUtils.isNotBlank(mustRole)) {
            UserRoleEnum mustUserRoleEnum = UserRoleEnum.getEnumByValue(mustRole);
            if (mustUserRoleEnum == null) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }

            String userRole = user.getUserRole();

            // 如果被封号，直接拒绝
            if (UserRoleEnum.BAN.equals(mustUserRoleEnum)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }

            // 必须拥有招聘者权限
            if (UserRoleEnum.EMPLOYER.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole) && !UserRoleEnum.ADMIN.getValue().equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }

            // 必须有管理员权限
            if (UserRoleEnum.ADMIN.equals(mustUserRoleEnum)) {
                if (!mustRole.equals(userRole)) {
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
            }


        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}

