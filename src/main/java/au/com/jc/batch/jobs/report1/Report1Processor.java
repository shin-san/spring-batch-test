package au.com.jc.batch.jobs.report1;


import au.com.jc.batch.model.Record;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class Report1Processor implements ItemProcessor<Record, Record> {

    @Override
    public Record process(final Record record) {
        log.info("Do report processing here...");
        log.info("Record: {}", record);
        return record;
    }

}
