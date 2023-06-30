package com.example.springbatchbasic.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class HelloWorldJobConfig{

    @Bean
    public Job helloWorldJob(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new JobBuilder("helloWorldJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // Job을 실행할 때 Id를 부여하는데, Sequence를 순차적으로 부여할 수 있도록 설정
                .start(helloWorldStep(jobRepository, platformTransactionManager))
                .build();
    }

    @JobScope
    @Bean
    public Step helloWorldStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("helloWorldStep", jobRepository)
                .tasklet(helloWorldTasklet(), platformTransactionManager)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet helloWorldTasklet(){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println("Hello World Spring Batch");
                return RepeatStatus.FINISHED;   // FINISHED를 명시함으로써 이 스탭을 끝내도록 설정
            }
        };
    }
}
