package com.example.banking.account.mapper;

import com.example.banking.account.dto.AccountResponse;
import com.example.banking.account.dto.TransactionResponse;
import com.example.banking.account.entity.AccountTransaction;
import com.example.banking.account.entity.BankAccount;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "transactions", expression = "java(toTransactionResponses(account.getTransactions()))")
    AccountResponse toResponse(BankAccount account);

    List<AccountResponse> toResponses(List<BankAccount> accounts);

    TransactionResponse toTransactionResponse(AccountTransaction transaction);

    default List<TransactionResponse> toTransactionResponses(List<AccountTransaction> transactions) {
        if (transactions == null) {
            return List.of();
        }
        return transactions.stream()
                .sorted(Comparator.comparing(AccountTransaction::getPostedAt).reversed())
                .map(this::toTransactionResponse)
                .toList();
    }
}
