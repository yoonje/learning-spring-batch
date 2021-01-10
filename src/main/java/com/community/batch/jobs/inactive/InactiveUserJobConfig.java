package com.community.batch.jobs.inactive;

import com.community.batch.domain.User;
import com.community.batch.domain.enums.UserStatus;
import com.community.batch.jobs.inactive.listener.InactiveIJobListener;
import com.community.batch.jobs.inactive.listener.InactiveStepListener;
import com.community.batch.repository.UserRepository;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class InactiveUserJobConfig {

    private final static int CHUNK_SIZE = 5;

    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job inactiveUserJob(JobBuilderFactory jobBuilderFactory, InactiveIJobListener inactiveIJobListener, Flow inactiveJobFlow) {
        return jobBuilderFactory.get("inactiveUserJob")
                .listener(inactiveIJobListener)
                .start(inactiveJobFlow)
                .end()
                .build();
    }

    @Bean
    public Flow inactiveJobFlow(Step inactiveJobStep) {
        FlowBuilder<Flow> flowBuilder = new FlowBuilder<>("inactiveJobFlow");
        return flowBuilder
                .start(new InactiveJobExecutionDecider())
                .on(FlowExecutionStatus.FAILED.getName()).end()
                .on(FlowExecutionStatus.COMPLETED.getName()).to(inactiveJobStep)
                .end();
    }

    @Bean
    // StepListener 대신 ChunkListener 들어가도 됨
    // inactiveUserJpaReader 대신 주석 해제하고 inactiveUserReader가 들어가도 됨
    public Step inactiveJobStep(StepBuilderFactory stepBuilderFactory, JpaPagingItemReader<User> inactiveUserJpaReader, InactiveStepListener inactiveStepListener) {
        return stepBuilderFactory.get("inactiveUserStep")
                .<User, User> chunk(CHUNK_SIZE)
                .reader(inactiveUserJpaReader)
                .processor(inactiveUserProcessor())
                .writer(inactiveUserWriter())
                .listener(inactiveStepListener)
                .build();
    }

    @Bean
    @StepScope
    public JpaPagingItemReader<User> inactiveUserJpaReader(){
        JpaPagingItemReader<User> jpaPagingItemReader = new JpaPagingItemReader(){
            @Override
            public int getPage() {
                return 0;
            }
        };
        jpaPagingItemReader.setQueryString("select u from User as u where u.updatedDate < :updatedDate and u.status = :status");

        Map<String, Object> map = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        map.put("updatedDate", now.minusYears(1));
        map.put("status", UserStatus.ACTIVE);

        jpaPagingItemReader.setParameterValues(map);
        jpaPagingItemReader.setEntityManagerFactory(entityManagerFactory);
        jpaPagingItemReader.setPageSize(CHUNK_SIZE);
        return jpaPagingItemReader;
    }

//    @Bean
//    @StepScope
//    // SpEL로 동적으로 파라미터를 전달받는 Reader
//    public ListItemReader<User> inactiveUserReader(@Value("#{jobParameters[nowDate]}") Date nowDate, UserRepository userRepository) {
//        LocalDateTime now = LocalDateTime.ofInstant(nowDate.toInstant(), ZoneId.systemDefault());
//        List<User> inactiveUsers = userRepository.findByCreatedDateBeforeAndStatusEquals(now.minusYears(1), UserStatus.ACTIVE);
//        return new ListItemReader<>(inactiveUsers);
//    }

    private InactiveItemProcessor inactiveUserProcessor() {
        return new InactiveItemProcessor();
    }

    private JpaItemWriter<User> inactiveUserWriter() {
        JpaItemWriter<User> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(entityManagerFactory);
        return jpaItemWriter;
    }
}
