package com.whale.api.archive.application.port.out

import java.util.UUID

interface DeleteArchiveItemOutput {
    /**
     * 아카이브 아이템을 삭제합니다.
     *
     * @param itemIdentifier 삭제할 아카이브 아이템 식별자
     */
    fun deleteArchiveItem(itemIdentifier: UUID)

    /**
     * 아카이브 아이템의 모든 메타데이터를 삭제합니다.
     *
     * @param itemIdentifier 아카이브 아이템 식별자
     */
    fun deleteArchiveMetadata(itemIdentifier: UUID)
}
