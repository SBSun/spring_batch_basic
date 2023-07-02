package com.example.springbatchbasic.job.filedata;

import com.example.springbatchbasic.job.filedata.dto.Player;
import com.example.springbatchbasic.job.filedata.dto.PlayerYears;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class FileDataReadWriteConfig {

    @Bean
    public Job fileReadWriteJob(JobRepository jobRepository, Step fileReadWriteStep) {
        return new JobBuilder("fileReadWriteJob", jobRepository)
                .start(fileReadWriteStep)
                .build();
    }

    @JobScope
    @Bean
    public Step fileReadWriteStep(JobRepository jobRepository, PlatformTransactionManager platformTransactionManager,
                                  ItemReader playerItemReader, ItemProcessor playersItemProcessor, ItemWriter playersItemWriter){
        return new StepBuilder("fileReadWriteStep", jobRepository)
                .<Player, PlayerYears>chunk(5, platformTransactionManager)
                .reader(playerItemReader)
                .processor(playersItemProcessor)
                .writer(playersItemWriter)
                .build();
    }

    @StepScope
    @Bean
    public FlatFileItemReader<Player> playerItemReader(){
        return new FlatFileItemReaderBuilder<Player>()
                .name("playerItemReader")
                .resource(new FileSystemResource("Players.csv")) // 파일 주소
                .lineTokenizer(new DelimitedLineTokenizer()) // 데이터가 , 단위로 나뉘게
                .fieldSetMapper(new PlayerFieldSetMapper()) // 읽어온 데이터를 객체로 변경할 수 있도록 Mapper
                .linesToSkip(1) // csv 파일의 첫 번째 행은 컬럼들의 이름이기 때문에 제외시킨다.
                .build();
    }

    @StepScope
    @Bean
    public ItemProcessor<Player, PlayerYears> playersItemProcessor(){
        return new ItemProcessor<Player, PlayerYears>() {
            @Override
            public PlayerYears process(Player item) throws Exception {
                return new PlayerYears(item);
            }
        };
    }

    @StepScope
    @Bean
    public FlatFileItemWriter<PlayerYears> playersItemWriter(){
        // FlatFileItemWriter에서는 어떤 필드를 사용할지에 대해서 명시해 줘야 한다.
        BeanWrapperFieldExtractor<PlayerYears> fieldExtractor = new BeanWrapperFieldExtractor<>();
        fieldExtractor.setNames(new String[]{"ID", "lastName", "position", "yearsExperience"});
        fieldExtractor.afterPropertiesSet();

        // 어떤 기준으로 파일을 만들어줄지
        DelimitedLineAggregator<PlayerYears> lineAggregator = new DelimitedLineAggregator<>();
        lineAggregator.setDelimiter(",");
        lineAggregator.setFieldExtractor(fieldExtractor);

        FileSystemResource outputResource = new FileSystemResource("players_output.txt");

        return new FlatFileItemWriterBuilder<PlayerYears>()
                .name("playersItemWriter")
                .resource(outputResource)
                .lineAggregator(lineAggregator)
                .build();
    }
}
