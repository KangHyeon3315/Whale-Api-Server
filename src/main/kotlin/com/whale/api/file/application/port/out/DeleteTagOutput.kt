package com.whale.api.file.application.port.out

import java.util.UUID

interface DeleteTagOutput {
    fun deleteByIdentifiers(identifiers: List<UUID>)
}
