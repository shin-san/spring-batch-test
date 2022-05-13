package au.com.jc.batch.model;

import lombok.*;

import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.NONE)
public class Record {

    @XmlAttribute(name = "id")
    private int id;

    @XmlElement(name = "date")
    private String date;

    @XmlElement(name = "impression")
    private String impression;

    @XmlElement(name = "clicks")
    private int clicks;

    @XmlElement(name = "earning")
    private BigDecimal earning;

}
