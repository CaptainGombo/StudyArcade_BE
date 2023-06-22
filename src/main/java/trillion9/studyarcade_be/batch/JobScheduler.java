package trillion9.studyarcade_be.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@Configuration
@RequiredArgsConstructor
public class JobScheduler {

    private final Job job;
    private final JobLauncher jobLauncher;

    //    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행되도록 설정
    @GetMapping("/api/batch")
    public void executeJob() throws JobParametersInvalidException, JobExecutionAlreadyRunningException,
            JobRestartException, JobInstanceAlreadyCompleteException {

        Map<String, JobParameter> jobParametersMap = new HashMap<>();

        String timeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date());

        jobParametersMap.put("DateTime", new JobParameter(timeString));

        JobParameters jobParameters = new JobParameters(jobParametersMap);

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

//        while (jobExecution.isRunning()) log.info("...");
//        log.info("Job Execution: " + jobExecution.getStatus());
//        log.info("Job getJobConfigurationName: " + jobExecution.getJobConfigurationName());
//        log.info("Job getJobId: " + jobExecution.getJobId());
//        log.info("Job getExitStatus: " + jobExecution.getExitStatus());
//        log.info("Job getJobInstance: " + jobExecution.getJobInstance());
//        log.info("Job getStepExecutions: " + jobExecution.getStepExecutions());
//        log.info("Job getLastUpdated: " + jobExecution.getLastUpdated());
//        log.info("Job getFailureExceptions: " + jobExecution.getFailureExceptions());
    }
}
