package au.com.jc.batch.controller;

import au.com.jc.batch.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Slf4j
@RestController
@RequestMapping("/api")
public class BatchController {

    JobLauncher jobLauncher;

    JobExplorer jobExplorer;

    JobRepository jobRepository;

    Job sendReportToDB;

    @Value("${input.report.folder}")
    private String reportFolder;

    @Value("${input.report1.folder}")
    private String report1Folder;

    @Value("${error.report.folder}")
    private String reportErrorFolder;

    @Value("${error.report1.folder}")
    private String report1ErrorFolder;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

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
    public @ResponseBody ResponseEntity<String> runBatch() {


        try {

            synchronized (this) {
                executorService.schedule(this::initiateBatchProcess, 0, TimeUnit.MILLISECONDS);
            }

        }
        catch (RejectedExecutionException e) {
            log.warn("No free threads available");
        }
        catch (Exception e) {
            log.error("Exception encountered: {}", e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    private void initiateBatchProcess() {
        log.info("Starting Report Batch process");

        inputFolderArrayInit().forEach((k,v) -> {
            // k = job name
            // v = directory
            try {
                List<File> files = checkFolders(v);

                if (files.size() > 0) {
                    files.forEach(file -> {
                        processFiles(file, k);
                    });
                } else {
                    log.info("No files found in {}, skipping...", v);
                }
            } catch (IOException e) {
                log.error("checkFolders() failed to retrieve files: {}", e.getMessage());
            }
        });
    }

    private void processFiles(File file, String jobName) {
        log.info("Report Batch: Processing {}", file.getAbsolutePath());

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String filename = file.getName();

        try {
            String fileHash = generateHash(file);
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString(Constants.FILE_NAME, filename)
                    .addString(Constants.TIMESTAMP, dateFormat.format(LocalDate.now()),false)
                    .addString(Constants.FILE_HASH, fileHash)
                    .toJobParameters();

            jobLauncher.run(sendReportToDB,
                    jobParameters);
        } catch (JobExecutionAlreadyRunningException e) {
            log.info("Process is already running. Skipping job");
        } catch (JobInstanceAlreadyCompleteException e) {
            log.warn("Process is already complete. Skipping job");
            moveFile(file, jobName);
        } catch (Exception e) {
            log.warn("Encountered unexpected errors: {}", e.getMessage());
        }
    }

    private void moveFile(File file, String jobName) {
        log.warn("Moving {} to /error folder", file.getName());
        String errorDir = "";
        switch (jobName) {
            case Constants.REPORT_JOB_NAME:
                errorDir = reportErrorFolder + file.getName();
                break;
            case Constants.REPORT1_JOB_NAME:
                errorDir = report1ErrorFolder + file.getName();
                break;
        }

        try {
            File destFile = FileUtils.getFile(errorDir);
            Files.move(file.toPath(), destFile.toPath(), REPLACE_EXISTING);
        }
        catch (IOException e) {
            log.error("Failed to move {} to {}", file.getName(), errorDir);
        }
    }

    private List<File> checkFolders(String inputFolder) throws IOException {
        Path dir = Paths.get(inputFolder);
        List<File> files;
        try (Stream<Path> stream = Files.list(dir)) {
            files = stream
                    .map(Path::toFile)
                    .filter(File::isFile)
                    .filter(file -> file.getName().contains(".xml"))
                    .collect(Collectors.toList());
        }
        return files;
    }

    private Map<String, String> inputFolderArrayInit() {
        HashMap<String,String> folderList = new HashMap<>();
        folderList.put(Constants.REPORT_JOB_NAME, reportFolder);
        folderList.put(Constants.REPORT1_JOB_NAME, report1Folder);
        return folderList;
    }

    private String generateHash(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.sha256Hex(fis);
        }
    }

}
