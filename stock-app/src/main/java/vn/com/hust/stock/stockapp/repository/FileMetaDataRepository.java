package vn.com.hust.stock.stockapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.hust.stock.stockmodel.entity.FileMetaData;

public interface FileMetaDataRepository extends CustomRepository<FileMetaData,String> {
    Page<FileMetaData> findAll(Pageable pageable);
}
