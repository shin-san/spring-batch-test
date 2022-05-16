package au.com.jc.batch.tasklet;

import au.com.jc.batch.util.Constants;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.UnexpectedJobExecutionException;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FileMovingTasklet implements Tasklet {

    @Value("${input.report.folder}")
    private String reportFolder;

    @Value("${input.report1.folder}")
    private String report1Folder;

    @Value("${success.report.folder}")
    private String successReportFolder;

    @Value("${success.report1.folder}")
    private String successReport1Folder;

    @Override
    public RepeatStatus execute(StepContribution contribution,
                                ChunkContext chunkContext) throws Exception {

        String jobName = chunkContext.getStepContext().getJobName();
        JobParameters jobParameters = chunkContext.getStepContext().getStepExecution().getJobParameters();
        String inputFilename = jobParameters.getString(Constants.FILE_NAME);
        File srcFile;
        File destFile;
        switch (jobName) {
            case Constants.REPORT_JOB_NAME:
                srcFile = new File(reportFolder + inputFilename);
                destFile = new File(successReportFolder + inputFilename);
                break;
            case Constants.REPORT1_JOB_NAME:
                srcFile = new File(report1Folder + inputFilename);
                destFile = new File(successReport1Folder + inputFilename);
                break;
            default:
                throw new UnexpectedJobExecutionException("Could not move file " +
                        inputFilename);
        }

        Assert.isTrue(!destFile.isDirectory(), "provided path is not a directory");

        Files.move(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        return RepeatStatus.FINISHED;
    }
}