package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.hust.stock.stockmodel.data.PositionLeader;
import vn.com.hust.stock.stockmodel.until.PositionLeaderConverter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "LeaderCompany")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaderCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private String idCart;
    private String domicile;
    private String placeOfBirth;
    private String resident;
    @JsonProperty("current_position")
    @Convert(converter = PositionLeaderConverter.class)
    private List<PositionLeader> currentPosition;


}
