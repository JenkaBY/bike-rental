package com.github.jenkaby.bikerental.finance.infrastructure.persistence.specification;

import com.github.jenkaby.bikerental.finance.infrastructure.persistence.entity.TransactionJpaEntity;
import jakarta.persistence.criteria.JoinType;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.GreaterThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.LessThan;
import net.kaczmarzyk.spring.data.jpa.web.annotation.And;
import net.kaczmarzyk.spring.data.jpa.web.annotation.JoinFetch;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.jpa.domain.Specification;


@JoinFetch(paths = "records", alias = "r", joinType = JoinType.INNER)
@And({
        @Spec(path = SpecConstant.TransactionField.CUSTOMER_ID, params = "customerId", spec = Equal.class),
        @Spec(path = "r." + SpecConstant.TransactionField.LEDGER_TYPE, constVal = {"CUSTOMER_HOLD", "CUSTOMER_WALLET"}, spec = In.class),
        @Spec(path = SpecConstant.TransactionField.RECORDED_AT, params = "fromDate", spec = GreaterThanOrEqual.class, config = SpecConstant.INSTANT_FORMAT),
        @Spec(path = SpecConstant.TransactionField.RECORDED_AT, params = "toDate", spec = LessThan.class, config = SpecConstant.INSTANT_FORMAT),
        @Spec(path = SpecConstant.TransactionField.SOURCE_ID, params = "sourceId", spec = Equal.class),
        @Spec(path = SpecConstant.TransactionField.SOURCE_TYPE, params = "sourceType", spec = Equal.class),
})
public interface CustomerTransactionsSpec extends Specification<TransactionJpaEntity> {
}
