package com.whale.api.archive.application.port.`in`

import java.util.UUID

interface DeleteArchiveItemUseCase {
    /**
     * 아카이브 아이템을 삭제합니다.
     * - 파일 시스템에서 실제 파일 삭제
     * - 라이브 포토 비디오 파일 삭제 (있는 경우)
     * - DB에서 메타데이터 삭제
     * - DB에서 아카이브 아이템 삭제
     *
     * @param itemIdentifier 삭제할 아카이브 아이템 식별자
     * @throws IllegalArgumentException 아이템을 찾을 수 없는 경우
     */
    fun deleteArchiveItem(itemIdentifier: UUID)
}

