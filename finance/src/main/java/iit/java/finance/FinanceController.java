package iit.java.finance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/Finance")
public class FinanceController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/Dashboard")
    public String dashboard(Model model) {
        try {
            model.addAttribute("totalIncome", transactionService.calculateTotalIncome());
            model.addAttribute("totalExpenses", transactionService.calculateTotalExpenses());
            model.addAttribute("netBalance", transactionService.calculateNetBalance());
            model.addAttribute("recentTransactions", transactionService.getRecentTransactions());
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading dashboard data. Please try again.");
            return "error";
        }
        return "Dashboard";
    }

    @GetMapping("/Entry")
    public String showTransactionEntryPage(Model model) {
        model.addAttribute("transaction", new Transaction());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "Entry";
    }
    @GetMapping("/api/finance-summary")
    @ResponseBody
    public Map<String, BigDecimal> getFinanceSummary() {
        Map<String, BigDecimal> financeSummary = new HashMap<>();
        financeSummary.put("totalIncome", transactionService.calculateTotalIncome());
        financeSummary.put("totalExpenses", transactionService.calculateTotalExpenses());
        return financeSummary;
    }
    @PostMapping("/api/transactions")
    public String addTransaction(@ModelAttribute Transaction transaction, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        try {
            Long categoryId = transaction.getCategory().getId();
            Category category = categoryService.getCategoryById(categoryId);
            transaction.setCategory(category);

            transactionService.saveTransaction(transaction);
            redirectAttributes.addFlashAttribute("message", "Transaction successfully added!");
            return "redirect:/Finance/History";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add transaction: " + e.getMessage());
            return "redirect:/Finance/Entry";
        }
    }

    @RequestMapping("/History")
    public String history(Model model) {
        model.addAttribute("transactions", transactionService.getFilteredTransactions());
        return "History";
    }

    @RequestMapping("/Manage")
    public String manage(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", new Category());
        return "Manage";
    }

    @PostMapping("/api/categories")
    public String addCategory(@ModelAttribute Category category, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "Manage";
        }
        if (categoryService.existsByName(category.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Category already exists!");
            return "redirect:/Finance/Manage";
        }

        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("message", "Category successfully added!");
        return "redirect:/Finance/Manage";
    }

    @PostMapping("/api/categories/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        categoryService.deleteCategory(id);
        redirectAttributes.addFlashAttribute("message", "Category successfully deleted!");
        return "redirect:/Finance/Manage";
    }

    @RequestMapping("/Register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "Register";
    }

    @PostMapping("/Register")
    public String registerUser(@ModelAttribute("user") User user, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "Register";
        }
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "error.user", "Passwords do not match");
            return "Register";
        }

        try {
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("message", "Registration successful!");
            return "redirect:/Finance/Login";
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "error.user", e.getMessage());
            return "Register";
        }
    }

    @GetMapping("/Login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "Login";
    }

    @PostMapping("/Login")
    public String login(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        if (authenticate(user.getUserName(), user.getPassword())) {
            return "redirect:/Finance/Dashboard";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid credentials");
            return "redirect:/Finance/Login";
        }
    }

    private boolean authenticate(String userName, String password) {
        User foundUser = userService.findByUserName(userName);
        return foundUser != null && foundUser.getPassword().equals(password);
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        return "error";
    }

}
