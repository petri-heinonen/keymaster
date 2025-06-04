package fi.pmh.keymaster.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface KeyRepository extends JpaRepository<Key, Long> {
    Key findByKeyId(String keyId);
    List<Key> findByClientIdOrderByCreatedAtDesc(String clientId);
    List<Key> findByClientIdAndIsPublishedTrueOrderByCreatedAtDesc(String clientId);

    @Query("SELECT DISTINCT k.clientId FROM Key k ORDER BY k.clientId ASC")
    List<String> findAllClients();
}