package com.app.domain.exception

/**
 * 네트워크가 연결되지 않은 상태에서 서버 저장을 시도했을 때 사용합니다.
 */
class NetworkUnavailableException : Exception("Network unavailable")
