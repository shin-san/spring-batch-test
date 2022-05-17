package au.com.jc.batch.jobs.report1;

import au.com.jc.batch.model.Record;
import au.com.jc.batch.model.Report;
import au.com.jc.batch.util.Constants;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class Report1Configuration {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    private Tasklet moveFileTasklet;

    @Autowired
    public Report1Configuration(JobBuilderFactory jobBuilderFactory,
                                StepBuilderFactory stepBuilderFactory,
                                Tasklet moveFileTasklet) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.moveFileTasklet = moveFileTasklet;
    }

    @Value("${input.report1.folder}")
    private String inputFolder;

    @Bean
    @StepScope
    public ItemStreamReader<Record> report1ItemReader(@Value("#{jobParameters['FILE_NAME']}") String filename) {

        StaxEventItemReader<Record> xmlFileReader = new StaxEventItemReader<>();
        xmlFileReader.setResource(new PathResource(inputFolder + filename));

        //instantiate a new Jaxb2Marshaller
        Jaxb2Marshaller xmlMarshaller = new Jaxb2Marshaller();
        //define the Jaxb annotated classes to be recognized in the JAXBContext
        xmlMarshaller.setClassesToBeBound(Report.class);

        xmlFileReader.setFragmentRootElementName("record");
        xmlFileReader.setUnmarshaller(xmlMarshaller);

        return xmlFileReader;
    }

    @Bean
    public JdbcBatchItemWriter<Record> writeReport1ToDB(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Record>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO record1 (id,date,impression,clicks,earning)\n" +
                        "VALUES (:id,:date,:impression,:clicks,:earning)\n" +
                        "ON CONFLICT (id)\n" +
                        "DO UPDATE set\n" +
                        "date = :date,\n" +
                        "impression = :impression,\n" +
                        "clicks = :clicks,\n" +
                        "earning = :earning")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Record, Record> report1Processor() {
        return new Report1Processor();
    }

    @Bean
    public Step transformReport1XMLtoPojo(ItemStreamReader<Record> report1ItemReader,
            ItemProcessor<Record, Record> report1Processor,
                                   JdbcBatchItemWriter<Record> writeReport1ToDB) {
        return stepBuilderFactory.get("transformReport1XMLtoPojo")
                .<Record, Record> chunk(1)
                .reader(report1ItemReader)
                .processor(report1Processor)
                .writer(writeReport1ToDB)
                .build();
    }

    @Bean
    public Step moveReport1ToSuccess() {
        return stepBuilderFactory.get("moveReport1ToSuccess")
                .tasklet(moveFileTasklet)
                .build();
    }

    @Bean
    public Job sendReport1ToDB(Step transformReport1XMLtoPojo,
                              Step moveReport1ToSuccess) {
        return jobBuilderFactory.get(Constants.REPORT1_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(transformReport1XMLtoPojo)
                .next(moveReport1ToSuccess)
                .end()
                .build();
    }
}
