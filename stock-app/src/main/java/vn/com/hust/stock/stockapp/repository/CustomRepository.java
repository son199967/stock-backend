package vn.com.hust.stock.stockapp.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomRepository<E,ID> extends CrudRepository<E, ID>, JpaSpecificationExecutor<E> {

}
