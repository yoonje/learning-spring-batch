package com.community.batch.repository;

import com.community.batch.domain.User;
import com.community.batch.domain.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Created by youngjae on 2018. 3. 3..
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findByCreatedDateBeforeAndStatusEquals(LocalDateTime localDateTime, UserStatus status);

    List<User> findByUpdatedDateBeforeAndStatusEquals(LocalDateTime localDateTime, UserStatus status);
}
