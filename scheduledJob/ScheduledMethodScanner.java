package com.sharkdom.service.scheduledJob;

import com.sharkdom.entity.scheduledJob.JobInfo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduledMethodScanner implements ApplicationContextAware {

    private ApplicationContext context;
    @Getter
    private final List<JobInfo> jobInfos = new ArrayList<>();

    @PostConstruct
    public void scanScheduledMethods() {
        for (String beanName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanName);
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

            for (Method method : targetClass.getDeclaredMethods()) {
                Scheduled scheduled = method.getAnnotation(Scheduled.class);
                if (scheduled != null) {
                    jobInfos.add(
                            JobInfo.builder().className(targetClass.getName())
                                    .methodName(method.getName())
                                    .cron(scheduled.cron().isEmpty() ? null : scheduled.cron())
                                    .fixedDelay(scheduled.fixedDelay() > 0 ? scheduled.fixedDelay() : null)
                                    .fixedRate(scheduled.fixedRate() > 0 ? scheduled.fixedRate() : null)
                                    .build()
                    );
                }
            }
        }
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
