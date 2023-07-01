package com.example.springbatchbasic.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class MyScheduler {

    @Autowired
    private JobRegistry jobRegistry;

    @Autowired
    private JobLauncher jobLauncher;

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(){
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);

        return jobRegistryBeanPostProcessor;
    }

    //@Scheduled(fixedDelay = 200000)
    public void startHelloWorldJob() throws JobExecutionException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobId", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("helloWorldJob"), params);
    }

    //@Scheduled(fixedDelay = 100000)
    public void startValidatedParamJob() throws JobExecutionException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobId", String.valueOf(System.currentTimeMillis()))
                .addString("fileName", "test")
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("validatedParamJob"), params);
    }

    //@Scheduled(fixedDelay = 100000)
    public void startJobListenerJob() throws JobExecutionException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobId", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("jobListenerJob"), params);
    }

    @Scheduled(fixedDelay = 100000)
    public void startTrMigrationJob() throws JobExecutionException {
        JobParameters params = new JobParametersBuilder()
                .addString("JobId", String.valueOf(System.currentTimeMillis()))
                .toJobParameters();

        jobLauncher.run(jobRegistry.getJob("trMigrationJob"), params);
    }
}
