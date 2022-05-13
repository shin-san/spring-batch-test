package au.com.jc.batch.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
public class BatchController {

    JobLauncher jobLauncher;

    JobExplorer jobExplorer;

    JobRepository jobRepository;

    Job sendReportToDB;

    @Autowired
    public BatchController(JobLauncher jobLauncher,
                           JobExplorer jobExplorer,
                           JobRepository jobRepository,
                           Job sendReportToDB) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobRepository = jobRepository;
        this.sendReportToDB = sendReportToDB;
    }


    @GetMapping(path = "/batch")
    public @ResponseBody ResponseEntity<String> runBatch() throws
            JobInstanceAlreadyCompleteException,
            JobExecutionAlreadyRunningException,
            JobParametersInvalidException,
            JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("FILE_NAME", "report.xml")
                        .toJobParameters();

        jobLauncher.run(sendReportToDB,
                jobParameters);

        JobExecution jobExecution = jobRepository.getLastJobExecution(sendReportToDB.getName(), jobParameters);

        assert jobExecution != null;
        jobExecution.getStatus();
        BatchStatus batchStatus = jobExecution.getStatus();

        if (BatchStatus.COMPLETED.equals(batchStatus)) {
            log.info("Batch has completed successfully!");
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.badRequest().build();
    }

}
