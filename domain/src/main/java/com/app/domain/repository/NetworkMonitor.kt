package com.app.domain.repository

/**
 * 네트워크 연결 여부를 확인합니다.
 * 오프라인일 때 물 기록 저장전, 토스트 안내용으로 사용합니다.
 */
interface NetworkMonitor {
    fun isConnected(): Boolean
}
