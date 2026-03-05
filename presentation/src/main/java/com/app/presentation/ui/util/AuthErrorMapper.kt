package com.app.presentation.ui.util

import com.app.domain.exception.NetworkUnavailableException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 앱 전역 예외를 사용자에게 보여줄 토스트 메시지로 변환합니다.
 */
object AuthErrorMapper {

    fun map(e: Throwable): String {
        return when {
            // 네트워크 없음 -> 와이파이 OFF
            isNetworkError(e) -> "네트워크 연결 상태가 좋지 않습니다"

            // Firebase Timeout / 서버 지연
            e is SocketTimeoutException -> "서버 지연"
            isTimeoutRelated(e) -> "서버 지연"

            // Firebase 이메일 없음(미가입) -> 가입 안된 계정
            e is FirebaseAuthInvalidUserException -> "가입 안된 계정"

            // 이메일 또는 비밀번호 오류 -> 잘못된 계정입니다
            e is FirebaseAuthInvalidCredentialsException -> "잘못된 계정입니다"

            // Firebase Auth 오류 -> 토큰 문제
            e is FirebaseAuthException -> "토큰 문제"

            // Firestore 실패 -> DB 저장 실패 (presentation 모듈에 Firestore 의존성 없음 → 클래스명으로 판별)
            isFirestoreException(e) -> "DB 저장 실패"

            // 회원탈퇴 실패 -> unlink 오류
            isUnlinkError(e) -> "unlink 오류"

            else -> "기타"
        }
    }

    /**
     * 물 기록 저장/로드 실패 시 사용. 네트워크 오류면 "네트워크 연결 상태가 좋지 않습니다" 표시.
     */
    fun mapForWaterUpdate(e: Throwable): String {
        return when {
            e is NetworkUnavailableException -> "네트워크 연결 상태가 좋지 않습니다"
            isNetworkError(e) -> "네트워크 연결 상태가 좋지 않습니다"
            else -> map(e)
        }
    }

    /**
     * OAuth(Google/Kakao/Naver) 로그인 실패 시 사용.
     * 알 수 없는 오류는 "로그인 실패"로 통일.
     */
    fun mapForOAuth(e: Throwable): String {
        return when {
            isNetworkError(e) -> "네트워크 연결 상태가 좋지 않습니다"
            e is SocketTimeoutException -> "서버 지연"
            isTimeoutRelated(e) -> "서버 지연"
            e is FirebaseAuthException -> "토큰 문제"
            isFirestoreException(e) -> "DB 저장 실패"
            else -> "로그인 실패"
        }
    }

    private fun isNetworkError(e: Throwable): Boolean {
        return e is UnknownHostException ||
            e is ConnectException ||
            e.cause?.let { isNetworkError(it) } == true
    }

    private fun isTimeoutRelated(e: Throwable): Boolean {
        if (e is SocketTimeoutException) return true
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("timeout") || msg.contains("timed out") ||
            e.cause?.let { isTimeoutRelated(it) } == true
    }

    private fun isUnlinkError(e: Throwable): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("unlink") || msg.contains("회원탈퇴") ||
            e.cause?.let { isUnlinkError(it) } == true
    }

    private fun isFirestoreException(e: Throwable): Boolean {
        val name = e::class.java.name
        return name == "com.google.firebase.firestore.FirebaseFirestoreException" ||
            e.cause?.let { isFirestoreException(it) } == true
    }
}
