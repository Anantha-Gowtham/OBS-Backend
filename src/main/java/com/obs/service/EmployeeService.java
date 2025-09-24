package com.obs.service;

import com.obs.model.*;
import com.obs.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {
    private final KycRequestRepository kycRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public EmployeeService(KycRequestRepository kycRepository, TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.kycRepository = kycRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    public List<KycRequest> pendingKyc(){ return kycRepository.findByStatus(KycStatus.PENDING); }
    public Map<String,Object> processKyc(String id, String decision, String comments){
        KycRequest req = kycRepository.findById(Long.parseLong(id)).orElseThrow();
        req.setStatus("APPROVE".equalsIgnoreCase(decision) ? KycStatus.APPROVED : KycStatus.REJECTED);
        req.setComments(comments); kycRepository.save(req);
        return Map.of("kycId", req.getId(), "status", req.getStatus().name());
    }
    public List<Transaction> pendingTransactions(){ return transactionRepository.findByStatus(TransactionStatus.PENDING); }
    public Map<String,Object> flagTransaction(String id, String reason){
        Transaction tx = transactionRepository.findById(Long.parseLong(id)).orElseThrow();
        tx.setStatus(TransactionStatus.FLAGGED); tx.setFlagReason(reason); transactionRepository.save(tx);
        return Map.of("transactionId", tx.getId(), "status", tx.getStatus().name(), "reason", tx.getFlagReason());
    }
    public List<Account> pendingAccounts(){ return accountRepository.findByStatus(AccountStatus.PENDING); }
    public Map<String,Object> processAccount(String id, String decision){
        Account acc = accountRepository.findById(Long.parseLong(id)).orElseThrow();
        acc.setStatus("APPROVE".equalsIgnoreCase(decision) ? AccountStatus.ACTIVE : AccountStatus.REJECTED);
        accountRepository.save(acc);
        return Map.of("accountId", acc.getId(), "status", acc.getStatus().name());
    }
}

