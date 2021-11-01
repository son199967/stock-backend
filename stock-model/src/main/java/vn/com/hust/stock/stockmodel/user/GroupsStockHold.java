package vn.com.hust.stock.stockmodel.user;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Getter;
import lombok.Setter;
import vn.com.hust.stock.stockmodel.until.StringArraysConverter;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Setter
public class GroupsStockHold {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nameGroups;

    @Convert(converter = StringArraysConverter.class)
    private List<String> symbols;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user")
    private User user;
}
