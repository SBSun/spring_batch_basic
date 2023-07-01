package com.example.springbatchbasic.job.readwrite;

import com.example.springbatchbasic.domain.accounts.Accounts;
import com.example.springbatchbasic.domain.accounts.AccountsRepository;
import com.example.springbatchbasic.domain.orders.Orders;
import com.example.springbatchbasic.domain.orders.OrdersRepository;
import com.example.springbatchbasic.job.joblistener.JobLoggerListener;
import org.aspectj.weaver.ast.Or;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.Collections;

/**
 * desc: 주문 테이블 -> 정산 테이블 데이터 이관
 */
@Configuration
public class TrMigrationConfig {

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private AccountsRepository accountsRepository;

    @Bean
    public Job trMigrationJob(JobRepository jobRepository, Step trMigrationStep){
        return new JobBuilder("trMigrationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobLoggerListener())
                .start(trMigrationStep)
                .build();
    }

    @JobScope
    @Bean
    public Step trMigrationStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                                ItemReader trOrdersReader, ItemProcessor trOrdersProcessor, ItemWriter trOrdersWriter){
        return new StepBuilder("trMigrationStep", jobRepository)
                .<Orders, Accounts>chunk(5, platformTransactionManager)
                .reader(trOrdersReader)
                .processor(trOrdersProcessor)
                .writer(trOrdersWriter)
                .build();
    }

    @StepScope
    @Bean
    public RepositoryItemReader<Orders> trOrdersReader(){
        return new RepositoryItemReaderBuilder<Orders>()
                .name("trOrdersReader")
                .repository(ordersRepository)
                .methodName("findAll")
                .pageSize(5)
                .arguments(Arrays.asList())
                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Orders, Accounts> trOrdersProcessor(){
        return new ItemProcessor<Orders, Accounts>() {
            @Override
            public Accounts process(Orders item) throws Exception {
                return new Accounts(item);
            }
        };
    }

    @StepScope
    @Bean
    public RepositoryItemWriter<Accounts> trOrdersWriter(){
        return new RepositoryItemWriterBuilder<Accounts>()
                .repository(accountsRepository)
                .methodName("save")
                .build();
    }
}
