package com.xorbank.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xorbank.models.Account;
import com.xorbank.models.Transaction;
import com.xorbank.repository.AccountRepository;
import com.xorbank.repository.TransactionRepository;
import com.xorbank.services.FundTransferService;

@Service
public class FundTransferServiceImpl implements FundTransferService {

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private AccountRepository accountRepository;

	public FundTransferServiceImpl() {
	}

	@Override
	public boolean checkAccountValidity(int accountId) {
		return accountRepository.existsById(accountId);
	}

	
	@Override
	public double checkAccountBalance(int accountId) {
		return accountRepository.getById(accountId).getBalance();
	}
	
	@Override
	public boolean getAccountStatus(int accountId) {
		
		return accountRepository.getById(accountId).getAccountStatus();
	}

	@Override
	public Transaction sendAmount(int fromAccount, int toAccount, double amount,String description) throws Exception {
		Transaction transaction = new Transaction();
		transaction.setFromAccount(fromAccount);
		transaction.setToAccount(toAccount);
		transaction.setDescription(description);
		transaction.setAmount(amount);
		transaction.setTransactionDate(LocalDateTime.now().toString());
		if (fromAccount != toAccount) {
			
			if (checkAccountValidity(toAccount) && checkAccountValidity(fromAccount)) {
				
				if (checkAccountBalance(fromAccount) > amount) {
					Account from = accountRepository.getById(fromAccount);
					Account to = accountRepository.getById(toAccount);
					from.setBalance(from.getBalance() - amount);
					to.setBalance(to.getBalance()+amount);
					accountRepository.save(from);
					accountRepository.save(to);
					transaction.setTransactionStatus("SUCCESS");
					transactionRepository.save(transaction);	
					getAllTransactionsFromAccount(fromAccount);
					
					
				} else {
					transaction.setTransactionStatus("FAILED");
					transactionRepository.save(transaction);
					throw new Exception("Insufficient Balance");
				}

			} else {
				transaction.setTransactionStatus("FAILED");
				transactionRepository.save(transaction);
				throw new Exception("Account Deactivated");
			}

		} else {
			transaction.setTransactionStatus("FAILED");
			transactionRepository.save(transaction);
			throw new Exception("Sender and receiver account cannot be same!");
		}
		return transaction;
	}

	@Override
	public List<Transaction> getAllTransactionsFromAccount(int accountId) {
		System.out.println("--------------------------------------");
		for(Transaction t:transactionRepository.findTransactionByFromAccount(accountId)) {
			System.out.println(t);
		}
		
		System.out.println("--------------------------------------");
		return transactionRepository.findTransactionByFromAccount(accountId);
	}

	

}