package fi.pmh.keymaster.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KeyRepository extends JpaRepository<Key, Long> {
    List<Key> findByClientIdOrderByCreatedAtDesc(String clientId);
    List<Key> findByClientIdAndIsPublishedTrueOrderByCreatedAtDesc(String clientId);
    Key findByKeyId(String keyId);

    List<Key> findAllByOrderByClientIdAscCreatedAtDesc();

    @Query("SELECT DISTINCT k.clientId FROM Key k ORDER BY k.clientId ASC")
    List<String> findAllClients();
}