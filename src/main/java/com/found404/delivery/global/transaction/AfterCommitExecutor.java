package com.found404.delivery.global.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

/**
 * 용도: DB 트랜잭션이 커밋된 뒤에 실행할 작업 컴포넌트
 * 1. 롤백으로 되돌릴 수 없는 외부 작업을 트랜잭션 커밋 전 실행하지 않도록 할 때 사용합니다 (예: S3 파일 삭제)
 * 2. 활성화된 DB 트랜잭션이 없으면 전달받은 작업을 실행합니다
 */

@Component
public class AfterCommitExecutor {

     // param: action - 실행할 작업
     // throws: NullPointerException - action이 null인 경우
    public void execute(Runnable action) {
        Objects.requireNonNull(action, "action은 null일 수 없습니다.");
        if (!canRegisterAfterCommitAction()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        action.run();
                    }
                }
        );
    }
    private boolean canRegisterAfterCommitAction() {
        return TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive();
    }
}