package vn.com.hust.stock.stockmodel.user;

import lombok.Data;
import vn.com.hust.stock.stockmodel.enumm.ERole;

import javax.persistence.*;

@Entity
@Table(name = "roles")
@Data
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ERole role;

}
