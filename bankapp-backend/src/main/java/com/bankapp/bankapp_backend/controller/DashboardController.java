    package com.bankapp.bankapp_backend.controller;

    import com.bankapp.bankapp_backend.model.Account;
    import com.bankapp.bankapp_backend.model.Customer;
    import com.bankapp.bankapp_backend.model.CustomerAccount;
    import com.bankapp.bankapp_backend.model.Transaction;

    import com.bankapp.bankapp_backend.repository.AccountRepository;
    import com.bankapp.bankapp_backend.repository.CustomerAccountRepository;
    import com.bankapp.bankapp_backend.repository.CustomerRepository;
    import com.bankapp.bankapp_backend.repository.TransactionRepository;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.util.*;

    @RestController
    @RequestMapping("/api/dashboard")
    @CrossOrigin(originPatterns = "*", allowCredentials = "true")
    public class DashboardController {

        @Autowired
        private CustomerRepository customerRepo;

        @Autowired
        private CustomerAccountRepository customerAccountRepo;

        @Autowired
        private AccountRepository accountRepo;

        @Autowired
        private TransactionRepository transactionRepo;

        @GetMapping("/data/{customerId}")
        public ResponseEntity<?> getDashboardData(@PathVariable int customerId) {

            Customer customer = customerRepo.findById(customerId).orElse(null);
            if (customer == null)
                return ResponseEntity.badRequest().body("Invalid customerId");

            List<CustomerAccount> linked = customerAccountRepo.findByCustomerID(customerId);

            List<Map<String, Object>> accountsList = new ArrayList<>();

            for (CustomerAccount ca : linked) {
                Account acc = accountRepo.findByAccountNo(ca.getAccountNo()).orElse(null);
                if (acc == null) continue;

                Map<String, Object> map = new HashMap<>();
                map.put("accountNo", acc.getAccountNo());
                map.put("type", acc.getAccountType());
                map.put("balance", acc.getAccountBalance());
                map.put("nickname", ca.getNickname());
                map.put("isPrimary", ca.getIsPrimary());
                accountsList.add(map);
            }

            // Fetch recent transactions only for primary account
            List<Transaction> recent = new ArrayList<>();

            if (!accountsList.isEmpty()) {
                String firstAcc = accountsList.get(0).get("accountNo").toString();
                recent = transactionRepo.findTop5BySenderAccountNoOrderByTransactionDateDesc(firstAcc);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("customerName", customer.getName());
            response.put("accounts", accountsList);
            response.put("recentTransactions", recent);

            return ResponseEntity.ok(response);

        }
    }
