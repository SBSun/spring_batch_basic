package com.example.springbatchbasic.job.validatedparam;

import com.example.springbatchbasic.job.validatedparam.validator.FileParamValidator;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.CompositeJobParametersValidator;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

/**
 * desc: 파일 이름 파라미터 전달 그리고 검증
 */
@Configuration
public class ValidatedParamJobConfig {

    @Bean
    public Job validatedParamJob(JobRepository jobRepository, Step validatedParamStep){
        return new JobBuilder("validatedParamJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .validator(new FileParamValidator())
                .start(validatedParamStep)
                .build();
    }

    private CompositeJobParametersValidator multipleValidator(){
        CompositeJobParametersValidator validator = new CompositeJobParametersValidator();
        validator.setValidators(Arrays.asList(new FileParamValidator()));

        return validator;
    }

    @JobScope
    @Bean
    public Step validatedParamStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager){
        return new StepBuilder("validatedParamStep", jobRepository)
                .tasklet(validatedParamTasklet(null), platformTransactionManager)
                .build();
    }

    @StepScope
    @Bean
    public Tasklet validatedParamTasklet(@Value("#{jobParameters['fileName']}") String fileName){
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                System.out.println(fileName);
                System.out.println("validated Param Tasklet");
                return RepeatStatus.FINISHED;
            }
        };
    }
}
