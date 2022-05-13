package au.com.jc.batch.jobs;

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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.PathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class ReportConfiguration {

    private JobBuilderFactory jobBuilderFactory;

    private StepBuilderFactory stepBuilderFactory;

    public ReportConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Value("${report.input.folder}")
    private String inputFolder;

    @Bean
    @StepScope
    public ItemStreamReader<Record> reportItemReader(@Value("#{jobParameters['FILE_NAME']}") String filename) {

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
    public JdbcBatchItemWriter<Record> writeToDB(DataSource dataSource) {

        return new JdbcBatchItemWriterBuilder<Record>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO record (id,date,impression,clicks,earning) VALUES (:id , :date, :impression, :clicks, :earning)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Record, Record> reportProcessor() {
        return new ReportProcessor();
    }

    @Bean
    public Step transformXMLtoPojo(ItemStreamReader<Record> reportItemReader,
            ItemProcessor<Record, Record> reportProcessor,
                                   JdbcBatchItemWriter<Record> writeToDB) {
        return stepBuilderFactory.get("transformXMLtoPojo")
                .<Record, Record> chunk(1)
                .reader(reportItemReader)
                .processor(reportProcessor)
                .writer(writeToDB)
                .build();
    }


    @Bean
    public Job sendReportToDB(Step transformXMLtoPojo) {
        return jobBuilderFactory.get(Constants.REPORT_JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .flow(transformXMLtoPojo)
                .end()
                .build();
    }
}
