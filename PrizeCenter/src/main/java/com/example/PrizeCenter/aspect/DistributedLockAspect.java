package com.example.PrizeCenter.aspect;

import com.example.PrizeCenter.annotation.DistributedLock;
import com.example.PrizeCenter.exception.DistributedLockException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import java.lang.reflect.Method;

@Aspect
@Component
public class DistributedLockAspect {

    @Resource
    private RedissonClient redissonClient;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.example.PrizeCenter.annotation.DistributedLock)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock lock = method.getAnnotation(DistributedLock.class);

        String key = lock.prefix() + parseSpEL(method, joinPoint.getArgs(), lock.key());
        RLock rLock = redissonClient.getLock(key);

        boolean acquired = rLock.tryLock(lock.waitTime(), lock.leaseTime(), lock.unit());
        if (!acquired) {
            throw new DistributedLockException("请求过于频繁，请稍后再试！");
        }
        try {
            return joinPoint.proceed();
        } finally {
            if (rLock.isHeldByCurrentThread()) {
                rLock.unlock();
            }
        }
    }

    private String parseSpEL(Method method, Object[] args, String spEL) {
        String[] params = discoverer.getParameterNames(method);
        // 使用 SimpleEvaluationContext 限制 SpEL 能力，仅支持读取变量，防止任意代码执行
        EvaluationContext context = SimpleEvaluationContext.forReadOnlyDataBinding().build();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                context.setVariable(params[i], args[i]);
            }
        }
        return parser.parseExpression(spEL).getValue(context, String.class);
    }
}