package vn.com.hust.stock.stockjob.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CustomRepository<E,ID> extends CrudRepository<E, ID>, JpaSpecificationExecutor<E> {
}
