package au.com.jc.batch.model;

import lombok.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@XmlRootElement(name = "report")
@XmlAccessorType (XmlAccessType.FIELD)
public class Report {

    @XmlElement(name = "record")
    private List<Record> record;

}
