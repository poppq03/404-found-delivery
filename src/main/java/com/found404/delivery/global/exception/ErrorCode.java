package com.found404.delivery.global.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    // ===== 공통 =====
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않습니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "필드 형식(길이 등)이 제약조건에 맞지 않습니다."),
    INVALID_PAGE_SIZE(HttpStatus.BAD_REQUEST, "페이지 크기는 10, 30, 50만 허용됩니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지는 5MB를 초과할 수 없습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST, "허용되지 않은 파일 확장자입니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),

    // ===== User =====
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 username입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 email입니다."),
    INVALID_USERNAME(HttpStatus.BAD_REQUEST, "username 형식이 올바르지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "password 형식이 올바르지 않습니다. (8~15자, 대소문자+숫자+특수문자)"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "username 또는 password가 일치하지 않습니다."),
    ACCOUNT_DELETED(HttpStatus.FORBIDDEN, "탈퇴(Soft Delete) 처리된 계정입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않거나 권한이 없는 유저입니다."),
    USER_ALREADY_DELETED(HttpStatus.CONFLICT, "이미 삭제된 계정입니다."),
    INVALID_CURRENT_PASSWORD(HttpStatus.FORBIDDEN, "현재 비밀번호가 일치하지 않습니다."),
    SAME_AS_OLD_PASSWORD(HttpStatus.CONFLICT, "새 비밀번호가 기존 비밀번호와 동일합니다."),
    ROLE_UPDATE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "본인의 role 필드는 수정할 수 없습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "role은 CUSTOMER 또는 OWNER만 가능합니다."),

    // ===== Store =====
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가게가 존재하지 않습니다."),
    NOT_STORE_OWNER(HttpStatus.FORBIDDEN, "본인 소유 가게가 아닙니다."),

    // ===== Menu =====
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴가 존재하지 않습니다."),
    MENU_UNAVAILABLE(HttpStatus.CONFLICT, "품절 또는 숨김 처리된 메뉴입니다."),
    AI_GENERATION_FAILED(HttpStatus.BAD_GATEWAY, "AI 설명 생성에 실패했습니다."),

    // ===== Cart =====
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 항목이 존재하지 않습니다."),
    CART_ITEM_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "장바구니 메뉴 수 상한을 초과했습니다."),
    DIFFERENT_STORE_MENU(HttpStatus.BAD_REQUEST, "서로 다른 가게의 메뉴가 포함되어 있습니다."),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 1 이상이어야 합니다."),

    // ===== Order =====
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "취소할 수 없는 주문 상태입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.CONFLICT, "허용되지 않은 상태 변경입니다."),
    INVALID_STATUS_VALUE(HttpStatus.BAD_REQUEST, "허용되지 않은 상태값입니다."),
    CANCEL_TIME_EXPIRED(HttpStatus.CONFLICT, "주문 생성 후 5분이 지나 취소할 수 없습니다."),
    ORDER_ALREADY_ACCEPTED(HttpStatus.CONFLICT, "이미 수락된 주문입니다."),
    NOT_ORDER_OWNER(HttpStatus.FORBIDDEN, "본인 주문이 아닙니다."),
    INVALID_ORDER_ITEM(HttpStatus.BAD_REQUEST, "주문 상품 정보가 올바르지 않습니다."),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "완료된 주문에 대해서만 가능합니다."),
    STORE_NOT_AVAILABLE(HttpStatus.CONFLICT, "현재 주문할 수 없는 가게입니다."),
    MIN_ORDER_PRICE_NOT_MET(HttpStatus.BAD_REQUEST, "최소 주문 금액을 충족하지 못했습니다."),

    // ===== Payment =====
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 내역이 존재하지 않습니다."),
    ALREADY_PAID_ORDER(HttpStatus.BAD_REQUEST, "이미 결제된 주문입니다."),
    ALREADY_CANCELED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 취소된 결제입니다."),
    INVALID_PAYMENT_METHOD(HttpStatus.BAD_REQUEST, "CARD 외 결제 수단은 지원하지 않습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "결제 완료 상태가 아니어서 취소할 수 없습니다."),

    // ===== Review =====
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰가 존재하지 않습니다."),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 해당 주문에 리뷰가 작성되었습니다."),
    ALREADY_REVIEWED_ORDER(HttpStatus.BAD_REQUEST, "이미 리뷰가 작성된 주문입니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "별점은 1~5 사이 정수여야 합니다."),

    // ===== Address =====
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지가 존재하지 않습니다."),
    FORBIDDEN_ADDRESS(HttpStatus.FORBIDDEN, "본인의 배송지가 아닙니다."),
    INVALID_ADDRESS(HttpStatus.BAD_REQUEST, "주소 입력값이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}