package iit.java.finance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Repository
public interface transactRepo extends JpaRepository<Transaction, Long> {

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.transactionType = :type")
    BigDecimal sumByType(@Param("type") String type);

    List<Transaction> findTop10ByOrderByTransactionDateDesc();

    @Query("SELECT c.name AS category, SUM(t.amount) AS total FROM Transaction t JOIN t.category c WHERE t.transactionType = 'EXPENSE' GROUP BY c.name")
    Map<String, BigDecimal> sumExpensesByCategory();


}
