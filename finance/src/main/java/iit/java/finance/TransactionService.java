package iit.java.finance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;


@Service
public class TransactionService {

    @Autowired
    private transactRepo transactionRepository;

    public BigDecimal calculateTotalIncome() {
        BigDecimal totalIncome = transactionRepository.sumByType("income");
        return (totalIncome != null) ? totalIncome : BigDecimal.ZERO; // Handle null case
    }

    public BigDecimal calculateTotalExpenses() {
        BigDecimal totalExpenses = transactionRepository.sumByType("expense");
        return (totalExpenses != null) ? totalExpenses : BigDecimal.ZERO; // Handle null case
    }

    public BigDecimal calculateNetBalance() {
        BigDecimal totalIncome = calculateTotalIncome();
        BigDecimal totalExpenses = calculateTotalExpenses();
        return totalIncome.subtract(totalExpenses);
    }

    public List<Transaction> getRecentTransactions() {
        return transactionRepository.findTop10ByOrderByTransactionDateDesc();
    }

    public Map<String, BigDecimal> getExpensesByCategory() {
        return transactionRepository.sumExpensesByCategory();
    }

    public void saveTransaction(Transaction transaction) {
        if (transaction.getAmount() == null || transaction.getDescription() == null || transaction.getTransactionType() == null || transaction.getCategory() == null) {
            throw new IllegalArgumentException("All transaction fields must be filled");
        }

        transaction.setTransactionDate(LocalDateTime.now()); // Set the current date/time
        transactionRepository.save(transaction);
        System.out.println("Transaction saved: " + transaction); // Add logging
    }


    public List<Transaction> getFilteredTransactions() {
        return transactionRepository.findAll();
    }


}
