package com.cloudnut.webterm.infrastructure.repo;

import com.cloudnut.webterm.infrastructure.entity.TokenRequestEntity;
import com.cloudnut.webterm.thirdparty.pojo.TokenRequest;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface TokenRequestRepo extends PagingAndSortingRepository<TokenRequestEntity, String>,
        JpaSpecificationExecutor<TokenRequestEntity> {
    boolean existsByToken(String token);
    Optional<TokenRequestEntity> findByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenRequestEntity e WHERE e.token = ?1")
    void deleteByToken(String token);

    @Modifying
    @Query("DELETE FROM TokenRequestEntity e WHERE e.parentToken = ?1")
    void deleteByParentToken(String parentToken);

    List<TokenRequestEntity> findByExpiredTimeAfter(Date expiredDate);
    List<TokenRequestEntity> findByExpiredTimeBefore(Date expiredDate);


    @Modifying
    @Query("SELECT e.token FROM TokenRequestEntity e WHERE e.parentToken = ?1")
    List<String> findByParentToken(String token);
}
