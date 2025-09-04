package io.yavero.aterna.domain.model.quest

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class QuestType {
    @SerialName("deep_work")
    DEEP_WORK,

    @SerialName("learning")
    LEARNING,

    @SerialName("creative")
    CREATIVE,

    @SerialName("training")
    TRAINING,

    @SerialName("admin")
    ADMIN,

    @SerialName("break")
    BREAK,

    @SerialName("other")
    OTHER
}
